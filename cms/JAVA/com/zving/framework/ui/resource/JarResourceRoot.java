package com.zving.framework.ui.resource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarFile;

import com.zving.framework.Config;
import com.zving.framework.collection.CacheMapx;
import com.zving.framework.collection.Mapx;
import com.zving.framework.extend.plugin.PluginConfig;
import com.zving.framework.extend.plugin.PluginManager;
import com.zving.framework.utility.StringUtil;
import com.zving.framework.utility.ZipUtil;

/**
 * 所有Jar中的资源
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-10-15
 */
public class JarResourceRoot {
	private static CacheMapx<String, Object> all = null;
	private static ReentrantLock lock = new ReentrantLock();

	public static JarResourceEntry getFile(String fullFileName) {
		init();
		if (fullFileName.equals("/") || fullFileName.equals("")) {
			return new JarResourceEntry(null, 0, "", true, 0);
		}
		int index = fullFileName.lastIndexOf("/");
		if (index < 0) {
			Object obj = all.get(fullFileName);
			if (obj == null) {
				return null;
			}
			if (obj instanceof JarResourceEntry) {
				return (JarResourceEntry) obj;
			} else {
				return new JarResourceEntry(null, 0, fullFileName, true, 0);// 表示目录
			}
		}
		String path = fullFileName.substring(0, index);
		String fileName = fullFileName.substring(index + 1);
		String[] arr = StringUtil.splitEx(path, "/");
		CacheMapx<String, Object> current = all;
		for (String seg : arr) {
			if (StringUtil.isEmpty(seg)) {
				continue;
			}
			@SuppressWarnings("unchecked")
			CacheMapx<String, Object> map = (CacheMapx<String, Object>) current.get(seg);
			if (map == null) {
				return null;
			}
			current = map;
		}
		Object obj = current.get(fileName);
		if (obj == null) {
			return null;
		}
		if (obj instanceof JarResourceEntry) {
			return (JarResourceEntry) obj;
		} else {
			return new JarResourceEntry(null, 0, fullFileName, true, 0);// 表示目录
		}
	}

	public static List<JarResourceEntry> listFiles(String path) {
		init();
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		String[] arr = StringUtil.splitEx(path, "/");
		CacheMapx<String, Object> current = all;
		for (String element : arr) {
			Object obj = current.get(element);
			if (obj == null) {
				return null;
			}
			if (obj instanceof JarResourceEntry) {
				return null;
			} else {
				@SuppressWarnings("unchecked")
				CacheMapx<String, Object> map = (CacheMapx<String, Object>) current.get(element);
				current = map;
			}
		}
		ArrayList<JarResourceEntry> res = new ArrayList<JarResourceEntry>();
		for (Entry<String, Object> entry : current.entrySet()) {
			Object obj = entry.getValue();
			if (obj instanceof JarResourceEntry) {
				res.add((JarResourceEntry) obj);
			} else {
				res.add(new JarResourceEntry(null, 0, path + "/" + entry.getKey(), true, 0));// 表示目录
			}
		}
		return res;
	}

	private static void init() {
		if (all == null) {
			lock.lock();
			try {
				if (all == null) {
					all = new CacheMapx<String, Object>();
					String path = Config.getPluginPath() + "lib/";
					if (!new File(path).exists()) {
						return;
					}
					for (PluginConfig pc : PluginManager.getInstance().getAllPluginConfig()) {// 此处得到的列表是按依赖关系排序之后的
						loadFromJar(new File(path + pc.getID() + ".ui.jar"));
						loadFromJar(new File(path + pc.getID() + ".resource.jar"));
					}
				}
			} finally {
				lock.unlock();
			}
		}

	}

	private static void loadFromJar(File f) {
		if (!f.exists()) {
			return;
		}
		try {
			JarFile jar = new JarFile(f);
			Mapx<String, Long> files = ZipUtil.getFileListInZip(f.getAbsolutePath());
			for (Entry<String, Long> entry : files.entrySet()) {
				String k = entry.getKey();
				JarResourceEntry re = new JarResourceEntry(f.getAbsolutePath(), jar.getEntry(k).getTime(), k, false, entry.getValue());
				String[] arr = StringUtil.splitEx(k, "/");
				CacheMapx<String, Object> current = all;
				for (int i = 0; i < arr.length - 1; i++) {
					@SuppressWarnings("unchecked")
					CacheMapx<String, Object> map = (CacheMapx<String, Object>) current.get(arr[i]);
					if (map == null) {
						map = new CacheMapx<String, Object>();
						current.put(arr[i], map);
					}
					current = map;
				}
				current.put(arr[arr.length - 1], re);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
