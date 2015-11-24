package com.zving.framework.core.scanner;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import com.zving.framework.Config;
import com.zving.framework.collection.Mapx;
import com.zving.framework.config.ExcludeClassScan;
import com.zving.framework.extend.plugin.PluginConfig;
import com.zving.framework.extend.plugin.PluginManager;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.thirdparty.asm.ClassReader;
import com.zving.framework.thirdparty.asm.tree.ClassNode;
import com.zving.framework.thirdparty.asm.tree.InnerClassNode;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.framework.utility.ZipUtil;

/**
 * 编译后资源扫描器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-1-6
 */
public class BuiltResourceScanner {
	private long lastTime;
	private IBuiltResourceVisitor visitor;
	private String WEBINFPath;

	public BuiltResourceScanner(IBuiltResourceVisitor visitor, String WEBINFPath) {
		this.visitor = visitor;
		this.WEBINFPath = WEBINFPath;
	}

	/**
	 * 扫描指定路径下的编译后资源
	 * 
	 * @param visitors 编译后资源遍历器列表
	 * @param WEBINFPath WEB-INF路径
	 */
	public void scan(long lastTime) {
		this.lastTime = lastTime;
		long t = System.currentTimeMillis();
		PluginManager pm = new PluginManager();
		String pluginPath = Config.getPluginPath();
		if (WEBINFPath == null) {
			pm = PluginManager.getInstance();
		} else {
			pm.init(WEBINFPath);
			pluginPath = WEBINFPath + "/plugins/";
		}
		List<PluginConfig> list = pm.getAllPluginConfig();
		for (PluginConfig pc : list) {// 此处得到的列表是按依赖关系排序之后的
			scanJar(new File(pluginPath + "lib/" + pc.getID() + ".plugin.jar"));// jar中的文件优先级最低
			for (String path : pc.getPluginFiles()) {
				if (path.startsWith("[D]")) {
					path = path.substring(3);
					if (path.startsWith("JAVA")) {
						path = pluginPath + "classes/" + path.substring(5);
						path = FileUtil.normalizePath(path);
						try {
							scanOneDir(new File(path), pluginPath + "classes/");
						} catch (Exception e) {
							e.printStackTrace();
							LogUtil.error("Load class directory failed:" + e.getMessage());
						}
					}
				} else {
					if (!path.endsWith(".java")) {
						continue;
					}
					path = path.substring(5);
					path = path.substring(0, path.lastIndexOf("."));
					path = pluginPath + "classes/" + path + ".class";
					File f = new File(path);
					if (!f.exists() || f.lastModified() < lastTime) {
						continue;
					}
					try {
						BuiltResource br = new BuiltResource(path, null);
						scanOneResource(br);
					} catch (Exception e) {
						e.printStackTrace();
						LogUtil.error("Load single class failed:" + path);
					}
				}
			}
		}
		if (lastTime == 0) {
			LogUtil.info("----" + Config.getAppCode() + "(" + LangUtil.get(Config.getAppName()) + "): Scan class and resource used "
					+ (System.currentTimeMillis() - t) + " ms----");
		}
	}

	private void scanJar(File f) {
		try {
			if (!f.exists() || f.lastModified() < lastTime) {
				return;
			}
			Mapx<String, Long> files = ZipUtil.getFileListInZip(f.getAbsolutePath());
			for (String entryName : files.keySet()) {
				if (entryName.indexOf("$") > 0) {
					continue;
				}
				BuiltResource br = new BuiltResource(f.getAbsolutePath(), entryName);
				scanOneResource(br);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void scanOneDir(File p, String prefix) throws Exception {
		if (!p.exists()) {
			return;
		}
		String path = FileUtil.normalizePath(p.getAbsolutePath());
		if (!path.endsWith("/")) {
			path += "/";
		}
		path = path.substring(prefix.length());
		String exclude = ExcludeClassScan.getValue();
		if (ObjectUtil.notEmpty(exclude)) {
			for (String str : StringUtil.splitEx(exclude, ",")) {
				if (ObjectUtil.notEmpty(str) && path.startsWith(str)) {
					return;
				}
			}
		}
		File[] fs = p.listFiles();
		if (fs == null) {
			return;// linux下可能会有问题
		}
		for (File f : fs) {
			if (f.isFile()) {
				if (f.lastModified() > lastTime) {
					BuiltResource br = new BuiltResource(f.getAbsolutePath(), null);
					if (br.getFileName().indexOf("$") > 0) {
						continue;
					}
					scanOneResource(br);
				}
			} else {
				scanOneDir(f, prefix);
			}
		}
	}

	private void scanOneResource(BuiltResource br) throws Exception {
		if (br.getFullName().indexOf("com/zving/framework/") >= 0) {
			return;
		}
		ClassNode cn = null;
		if (visitor.match(br)) {
			if (cn == null) {
				if (br.isClass()) {
					InputStream is = br.getInputStream();
					try {
						ClassReader cr = new ClassReader(is);
						cn = new ClassNode();
						cr.accept(cn, 0);
					} finally {
						is.close();
					}
					visitor.visitClass(br, cn);
					for (InnerClassNode icn : cn.innerClasses) {
						if (icn.outerName == null || !icn.name.startsWith(cn.name)) {
							continue;
						}
						InputStream iis = br.getInnerClassInputStream(icn.name);
						try {
							ClassReader cr = new ClassReader(iis);
							ClassNode cn2 = new ClassNode();
							cr.accept(cn2, 0);
							visitor.visitInnerClass(br, cn, cn2);
						} finally {
							iis.close();
						}
					}
				} else {
					visitor.visitResource(br);
				}
			}
		}
	}
}
