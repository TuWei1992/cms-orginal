package com.zving.framework.extend;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.zving.framework.Config;
import com.zving.framework.SessionListener;
import com.zving.framework.collection.CacheMapx;
import com.zving.framework.extend.action.AfterAllPluginStartedAction;
import com.zving.framework.extend.exception.CreateExtendActionInstanceException;
import com.zving.framework.extend.menu.MenuManager;
import com.zving.framework.extend.plugin.IPlugin;
import com.zving.framework.extend.plugin.PluginConfig;
import com.zving.framework.extend.plugin.PluginException;
import com.zving.framework.extend.plugin.PluginManager;
import com.zving.framework.schedule.CronManager;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.PropertiesUtil;

/**
 * 扩展管理器
 * 
 * @date 2009-11-7 <br>
 * @author 王育春 <br>
 * @email wangyc@zving.com <br>
 */
public class ExtendManager {
	private Map<String, ArrayList<ExtendActionConfig>> extendActionMap;
	private Map<String, ArrayList<ExtendItemConfig>> extendItemMap;
	private Map<String, ExtendPointConfig> extendPointMap;
	private Map<String, ExtendServiceConfig> extendServiceMap;
	private Map<String, ExtendServiceConfig> extendServiceClassMap;
	private static ExtendManager instance = new ExtendManager();
	private ReentrantLock lock = new ReentrantLock();

	public static ExtendManager getInstance() {
		return instance;
	}

	/**
	 * 加载插件配置文件，初始化相关扩展数据。
	 */
	public void start() {
		if (extendActionMap == null) {
			lock.lock();
			try {
				if (extendActionMap == null) {
					extendActionMap = new CacheMapx<String, ArrayList<ExtendActionConfig>>();
					extendItemMap = new CacheMapx<String, ArrayList<ExtendItemConfig>>();
					extendPointMap = new CacheMapx<String, ExtendPointConfig>();
					extendServiceMap = new CacheMapx<String, ExtendServiceConfig>();
					extendServiceClassMap = new CacheMapx<String, ExtendServiceConfig>();

					// 先读取所有插件信息
					long t = System.currentTimeMillis();

					PluginManager.getInstance().init(Config.getWEBINFPath());
					List<IPlugin> list = new ArrayList<IPlugin>();

					List<PluginConfig> configList = PluginManager.getInstance().getAllPluginConfig();
					for (PluginConfig pc : configList) {
						if (!pc.isEnabled() || pc.isRunning()) {
							continue;
						}
						initPlugin(pc, list);
					}
					// 所有扩展信息读取完成后再逐个启动
					for (IPlugin plugin : list) {
						try {
							plugin.start();
						} catch (PluginException e) {
							e.printStackTrace();
						}
					}
					LogUtil.info("All plugins started,cost " + (System.currentTimeMillis() - t) + " ms");
				}
			} finally {
				lock.unlock();
				ExtendManager.invoke(AfterAllPluginStartedAction.ExtendPointID, new Object[] {});
			}
		}
	}

