package com.zving.framework.extend.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.zving.framework.Config;
import com.zving.framework.collection.ConcurrentMapx;
import com.zving.framework.collection.Mapx;
import com.zving.framework.collection.ReadOnlyList;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.PropertiesUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.framework.utility.ZipUtil;

/**
 * 插件管理器
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-7-8
 */
public class PluginManager {
	private ArrayList<PluginConfig> configList = null;
	private ConcurrentMapx<String, String> statusMap = new ConcurrentMapx<String, String>();
	private ReentrantLock lock = new ReentrantLock();
	private static PluginManager instance = new PluginManager();

	public static PluginManager getInstance() {
		return instance;
	}

	public void init(String path) {
		if (configList == null || configList.size() == 0) {
			lock.lock();
			try {
				if (configList == null || configList.size() == 0) {
					loadAllConfig(path);
				}
			} finally {
				lock.unlock();
			}
		}
	}

	public void destory() {
		statusMap.clear();
		configList = null;
		statusMap = null;
	}

	public Map<String, String> getStatusMap() {
		return statusMap;
	}

	/**
	 * 读取指定应用下的所有插件配置文件,参数path应该是一个UI/WEB-INF目录
	 */
	private void loadAllConfig(String path) {
		configList = new ArrayList<PluginConfig>();
		Mapx<String, String> map = new Mapx<String, String>();
		statusMap.clear();

		// 读取插件配置
		File statusFile = new File(path + "/plugins/classes/plugins/status.config");
		if (statusFile.exists()) {
			statusMap.putAll(PropertiesUtil.read(statusFile));
		}

		// 首先需要读取自己的.plugin
		InputStream is = PluginManager.class.getResourceAsStream("/plugins/com.zving.framework.plugin");
		try {
			if (is != null) {
				byte[] bs = FileUtil.readByte(is);
				PluginConfig pc = new PluginConfig();
				pc.parse(new String(bs, "UTF-8"));
				if (!map.containsKey(pc.getID())) {
					configList.add(pc);
				}
				//map.put(pc.getID(), "");
			}
		} catch (Exception e) {
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

		if (!Config.isPluginContext()) {
			return;// 如果不是插件环境，则只返回Framework本身的配置信息
		}

		// 首先读取jar文件中的资源文件
		loadFromLib(path + "/lib", map);
		loadFromLib(path + "/plugins/lib", map);

		// 读取应用下的插件配置文件
		loadFromClasses(path + "/classes/plugins", map);
		loadFromClasses(path + "/plugins/classes/plugins", map);

		for (PluginConfig pc : configList) {
			if ("false".equals(statusMap.get(pc.getID()))) {
				pc.setEnabled(false);// 如果配置中停用，则置为false
			}
		}
		computeRela();// 计算插件依赖关系

		ArrayList<PluginConfig> result = new ArrayList<PluginConfig>();
		ArrayList<PluginConfig> tmp = new ArrayList<PluginConfig>();
		tmp.addAll(configList);
		for (PluginConfig pc : tmp) {
			sort(result, pc);
		}
		configList = new ReadOnlyList<PluginConfig>(result);
	}

	private void loadFromLib(String path, Mapx<String, String> map) {
		if (new File(path).exists()) {
			File[] fs = new File(path).listFiles();
			for (File f : fs) {
				if (f.getName().endsWith(".plugin.jar")) {
					try {
						Mapx<String, Long> files = ZipUtil.getFileListInZip(f.getAbsolutePath());
						for (String fileName : files.keySet()) {
							if (fileName.endsWith(".plugin")) {
								byte[] bs = ZipUtil.readFileInZip(f.getAbsolutePath(), fileName);
								PluginConfig pc = new PluginConfig();
								pc.parse(new String(bs, "UTF-8"));
								if (!map.containsKey(pc.getID())) {
									configList.add(pc);
								} else {
									LogUtil.warn("PluginConfig is duplication:" + map.get(pc.getID()) + " & " + f.getAbsolutePath() + "!"
											+ fileName);
								}
								map.put(pc.getID(), f.getAbsolutePath() + "!" + fileName);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}
		}
	}

	private void loadFromClasses(String path, Mapx<String, String> map) {
		File langDir = new File(path);
		if (langDir.exists()) {
			try {
				File[] fs = langDir.listFiles();// 按插件分目录
				for (File f : fs) {
					if (f.isFile() && f.getName().toLowerCase().endsWith(".plugin")) {
						PluginConfig pc = new PluginConfig();
						pc.parse(FileUtil.readText(f, "UTF-8"));
						if (!map.containsKey(pc.getID())) {
							configList.add(pc);
							map.put(pc.getID(), f.getAbsolutePath());
						} else {
							// 用classes下的配置文件优先于jar中的
							int i = 0;
							for (PluginConfig pc2 : configList) {
								if (pc2.getID().equals(pc.getID())) {
									configList.set(i, pc);
									break;
								}
								i++;
							}
							if (!pc.getID().equals(FrameworkPlugin.ID)) {// Framework很可能会重复
								LogUtil.warn("PluginConfig is duplication:" + map.get(pc.getID()) + " & " + f.getAbsolutePath());
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 逆序排列
	 * 
	 * @param result
	 * @param configList
	 * @param pc
	 */
	private void sort(List<PluginConfig> result, PluginConfig pc) {
		if (getPluginConfig(result, pc.getID()) != null) {
			return;
		}
		configList.remove(pc);// 避免出现死循环
		for (String pluginID : pc.getRequiredPlugins().keySet()) {
			PluginConfig c = getPluginConfig(configList, pluginID);
			if (c == null) {
				continue;
			}
			sort(result, c);
		}
		if (getPluginConfig(result, pc.getID()) == null) {
			result.add(pc);
		}
	}

	private void computeRela() {
		for (PluginConfig pc : configList) {
			if (!pc.isEnabled()) {
				continue;// 已经被停用，不需要计算
			}

			// 如果依赖的插件不存在，则置为false
			boolean requiredFlag = true;// 默认满足
			for (String pluginID : pc.getRequiredPlugins().keySet()) {
				PluginConfig c = getPluginConfig(configList, pluginID);
				if (c == null || !c.isEnabled()) {
					if (c == null) {
						LogUtil.error("Plugin " + pluginID + " needed by " + pc.getID() + " is not found!");
					}
					requiredFlag = false;
					break;
				}
				// 目标插件的版本是否符合要求
				String v = c.getVersion();
				String need = pc.getRequiredPlugins().get(pluginID);
				if (!isVersionCompatible(need, v)) {
					LogUtil.error("Plugin " + pluginID + "'s version is " + v + ", but " + need + " is needed by " + pc.getID() + "!");
				}
			}
			if (!requiredFlag) {
				setDisable(pc);
				continue;// 接着继续下一个
			}

			// 如果依赖的扩展点不存在，则置为false
			requiredFlag = true;
			for (String extendPointID : pc.getRequiredExtendPoints().keySet()) {
				boolean flag = false;
				for (PluginConfig c : configList) {
					if (c.getExtendPoints().containsKey(extendPointID)) {
						flag = true;
						break;
					}
				}
				if (!flag) {
					LogUtil.error("ExtendPoint " + extendPointID + " needed by " + pc.getID() + " is not found!");
					requiredFlag = false;
					break;
				}
			}
			if (!requiredFlag) {
				setDisable(pc);
				continue;// 接着继续下一个
			}
		}
	}

	/**
	 * 判断版本是否兼容
	 */
	private boolean isVersionCompatible(String need, String version) {
		if (need.indexOf("-") > 0) {
			String[] arr = StringUtil.splitEx(need, "-");
			if (arr.length != 2) {
				return false;
			}
			String start = arr[0];
			String end = arr[1];
			if (start.endsWith(".x")) {
				start = start.substring(0, start.length() - 2);
			}
			if (end.endsWith(".x")) {
				end = end.substring(0, end.length() - 2);
			}
			double s = Double.parseDouble(start);
			double e = Double.parseDouble(end);
			double v = Double.parseDouble(version);
			return s <= v && e >= v;
		} else {
			if (need.endsWith(".x")) {
				return version.startsWith(need.substring(0, need.length() - 1));
			} else {
				return getVersion(version) >= getVersion(need);
			}
		}
	}

	private double getVersion(String ver) {
		int i1 = ver.indexOf('.');
		if (i1 > 0) {
			int i2 = ver.indexOf('.', i1 + 1);
			if (i2 > 0) {
				ver = ver.substring(0, i2);
			}
		}
		try {
			return Double.parseDouble(ver);
		} catch (Exception e) {
			LogUtil.info("Invalid version number:" + ver);
			return 0;
		}
	}

	private void setDisable(PluginConfig pc) {
		if (!pc.isEnabled()) {
			return;// 不需要再计算
		}
		pc.setEnabled(true);
		for (PluginConfig c : configList) {
			if (c.getID().equals(pc.getID())) {
				continue;
			}
			if (c.getRequiredPlugins().containsKey(pc.getID())) {
				setDisable(c);
				continue;
			}
			for (String extendPointID : c.getRequiredExtendPoints().keySet()) {
				if (pc.getExtendPoints().containsKey(extendPointID)) {
					setDisable(c);
					break;
				}
			}
		}
	}

	public PluginConfig getPluginConfig(List<PluginConfig> list, String pluginID) {
		for (PluginConfig c : list) {
			if (c.getID().equals(pluginID)) {
				return c;
			}
		}
		return null;
	}

	public PluginConfig getPluginConfig(String pluginID) {
		return getPluginConfig(configList, pluginID);
	}

	public ArrayList<PluginConfig> getAllPluginConfig() {
		return configList;
	}
}
