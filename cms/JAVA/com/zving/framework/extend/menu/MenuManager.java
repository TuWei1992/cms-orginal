package com.zving.framework.extend.menu;

import java.io.File;
import java.util.concurrent.locks.ReentrantLock;

import com.zving.framework.Config;
import com.zving.framework.collection.CacheMapx;
import com.zving.framework.collection.Mapx;
import com.zving.framework.extend.plugin.PluginConfig;
import com.zving.framework.extend.plugin.PluginException;
import com.zving.framework.extend.plugin.PluginManager;
import com.zving.framework.json.JSON;
import com.zving.framework.json.JSONObject;
import com.zving.framework.utility.Errorx;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.ZipUtil;

/**
 * 菜单管理器
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-8-12
 */
public class MenuManager {
	private static CacheMapx<String, Menu> menus;
	private static long lastTime = 0;
	private static ReentrantLock lock = new ReentrantLock();

	static CacheMapx<String, Menu> load(String path) {
		CacheMapx<String, Menu> menus = new CacheMapx<String, Menu>();
		// 从jar中加载
		if (new File(path + "/lib/").exists()) {
			File[] fs = new File(path + "/lib/").listFiles();
			for (File f : fs) {
				if (f.getName().endsWith(".plugin.jar")) {
					try {
						Mapx<String, Long> files = ZipUtil.getFileListInZip(f.getAbsolutePath());
						for (String fileName : files.keySet()) {
							if (fileName.endsWith(".plugin")) {
								byte[] bs = ZipUtil.readFileInZip(f.getAbsolutePath(), fileName);
								PluginConfig pluginConfig = new PluginConfig();
								pluginConfig.parse(new String(bs, "UTF-8"));
								// 只有已经启用了的才重新载入菜单
								PluginConfig pc = PluginManager.getInstance().getPluginConfig(pluginConfig.getID());
								if (pc == null || pc.isEnabled()) {// 新插件或者未被停用的旧插件
									for (Menu m : pluginConfig.getMenus().values()) {
										menus.put(m.getID(), m);
									}
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}
		}
		// 加载classes下的plugin文件
		String dir = Config.getContextRealPath() + "WEB-INF/plugins/classes/plugins/";
		File p = new File(dir);
		if (p.exists()) {
			for (File f : p.listFiles()) {
				if (!f.getName().endsWith(".plugin")) {
					continue;
				}
				PluginConfig pluginConfig = new PluginConfig();
				String xml = FileUtil.readText(f, "UTF-8");
				try {
					pluginConfig.parse(xml);
					// 只有已经启用了的才重新载入菜单
					PluginConfig pc = PluginManager.getInstance().getPluginConfig(pluginConfig.getID());
					if (pc == null || pc.isEnabled()) {// 新插件或者未被停用的旧插件
						for (Menu m : pluginConfig.getMenus().values()) {
							menus.put(m.getID(), m);
						}
					}
				} catch (PluginException e) {
					e.printStackTrace();
				}
			}
		}
		// 读取目录配置文件
		String menuCfg = Config.getContextRealPath() + "WEB-INF/plugins/classes/plugins/menu.config";
		if (FileUtil.exists(menuCfg)) {
			String json = FileUtil.readText(menuCfg);
			@SuppressWarnings("unchecked")
			Mapx<String, JSONObject> menux = (Mapx<String, JSONObject>) JSON.parse(json);
			for (String mid : menux.keySet()) {
				JSONObject jo = menux.get(mid);
				PluginConfig pc = PluginManager.getInstance().getPluginConfig(menux.getString("PluginID"));
				if (pc == null || pc.isEnabled()) {
					Menu m = menus.get(mid);
					if (ObjectUtil.empty(m)) {
						m = new Menu();
						m.setID(mid);
						menus.put(mid, m);
					}
					m.setParentID(jo.getString("PID"));
					m.setOrder(jo.getString("Order"));
					m.setIcon(jo.getString("Icon"));
					m.setDescription(jo.getString("Memo"));
					m.setName(jo.getString("Name"));
					m.setType(jo.getString("Type"));
				} else {
					Menu m = menus.get(mid);
					m.setName(jo.getString("Name"));
					m.setParentID(jo.getString("PID"));
					m.setOrder(jo.getString("Order"));

				}
			}
		}
		return menus;
	}

	public static Mapx<String, Menu> getMenus() {
		if (menus == null) {
			lock.lock();
			try {
				if (menus == null) {
					String path = Config.getContextRealPath() + "WEB-INF/plugins";
					menus = load(path);
					lastTime = System.currentTimeMillis();
				}
			} finally {
				lock.unlock();
			}
		} else if (Config.isDebugMode() && System.currentTimeMillis() - lastTime > 5000) {// 需要定时刷新菜单,主要是为了开发用
			lock.lock();
			try {
				if (System.currentTimeMillis() - lastTime > 5000) {
					// 首先读取jar文件中的资源文件
					String path = Config.getContextRealPath() + "WEB-INF/plugins";
					menus = load(path);
					lastTime = System.currentTimeMillis();
				}
			} finally {
				lock.unlock();
			}
		}
		return menus;
	}

	public static void reloadMenus() {
		String path = Config.getContextRealPath() + "WEB-INF/plugins";
		menus = load(path);
		lastTime = System.currentTimeMillis();
	}

	public static Menu getMenu(String id) {
		return menus.get(id);
	}

	public static boolean addMenu(Menu m) {
		if (ObjectUtil.notEmpty(menus.get(m.getID()))) {
			Errorx.addError("menuID is existed!");
			return false;
		}
		menus.put(m.getID(), m);
		writeMenuConfigFile();
		return true;
	}

	private static void writeMenuConfigFile() {
		Mapx<String, JSONObject> out = new Mapx<String, JSONObject>();
		for (Menu e : menus.values()) {
			JSONObject jo = new JSONObject();
			jo.put("PID", e.getParentID());
			jo.put("Name", e.getName());
			jo.put("Order", e.getOrder());
			jo.put("Icon", e.getIcon());
			jo.put("Memo", e.getDescription());
			jo.put("Type", e.getType());
			jo.put("PluginID", e.getParentID());
			out.put(e.getID(), jo);
		}
		String json = JSON.toJSONString(out);
		String menuCfg = Config.getContextRealPath() + "WEB-INF/plugins/classes/plugins/menu.config";
		if (!FileUtil.exists(menuCfg.substring(0, menuCfg.lastIndexOf("/")))) {
			FileUtil.mkdir(menuCfg.substring(0, menuCfg.lastIndexOf("/")));
		}
		FileUtil.writeText(menuCfg, json);
		if (FileUtil.exists(new File(Config.getContextRealPath()).getParentFile().getAbsolutePath() + "/JAVA")) {
			FileUtil.writeText(new File(Config.getContextRealPath()).getParentFile().getAbsolutePath() + "/JAVA/plugins/menu.config", json);
		}
	}

	public static boolean editMenu(Menu m) {
		if (ObjectUtil.empty(menus.get(m.getID()))) {
			Errorx.addError("menuID is not existed!");
			return false;
		}
		menus.put(m.getID(), m);
		writeMenuConfigFile();
		return true;
	}

	public static boolean deleteMenu(Menu m) {
		if (ObjectUtil.empty(menus.get(m.getID()))) {
			Errorx.addError("menuID is not existed!");
			return false;
		}
		menus.remove(m.getID());
		writeMenuConfigFile();
		return true;
	}
}
