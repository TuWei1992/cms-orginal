package com.zving.framework.ui.zhtml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import com.zving.framework.Config;
import com.zving.framework.Constant;
import com.zving.framework.collection.CacheMapx;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.template.ITemplateManager;
import com.zving.framework.template.TemplateCompiler;
import com.zving.framework.template.TemplateExecutor;
import com.zving.framework.template.exception.TemplateCompileException;
import com.zving.framework.template.exception.TemplateNotFoundException;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.ui.resource.UIResourceFile;
import com.zving.framework.ui.weaver.ZhtmlWeaveService;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.LogUtil;

/**
 * Zhtml页面管理器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-6-28
 */
public class ZhtmlManager implements ITemplateManager {
	private static CacheMapx<String, TemplateExecutor> map = new CacheMapx<String, TemplateExecutor>();

	/**
	 * 返回true表示能找到.zhtml文件并执行成功
	 */
	@Override
	public boolean execute(String url, AbstractExecuteContext context) throws TemplateRuntimeException, TemplateCompileException {
		String fileName = url;
		if (fileName.indexOf('?') > 0) {
			fileName = fileName.substring(0, fileName.indexOf('?'));
		}
		if (fileName.indexOf('#') > 0) {
			fileName = fileName.substring(0, fileName.indexOf('#'));
		}
		if (fileName.charAt(0) == '/') {
			fileName = fileName.substring(1);
		}
		TemplateExecutor je = getExecutor(fileName);
		if (context instanceof ZhtmlExecuteContext) {
			// 检查会话
			HttpServletRequest request = ((ZhtmlExecuteContext) context).getRequest();
			if (request != null && !je.isSessionFlag()) {
				request.setAttribute(Constant.NoSession, "true");
			}
		}
		je.execute(context);
		return true;
	}

	public static String getSourceInJar(String fileName) {
		InputStream is = null;
		try {
			UIResourceFile urf = new UIResourceFile(fileName);
			if (!urf.exists()) {
				return null;
			}
			is = urf.toStream();
			return new String(FileUtil.readByte(is), Config.getGlobalCharset());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 是否重新加载
	 */
	private static boolean needReload(TemplateExecutor je, String fileName) throws FileNotFoundException {
		if (je.isFromJar()) {
			return false;
		}
		if (System.currentTimeMillis() - je.getLastCheckTime() > 3000) {
			File f = new File(FileUtil.normalizePath(Config.getContextRealPath() + "/" + fileName));
			if (!f.exists()) {
				map.remove(fileName);
				throw new FileNotFoundException("File not found:" + fileName);
			}
			if (f.lastModified() != je.getLastModified()) {
				return true;
			}
			for (String includeFile : je.getIncludeFiles().keySet()) {
				f = new File(includeFile);
				if (!f.exists()) {
					LogUtil.warn("Include file in " + fileName + " not found:" + includeFile);
					return true;
				} else {
					if (je.getIncludeFiles().get(includeFile) != f.lastModified()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public TemplateExecutor getExecutor(String fileName) throws TemplateCompileException {
		try {
			TemplateExecutor je = map.get(fileName);
			if (je == null) {
				je = compile(fileName);
				map.put(fileName, je);
			} else if (needReload(je, fileName)) {
				je = compile(fileName);
				je.setLastCheckTime(System.currentTimeMillis());
				map.put(fileName, je);
			}
			return je;
		} catch (FileNotFoundException e) {
			throw new TemplateNotFoundException(e.getMessage());
		}
	}

	private static TemplateExecutor compile(String fileName) throws TemplateCompileException, FileNotFoundException {
		TemplateCompiler jc = new TemplateCompiler(ZhtmlManagerContext.getInstance());
		UIResourceFile u = new UIResourceFile(fileName);
		if (!u.exists()) {
			throw new TemplateNotFoundException("Template not found: " + fileName);
		}
		String source = u.readText();
		source = ZhtmlWeaveService.weave(fileName, source);// 处理页面织入
		jc.setFileName(fileName);
		jc.compileSource(source);
		TemplateExecutor je = jc.getExecutor();

		if (u.isFromJar()) {
			je.setFromJar(true);
		}
		return je;
	}
}