	/**
	 * 初始插件中的配置信息
	 */
	private void initPlugin(PluginConfig pc, List<IPlugin> list) {
		if (pc == null || pc.isRunning()) {
			return;
		}
		if (ObjectUtil.notEmpty(pc.getClassName())) {
			try {
				LogUtil.debug("Loading plugin:" + pc.getID());
				pc.setRunning(true);// 需要先设置，以免无限递归
				pc.setEnabled(true);
				for (String id : pc.getRequiredPlugins().keySet()) {
					initPlugin(PluginManager.getInstance().getPluginConfig(id), list);
				}
				Class<?> c = Class.forName(pc.getClassName());
				if (!IPlugin.class.isAssignableFrom(c)) {
					LogUtil.error("Plugin class '" + pc.getClassName() + "' isn't inherit from IPlugin");
					return;
				}
				readExtendInfo(pc);
				IPlugin plugin = (IPlugin) c.newInstance();
				list.add(plugin);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void readExtendInfo(PluginConfig pc) {
		// 加入扩展点
		extendPointMap.putAll(pc.getExtendPoints());

		// 加入扩展服务
		for (ExtendServiceConfig es : pc.getExtendServices().values()) {
			extendServiceMap.put(es.getID(), es);
			extendServiceClassMap.put(es.getClassName(), es);
		}

		// 加入扩展行为
		Collection<ExtendActionConfig> actions = pc.getExtendActions().values();
		for (ExtendActionConfig action : actions) {
			// LogUtil.debug("\tLoading extendAction:" + action.getID());
			if (!extendPointMap.containsKey(action.getExtendPointID())) {
				LogUtil.error("ExtendAction " + action.getID() + "'s ExtendPoint not found");
				continue;
			}
			ArrayList<ExtendActionConfig> list = extendActionMap.get(action.getExtendPointID());
			if (list == null) {
				list = new ArrayList<ExtendActionConfig>();
				extendActionMap.put(action.getExtendPointID(), list);
			}
			list.add(action);
		}

		// 加入扩展项
		Collection<ExtendItemConfig> items = pc.getExtendItems().values();
		for (ExtendItemConfig item : items) {
			// LogUtil.debug("\tLoading extendItem:" + item.getID());
			if (!extendServiceMap.containsKey(item.getExtendServiceID())) {
				LogUtil.error("ExtendItem " + item.getID() + "'s ExtendService not found");
				continue;
			}

			ArrayList<ExtendItemConfig> list = extendItemMap.get(item.getExtendServiceID());
			if (list == null) {
				list = new ArrayList<ExtendItemConfig>();
				extendItemMap.put(item.getExtendServiceID(), list);
			}
			list.add(item);
		}
	}

	/**
	 * 启用插件
	 */
	public void startPlugin(PluginConfig pc) throws PluginException {
		if (pc == null || pc.isRunning()) {
			return;
		}
		if (ObjectUtil.notEmpty(pc.getClassName())) {
			try {
				LogUtil.debug("Starting plugin:" + pc.getID());
				pc.setRunning(true);// 需要先设置，以免无限递归
				pc.setEnabled(true);
				for (String id : pc.getRequiredPlugins().keySet()) {
					startPlugin(PluginManager.getInstance().getPluginConfig(id));
				}
				Class<?> c = Class.forName(pc.getClassName());
				if (!IPlugin.class.isAssignableFrom(c)) {
					LogUtil.error("Plugin class '" + pc.getClassName() + "' isn't inherit from IPlugin");
					return;
				}
				readExtendInfo(pc);
				IPlugin plugin = (IPlugin) c.newInstance();
				plugin.start();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 查找被指定插件依赖的插件列表
	 */
	public List<PluginConfig> getRequiredPlugins(PluginConfig pc) throws PluginException {
		ArrayList<PluginConfig> list = new ArrayList<PluginConfig>();
		for (PluginConfig pc2 : PluginManager.getInstance().getAllPluginConfig()) {
			if (!pc2.getID().equals(pc.getID()) && pc2.getRequiredPlugins().containsKey(pc.getID())) {
				list.add(pc2);
			}
		}
		return list;
	}

	/**
	 * 停止插件
	 */
	public void stopPlugin(PluginConfig pc) throws PluginException {
		if (!pc.isEnabled() || !pc.isRunning()) {
			return;
		}
		if (ObjectUtil.notEmpty(pc.getClassName())) {
			try {
				Class<?> c = Class.forName(pc.getClassName());
				if (!IPlugin.class.isAssignableFrom(c)) {
					throw new PluginException("Plugin class '" + pc.getClassName() + "' isn't inherit from IPlugin");
				}
				IPlugin plugin = (IPlugin) c.newInstance();
				plugin.stop();

				for (PluginConfig pc2 : getRequiredPlugins(pc)) {
					stopPlugin(pc2);
				}
				pc.setRunning(false);
				pc.setEnabled(false);

				for (ExtendActionConfig ea : pc.getExtendActions().values()) {
					extendActionMap.get(ea.getExtendPointID()).remove(ea);
				}

				// 去掉扩展点和扩展行为
				for (String id : pc.getExtendPoints().keySet()) {
					extendPointMap.remove(id);
				}
				// 移除相应的扩展项(必须在移除扩展服务之前，因为本插件可以自己注册自己的扩展服务的扩展项)
				for (ExtendItemConfig ei : pc.getExtendItems().values()) {
					ExtendServiceConfig es = extendServiceMap.get(ei.getExtendServiceID());
					if (es != null) {
						es.getInstance().remove(ei.getInstance().getExtendItemID());
					}
					extendItemMap.get(ei.getExtendServiceID()).remove(ei);
				}
				// 去掉扩展服务
				for (ExtendServiceConfig es : pc.getExtendServices().values()) {
					extendServiceMap.remove(es.getID());
					extendServiceClassMap.remove(es.getID());
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 停止掉所有的插件
	 */
	public void destory() {
		for (PluginConfig pc : PluginManager.getInstance().getAllPluginConfig()) {
			try {
				Class<?> c = Class.forName(pc.getClassName());
				if (!IPlugin.class.isAssignableFrom(c)) {
					throw new PluginException("Plugin class '" + pc.getClassName() + "' isn't inherit from IPlugin");
				}
				IPlugin plugin = (IPlugin) c.newInstance();
				plugin.destory();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (extendServiceMap != null) {
			for (ExtendServiceConfig es : extendServiceMap.values()) {
				es.destory();
			}
			extendServiceMap.clear();
			extendServiceMap = null;
		}
		if (extendActionMap != null) {
			extendActionMap.clear();
			extendActionMap = null;
		}
		if (extendItemMap != null) {
			extendItemMap.clear();
			extendItemMap = null;
		}
		if (extendPointMap != null) {
			extendPointMap.clear();
			extendPointMap = null;
		}
		if (extendServiceClassMap != null) {
			extendServiceClassMap.clear();
			extendServiceClassMap = null;
		}
		PluginManager.getInstance().destory();
	}

	/**
	 * 指定的扩展点下是否有扩展行为。
	 */
	public boolean hasAction(String targetPoint) {
		start();
		return extendActionMap.get(targetPoint) != null;
	}

	/**
	 * 查找扩展了指定扩展点的扩展行为列表
	 */
	public ArrayList<ExtendActionConfig> findActionsByPointID(String extendPointID) {
		start();
		return extendActionMap.get(extendPointID);
	}

	/**
	 * 查找注册到指定扩展服务的扩展项列表
	 */
	public ArrayList<ExtendItemConfig> findItemsByServiceID(String extendServiceID) {
		start();
		return extendItemMap.get(extendServiceID);
	}

	/**
	 * 根据扩展点类名查找扩展点描述
	 */
	public ExtendPointConfig findExtendPoint(String extendPointID) {
		start();
		return extendPointMap.get(extendPointID);
	}

	/**
	 * 根据扩展服务ID查找扩展服务描述
	 */
	public ExtendServiceConfig findExtendService(String extendServiceID) {// NO_UCD
		start();
		return extendServiceMap.get(extendServiceID);
	}

	/**
	 * 根据扩展服务类名查找扩展服务描述
	 * 
	 * @param className 扩展服务类名
	 * @return 扩展服务描述类
	 */
	public ExtendServiceConfig findExtendServiceByClass(String className) {
		start();
		return extendServiceClassMap.get(className);
	}

	/**
	 * 调用扩展点
	 */
	public static Object[] invoke(String extendPointID, Object[] args) {
		return instance.invokePoint(extendPointID, args);
	}

	public Object[] invokePoint(String extendPointID, Object[] args) {
		try {
			if (!Config.isPluginContext()) {
				return new Object[] {};
			}
			start();
			if (!extendPointMap.containsKey(extendPointID)) {
				LogUtil.warn("ExtendPoint is not found:" + extendPointID);
				return new Object[] {};
			}
			ArrayList<ExtendActionConfig> actions = findActionsByPointID(extendPointID);
			if (actions == null) {
				return null;
			}
			List<Object> r = new ArrayList<Object>();
			for (int i = 0; i < actions.size(); i++) {
				try {
					IExtendAction ea = actions.get(i).getInstance();
					if (!ea.isUsable()) {
						continue;
					}
					r.add(ea.execute(args));
				} catch (CreateExtendActionInstanceException e) {
					e.printStackTrace();// extend action实例创建失败后只是输出异常
					actions.remove(i);
					i--;
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			return r.toArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 启用插件
	 */
	public void enablePlugin(String pluginID) throws PluginException {
		startPlugin(PluginManager.getInstance().getPluginConfig(pluginID));
		setStatusValue(pluginID, "true");
		MenuManager.reloadMenus();
	}

	/**
	 * 停用插件
	 */
	public void disablePlugin(String pluginID) throws PluginException {
		stopPlugin(PluginManager.getInstance().getPluginConfig(pluginID));
		MenuManager.reloadMenus();
		setStatusValue(pluginID, "false");
	}

	/**
	 * 启用菜单
	 */
	public void enableMenu(String menuID) {
		setStatusValue("MENU." + menuID, "true");
	}

	/**
	 * 停用菜单
	 */
	public void disableMenu(String menuID) {
		setStatusValue("MENU." + menuID, "false");
	}

	/**
	 * 插件是否被启用
	 */
	public boolean isPluginEnable(String pluginID) {// NO_UCD
		return !"false".equals(PluginManager.getInstance().getStatusMap().get(pluginID));
	}

	/**
	 * 菜单是否被启用
	 */
	public boolean isMenuEnable(String menuID) {
		return !"false".equals(PluginManager.getInstance().getStatusMap().get("MENU." + menuID));
	}

	/**
	 * 将状态值写入文件
	 */
	private void setStatusValue(String key, String value) {
		File f = new File(Config.getPluginPath() + "classes/plugins/status.config");
		PluginManager.getInstance().getStatusMap().put(key, value);
		PropertiesUtil.write(f, PluginManager.getInstance().getStatusMap());
		if (FileUtil.exists(new File(Config.getContextRealPath()).getParentFile().getAbsolutePath() + "/JAVA")) {
			File ff = new File(new File(Config.getContextRealPath()).getParentFile().getAbsolutePath() + "/JAVA/plugins/status.config");
			PropertiesUtil.write(ff, PluginManager.getInstance().getStatusMap());
		}

	}

	/**
	 * 重新启动插件运行环境。<br>
	 * 本方法将先中止所有会话和定时任务。<br>
	 * 一般在系统安装、插件安装卸载时调用。
	 */
	public void restart() {// NO_UCD
		SessionListener.forceExit();// 现有会话强制退出
		Config.setAllowLogin(false);// 暂时不允许登录
		CronManager.getInstance().destory();// 定时任务强制退出

		extendActionMap = null;
		start();// 重新读入掉插件注册信息
		Config.setAllowLogin(true);
	}
}
