package com.zving.framework.core.handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zving.framework.Config;
import com.zving.framework.collection.ConcurrentMapx;
import com.zving.framework.core.DispatchServlet;
import com.zving.framework.core.Dispatcher.HandleEndException;
import com.zving.framework.core.IURLHandler;
import com.zving.framework.ui.resource.UIResourceFile;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.ServletUtil;

/**
 * 资源文件处理者
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-5
 */
public class ResourceHandler implements IURLHandler {
	public static final String ID = "com.zving.framework.core.ResourceURLHandler";

	private static final String[] extensions = new String[] { ".gif", ".jpg", ".png", ".js", ".css", ".ico", ".swf", ".htm", ".html",
			".shtml", ".xml", ".txt", ".properties", ".jpeg", ".cur", ".htc" };

	/**
	 * 请使用DateUtil.Format_LastModified代替
	 */
	@Deprecated
	public static String LastModifiedFormat = DateUtil.Format_LastModified;

	private static ConcurrentMapx<String, CachedResource> map = new ConcurrentMapx<String, CachedResource>(2000);// 最多缓存2000个
	private static ConcurrentMapx<String, String> files = new ConcurrentMapx<String, String>();// 存放哪些是磁盘文件

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public boolean match(String url) {
		String ext = ServletUtil.getUrlExtension(url);
		if (ObjectUtil.empty(ext)) {
			return false;
		}
		if (ObjectUtil.in(ext, extensions)) {
			return true;
		}
		return false;
	}

	@Override
	public String getExtendItemName() {
		return "Resource URL Processor";
	}

	@Override
	public boolean handle(String url, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String fileName = ServletUtil.getFileName(url);
		if (files.containsKey(fileName)) {
			DispatchServlet.getInstance().forwardToDefaultServlet(request, response);
			throw new HandleEndException();// 避免文件检查
		}
		CachedResource r = map.get(fileName);
		if (r != null && System.currentTimeMillis() - r.LastCheck < 3000) {
			String since = request.getHeader("If-Modified-Since");
			if (ObjectUtil.notEmpty(since)) {
				Date d = DateUtil.parseLastModified(since);
				if (d != null && d.getTime() == r.LastModified) {
					response.setStatus(304);
					return true;// 直接从浏览器缓存中获取
				}
			}
			response.getOutputStream().write(r.Data);
			return true;
		}
		String fullFileName = Config.getContextRealPath() + fileName;
		File f = new File(fullFileName);
		if (f.exists()) {
			if (f.isFile()) {
				files.put(fileName, "Y");
				DispatchServlet.getInstance().forwardToDefaultServlet(request, response);
			}
			throw new HandleEndException();
		} else {
			UIResourceFile rf = new UIResourceFile(fileName);
			long lastModified = rf.lastModified();
			if (lastModified != 0) {
				String lastModifiedStr = DateUtil.getLastModifiedFormat().format(new Date(lastModified));
				response.setHeader("Last-Modified", lastModifiedStr);
				String since = request.getHeader("If-Modified-Since");
				if (ObjectUtil.notEmpty(since)) {
					try {
						Date d = DateUtil.parseLastModified(since);
						if (d != null && d.getTime() == lastModified) {
							response.setStatus(304);
							response.getOutputStream().write(0);
							response.flushBuffer();
							if (r != null) {
								r.LastCheck = System.currentTimeMillis();
							}
							return true;// 直接从浏览器缓存中获取
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				String ext = ServletUtil.getUrlExtension(url);
				if (ObjectUtil.in(ext, ".shtml", ".html", "htm")) {
					response.setHeader("Content-Type", "text/html");
					write(response, rf);
				} else if (ObjectUtil.in(ext, ".gif", ".jpg", ".jpeg", ".png", "bmp")) {
					response.setHeader("Content-Type", "image/" + ext.substring(1));
					write(response, rf);
				} else if (ObjectUtil.in(ext, ".css")) {
					if (Config.getServletMajorVersion() == 2 && Config.getServletMinorVersion() == 3) {
						response.setContentType("text/css;charset=" + Config.getGlobalCharset());
					} else {
						response.setHeader("Content-Type", "text/css");
						response.setCharacterEncoding(Config.getGlobalCharset());
					}
					write(response, rf);
				} else if (ObjectUtil.in(ext, ".js", ".txt")) {
					String contentType = ext.equals(".js") ? "application/x-javascript" : "text/plain";
					if (Config.getServletMajorVersion() == 2 && Config.getServletMinorVersion() == 3) {
						response.setContentType(contentType + ";charset=" + Config.getGlobalCharset());
					} else {
						response.setHeader("Content-Type", contentType);
						response.setCharacterEncoding(Config.getGlobalCharset());
					}
					write(response, rf);
				} else if (ObjectUtil.in(ext, ".swf", ".swc")) {
					response.setHeader("Content-Type", "application/x-shockwave-flash");
					write(response, rf);
				} else {
					write(response, rf);
				}
				response.flushBuffer();
				if (rf.length() <= 512000) {
					r = new CachedResource();
					r.LastCheck = System.currentTimeMillis();
					r.LastModified = rf.lastModified();
					r.Data = rf.readByte();
					map.put(fileName, r);
				}
			} else {
				return false;
			}
		}
		return true;
	}

	public static void write(HttpServletResponse response, UIResourceFile rf) throws IOException {
		InputStream is = rf.toStream();
		if (is != null) {
			try {
				response.setBufferSize(102400);
				OutputStream os = response.getOutputStream();
				int len = 0;
				byte[] bs = new byte[1024 * 100];
				while ((len = is.read(bs)) != -1) {
					os.write(bs, 0, len);
				}
			} finally {
				is.close();
			}
		}
	}

	public static class CachedResource {
		public long LastCheck;// 最后检查时间
		public long LastModified;// 资源的最后修改时间
		public byte[] Data;// 如果不是文件，则缓存二进制数据
	}

	@Override
	public void init() {
	}

	@Override
	public void destroy() {
	}

	@Override
	public int getOrder() {
		return 9999;
	}
}
