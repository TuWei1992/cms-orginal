package com.zving.framework;

import java.io.File;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.zving.framework.collection.Mapx;
import com.zving.framework.core.FrameworkException;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.framework.xml.XMLElement;
import com.zving.framework.xml.XMLMultiLoader;

/**
 * 全局配置文件加载器，会加载WEB-INF/plugins/classes下的所有xml文件中的配置信息
 * 
 * @Author 王育春
 * @Date 2010-9-1
 * @Mail wyuch@zving.com
 **/
public class ConfigLoader {
	private static boolean loaded = false;

	private static ReentrantLock lock = new ReentrantLock();

	private static XMLMultiLoader loader = new XMLMultiLoader();

	/**
	 * 载入配置文件
	 */
	public static void load() {
		try {
			load(Config.getPluginPath() + "classes/");
		} catch (Exception e) {
			throw new ConfigLoadException(e.getMessage());
		}
	}

	/**
	 * 载入指定路径下的所有xml文件
	 */
	public static void load(String path) {
		if (!loaded) {
			lock.lock();
			try {
				if (!loaded) {// 只启动时加载一次
					loader.clear();
					File f = new File(path);
					if (!f.exists()) {
						return;
					}
					String file = path + "charset.config";
					if (new File(file).exists()) {
						String txt = FileUtil.readText(file, "UTF-8");// 必须指定字符集，否则会导致Config.getGlobalCharset()死循环
						Mapx<String, String> map = StringUtil.splitToMapx(txt, "\n", "=");
						Config.globalCharset = "GBK".equalsIgnoreCase(map.getString("global")) ? "GBK" : "UTF-8";
					} else {
						throw new FrameworkException("File charset.config not found!");
					}

					loader.load(path);
					XMLElement data = loader.elements("framework.application.config", "name", "ComplexDeployMode");
					Config.isComplexDepolyMode = data != null && "true".equals(data.getText());
				}
				loaded = true;
			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * 重新载入配置文件
	 */
	public static void reload() {
		loaded = false;
		load();
	}

	/**
	 * @param path XML路径
	 * @return 指定XML路径下的所有XML元素
	 */
	public static List<XMLElement> getElements(String path) {
		return loader.elements(path);
	}
}
