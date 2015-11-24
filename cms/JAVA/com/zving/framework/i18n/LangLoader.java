package com.zving.framework.i18n;

import java.io.ByteArrayInputStream;
import java.io.File;

import com.zving.framework.Config;
import com.zving.framework.collection.CacheMapx;
import com.zving.framework.collection.ConcurrentMapx;
import com.zving.framework.collection.Mapx;
import com.zving.framework.config.DefaultLanguage;
import com.zving.framework.extend.plugin.PluginConfig;
import com.zving.framework.extend.plugin.PluginManager;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.PropertiesUtil;
import com.zving.framework.utility.ZipUtil;

/**
 * 语言文件加载器
 * 
 * @Author 王育春
 * @Date 2011-4-21
 * @Mail wyuch@zving.com
 */
public class LangLoader {

	public static LangMapping load() {
		String path = Config.getWEBINFPath();
		if (new File(path).exists()) {
			PluginManager.getInstance().init(path);
			LangMapping lm = loadMapping(path, PluginManager.getInstance());
			String lang = DefaultLanguage.getValue();
			if (ObjectUtil.notEmpty(lang)) {
				lm.defaultLanguage = lang;
			}
			return lm;
		} else {
			LangMapping lm = new LangMapping();
			lm.mapping = new ConcurrentMapx<String, ConcurrentMapx<String, String>>();
 			return lm;
		}
	}

	/**
	 * 读取指定应用下的国际化资源文件,path参数应该一个UI/WEB-INF/目录
	 */
	public static LangMapping loadMapping(String path, PluginManager pm) {
		ConcurrentMapx<String, ConcurrentMapx<String, String>> mapping = new ConcurrentMapx<String, ConcurrentMapx<String, String>>();
		LangMapping lm = new LangMapping();
		lm.mapping = mapping;

		File langFile = new File(path + "/plugins/classes/lang/lang.i18n");
		if (langFile.exists()) {
			Mapx<String, String> map = PropertiesUtil.read(langFile);
			lm.languageMap = new CacheMapx<String, String>();
			lm.languageMap.putAll(map);
		} else {
			lm.languageMap = new CacheMapx<String, String>();
			lm.languageMap.put("zh-cn", "中文(简体)");
		}
		for (PluginConfig pc : pm.getAllPluginConfig()) {// 此处得到的列表是按依赖关系排序之后的
			loadFromJar(new File(path + "/plugins/lib/" + pc.getID() + ".plugin.jar"), lm);
			loadFromClasses(new File(path + "/plugins/classes/lang/" + pc.getID() + "/"), lm);
		}
		return lm;
	}

	private static void loadFromJar(File f, LangMapping lm) {
		if (!f.exists()) {
			return;
		}
		if (f.getName().endsWith(".plugin.jar")) {
			try {
				Mapx<String, Long> files = ZipUtil.getFileListInZip(f.getAbsolutePath());
				for (String fileName : files.keySet()) {
					if (fileName.endsWith(".i18n")) {
						byte[] bs = ZipUtil.readFileInZip(f.getAbsolutePath(), fileName);
						int start = fileName.indexOf("/") >= 0 ? fileName.lastIndexOf("/") + 1 : 0;
						int end = fileName.lastIndexOf(".");
						String lang = fileName.substring(start, end);
						Mapx<String, String> map = PropertiesUtil.read(new ByteArrayInputStream(bs));
						if (fileName.endsWith("/lang.i18n")) {
							lm.languageMap = new CacheMapx<String, String>();
							lm.languageMap.putAll(map);
						} else {
							for (String key : map.keySet()) {
								lm.put(lang, key, map.get(key));
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void loadFromClasses(File f, LangMapping lm) {
		if (!f.exists()) {
			return;
		}
		try {
			for (File f2 : f.listFiles()) {
				if (f2.isFile() && f2.getName().toLowerCase().endsWith(".i18n")) {
					Mapx<String, String> map = PropertiesUtil.read(f2);
					for (String key : map.keySet()) {
						String lang = f2.getName().substring(0, f2.getName().lastIndexOf("."));
						lm.put(lang, key, map.get(key));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
