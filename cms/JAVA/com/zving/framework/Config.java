package com.zving.framework;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.zving.cxdata.UCMConfig;
import com.zving.framework.User.UserData;
import com.zving.framework.collection.ConcurrentMapx;
import com.zving.framework.collection.Mapx;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.framework.xml.XMLElement;
import com.zving.preloader.facade.HttpSessionListenerFacade;

/**
 * 全局配置信息类。<br>
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-4-15
 */
public class Config {
	/**
	 * 保存各配置项的Map
	 */
	protected static ConcurrentMapx<String, String> configMap = new ConcurrentMapx<String, String>();

	/**
	 * 数据库是否己配置
	 */
	protected static boolean isInstalled = false;

	/**
	 * 是否临时禁止登录
	 */
	protected static boolean isAllowLogin = true;

	/**
	 * 是否运行在一个插件环境下
	 */
	protected static boolean isPluginContext = false;

	/**
	 * 应用代码
	 */
	protected static String appCode = null;

	/**
	 * 应用名称
	 */
	protected static String appName = null;

	/**
	 * 是否是复杂部署模式，复杂部署模式需要考虑到一个应用有多个路径的问题，例如内外网不同的访问路径
	 */
	protected static boolean isComplexDepolyMode = false;

	/**
	 * 是否是前置部署，前置部署是将非后台的功能单独部署
	 */
	protected static boolean isFrontDeploy = false;

	/**
	 * Servlet容器支持的JSP规范的最大版本
	 */
	protected static int servletMajorVersion;

	/**
	 * Servlet容器支持的JSP规范的最小版本
	 */
	protected static int servletMinorVersion;

	/**
	 * 全局字符集设置，在读写文本、与数据库通信等所有涉及到字符串但又未明确指定字符集的地方会使用全局字符集进行操作
	 */
	protected static String globalCharset;

	/**
	 * classes目录的全路径
	 */
	protected static String classesPath;
	/**
	 * 调试模式标识
	 */
	protected static Boolean isDebugMode;

	/**
	 * 初始化配置项
	 */
	protected static void init() {
		if (!configMap.containsKey("System.JavaVersion")) {
			ConfigLoader.load();

			configMap.put("App.ContextRealPath", Config.getContextRealPath());
			configMap.put("System.JavaVersion", System.getProperty("java.version"));
			configMap.put("System.JavaVendor", System.getProperty("java.vendor"));
			configMap.put("System.JavaHome", System.getProperty("java.home"));
			configMap.put("System.OSPatchLevel", System.getProperty("sun.os.patch.level"));// 其他JDK以后补充
			configMap.put("System.OSArch", System.getProperty("os.arch"));
			configMap.put("System.OSVersion", System.getProperty("os.version"));
			configMap.put("System.OSName", System.getProperty("os.name"));
			if (System.getProperty("os.name").toLowerCase().indexOf("windows") > 0 && System.getProperty("os.name").equals("6.1")) {
				configMap.put("System.OSName", "Windows 7");
			}
			configMap.put("System.OSUserLanguage", System.getProperty("user.language"));
			configMap.put("System.OSUserName", System.getProperty("user.name"));
			configMap.put("System.LineSeparator", System.getProperty("line.separator"));
			configMap.put("System.FileSeparator", System.getProperty("file.separator"));
			configMap.put("System.FileEncoding", System.getProperty("file.encoding"));

			List<XMLElement> datas = ConfigLoader.getElements("framework.application.config");
			if (datas == null) {
				LogUtil.warn("File framework.xml not found");
				isInstalled = false;
				return;
			}
			for (XMLElement data : datas) {
				configMap.put("App." + data.getAttributes().get("name"), data.getText());
			}
			datas = ConfigLoader.getElements("*.allowUploadExt.config");
			for (XMLElement data : datas) {
				configMap.put(data.getAttributes().get("name"), data.getText());
			}
			datas = ConfigLoader.getElements("data.config");
			for (XMLElement data : datas) {
				configMap.put(data.getAttributes().get("name"), data.getAttributes().get("value"));
			}
			isComplexDepolyMode = "true".equals(configMap.get("App.ComplexDepolyMode"));
			isFrontDeploy = "true".equals(configMap.get("App.FrontDeploy"));

			datas = ConfigLoader.getElements("framework.databases.database");
			for (XMLElement data : datas) {
				String dbname = data.getAttributes().get("name");
				List<XMLElement> children = data.elements();
				for (int k = 0; k < children.size(); k++) {
					String attr = children.get(k).getAttributes().get("name");
					String value = children.get(k).getText();
					if (attr.equalsIgnoreCase("Password")) {
						if (value.startsWith("$KEY")) {// 以下是临时写法，以兼容以前的版本
							try {
								Class<?> c = Class.forName("com.zving.framework.security.EncryptUtil");
								if (c != null) {
									Method decrypt3DES = c.getMethod("decrypt3DES", new Class<?>[] { String.class, String.class });
									Object defaultKey = c.getField("DEFAULT_KEY").get(null);
									Object obj = decrypt3DES.invoke(null, new Object[] { value.substring(4), defaultKey });
									if (obj != null) {
										value = obj.toString();
									}
								}
							} catch (Exception e) {
							}
						}
					}
					//车享 从UCM统一配置中心读取数据库配置信息
					if (value.startsWith(UCMConfig.CONFIGKEY)) {
						value = UCMConfig.getValue(value.substring(UCMConfig.CONFIGKEY.length()+1));
					}
					
					configMap.put("Database." + dbname + "." + attr, value);
				}
			}
			if (datas.size() > 0) {
				isInstalled = true;
			} else {
				isInstalled = false;
			}
			LogUtil.info("----" + Config.getAppCode() + "(" + Config.getAppName() + "): Config Initialized----");
		}
	}

	/**
	 * 载入所有全局配置项
	 */
	public static void loadConfig() {
		configMap.remove("System.JavaVersion");
		ConfigLoader.reload();
		init();
	}

	/**
	 * @return 所有全局配置项组成的Mapx
	 */
	public static Mapx<String, String> getMapx() {
		return configMap;
	}

	/**
	 * @return 是否是前置部署
	 */
	public static boolean isFrontDeploy() {
		return isFrontDeploy;
	}

	/**
	 * @return 导出Excel时使用的默认版本
	 */
	public static String getExcelVersion() {
		String ev = getValue("App.ExcelVersion");
		if (StringUtil.isEmpty(ev)) {
			ev = "2007"; // 默认2007
		}
		return ev;
	}

	/**
	 * 返回配置项的值，XML配置文件中的framework/application/config节点中的配置项名称必须使用“App.”前缀访问。
	 * 
	 * @param configName 配置项名称
	 * @return 配置项的值
	 */
	public static String getValue(String configName) {
		init();
		return configMap.get(configName);
	}

	/**
	 * 设置配置项的值
	 * 
	 * @param configName 配置项名称
	 * @param configValue 配置项的值
	 */
	public static void setValue(String configName, String configValue) {
		init();
		configMap.put(configName, configValue);
	}

	/**
	 * 返回WEB-INF所在目录的全路径
	 */
	public static String getWEBINFPath() {
		String path = Config.getClassesPath();
		if (path.indexOf("WEB-INF") > 0) {
			path = path.substring(0, path.lastIndexOf("WEB-INF") + 8);
		}
		return path;
	}

	/**
	 * J2EE环境下返回WEB-INF/plugins/classes目录的实际路径，独立运行时返回class的根目录
	 */
	public static String getClassesPath() {
		if (classesPath == null) {
			URL url = Config.class.getClassLoader().getResource("com/zving/framework/Config.class");
			if (url == null) {
				System.err.println("Config.getClassesPath() failed!");
				return "";
			}
			try {
				String path = URLDecoder.decode(url.getPath(), Config.getFileEncode());
				if (System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0) {
					if (path.startsWith("/")) {
						path = path.substring(1);
					}
				}
				if (path.startsWith("file:/")) {
					path = path.substring(6);
				} else if (path.startsWith("jar:file:/")) {
					path = path.substring(10);
				}
				if (path.indexOf(".jar!") > 0) {
					path = path.substring(0, path.indexOf(".jar!"));
				}
				path = path.replace('\\', '/');
				path = path.substring(0, path.lastIndexOf("/") + 1);
				if (path.indexOf("WEB-INF") >= 0) {
					path = path.substring(0, path.lastIndexOf("WEB-INF") + 7) + "/plugins/classes/";
				}
				if (System.getProperty("os.name").toLowerCase().indexOf("windows") < 0) {
					if (!path.startsWith("/")) {
						path = "/" + path;
					}
				}
				classesPath = path;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return classesPath;
	}

	/**
	 * 返回插件文件所在目录
	 */
	public static String getPluginPath() {
		File f = new File(getClassesPath());
		String path = f.getParentFile().getAbsolutePath() + "/";
		return path;
	}

	/**
	 * WEB应用下返回应用的实际路径
	 */
	public static String getContextRealPath() {
		if (configMap != null) {
			String str = configMap.get("App.ContextRealPath");
			if (str != null) {
				return str;
			}
		}
		String path = getClassesPath();
		int index = path.indexOf("WEB-INF");
		if (index > 0) {
			path = path.substring(0, index);
		}
		return path;
	}

	/**
	 * 返回应用路径，返回值以/结束。
	 * 考虑到同一个应用在内外网有不同的路径的情况，该处变量在每一次进入Filter后都会重新设置<br>
	 */
	public static String getContextPath() {
		if (isComplexDepolyMode) {
			String path = (String) User.getValue("App.ContextPath");
			if (StringUtil.isEmpty(path)) {
				path = Config.getValue("App.ContextPath");
			}
			return path;
		} else {
			return Config.getValue("App.ContextPath");
		}
	}

	private static void initProduct() {
		if (appCode == null) {
			if (configMap.get("App.Code") != null) {
				appCode = configMap.get("App.Code");
				appName = configMap.get("App.Name");
			}
			if (appCode == null) {
				appCode = LangUtil.get("@{Product.Code}");
				appName = LangUtil.get("@{Product.Name}");
			}
			if (appCode == null) {
				appCode = "ZCF";
				appName = "Zving Common Framework";
			}
		}
	}

	/**
	 * @return 应用代码
	 */
	public static String getAppCode() {
		initProduct();
		return appCode;
	}

	/**
	 * @return 应用名称
	 */
	public static String getAppName() {
		initProduct();
		return appName;
	}

	/**
	 * @return 是否是调试模式，调试模式将会自动复原Session
	 */
	public static boolean isDebugMode() {
		if (isDebugMode == null) {
			isDebugMode = "true".equalsIgnoreCase(Config.getValue("App.DebugMode"));
		}
		return isDebugMode;
	}

	/**
	 * @return 中间件容器信息
	 */
	public static String getContainerInfo() {
		return Config.getValue("System.ContainerInfo");
	}

	/**
	 * @return 中间件容器的版本
	 */
	public static String getContainerVersion() {// NO_UCD
		String str = Config.getValue("System.ContainerInfo");
		if (str.indexOf("/") > 0) {
			return str.substring(str.lastIndexOf("/") + 1);
		}
		return "0";
	}

	/**
	 * @return 文本文件默认分隔符
	 */
	public static String getLineSeparator() {// NO_UCD
		return Config.getValue("System.LineSeparator");
	}

	/**
	 * @return 文件名中的路径分隔符
	 */
	public static String getFileSeparator() {// NO_UCD
		return Config.getValue("System.FileSeparator");
	}

	/**
	 * @return 操作系统的默认文件编码
	 */
	public static String getFileEncode() {
		return System.getProperty("file.encoding");
	}

	/**
	 * @return 己登录的后台用户数
	 */
	public static int getLoginUserCount() {
		int count = 0;
		for (HttpSession session : HttpSessionListenerFacade.getMap().values()) {
			UserData ud = SessionListener.getUserDataFromSession(session);
			if (ud != null && ud.isLogin()) {
				count++;
			}
		}
		return count;
	}

	/**
	 * @return 己登录的会员数
	 */
	public static int getLoginMemberCount() {
		int count = 0;
		for (HttpSession session : HttpSessionListenerFacade.getMap().values()) {
			UserData ud = SessionListener.getUserDataFromSession(session);
			if (ud != null && ud.getMemberData() != null && ud.getMemberData().isLogin) {
				count++;
			}
		}
		return count;
	}

	/**
	 * @return 中间件是否是Tomcat
	 */
	public static boolean isTomcat() {
		if (StringUtil.isEmpty(Config.getContainerInfo())) {
			getJBossInfo();
		}
		return Config.getContainerInfo().toLowerCase().indexOf("tomcat") >= 0;
	}

	/**
	 * JBoss需要特别处理 JBoss调用ServletContext.getServerInfo()时会返回Apache Tomcat
	 * 5.x之类的， 且MainFilter会后面Config执行，需要特别处理
	 */
	public static void getJBossInfo() {
		String jboss = System.getProperty("jboss.home.dir");
		if (StringUtil.isNotEmpty(jboss)) {
			try {
				Class<?> c = Class.forName("org.jboss.Version");
				Method m = c.getMethod("getInstance", (Class[]) null);
				Object o = m.invoke(null, (Object[]) null);
				m = c.getMethod("getMajor", (Class[]) null);
				Object major = m.invoke(o, (Object[]) null);
				m = c.getMethod("getMinor", (Class[]) null);
				Object minor = m.invoke(o, (Object[]) null);
				m = c.getMethod("getRevision", (Class[]) null);
				Object revision = m.invoke(o, (Object[]) null);
				m = c.getMethod("getTag", (Class[]) null);
				Object tag = m.invoke(o, (Object[]) null);
				Config.configMap.put("System.ContainerInfo", "JBoss/" + major + "." + minor + "." + revision + "." + tag);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return 中间件是否是JBoss
	 */
	public static boolean isJboss() {
		if (StringUtil.isEmpty(Config.getContainerInfo())) {
			getJBossInfo();
		}
		return Config.getContainerInfo().toLowerCase().indexOf("jboss") >= 0;
	}

	/**
	 * @return 中间件是否是WebLogic
	 */
	public static boolean isWeblogic() {
		return Config.getContainerInfo().toLowerCase().indexOf("weblogic") >= 0;
	}

	/**
	 * @return 中间件是否是WebSphere
	 */
	public static boolean isWebSphere() {
		return Config.getContainerInfo().toLowerCase().indexOf("websphere") >= 0;
	}

	/**
	 * @return 是否是复杂部署模式，该模式下一个应用可能因为对外暴露的地址不一样而有多个ContextPath
	 */
	public static boolean isComplexDepolyMode() {
		return isComplexDepolyMode;
	}

	/**
	 * @return 后台用户登陆页面（相对于应用路径的地址）
	 */
	public static String getLoginPage() {
		String str = configMap.get("App.LoginPage");
		if (StringUtil.isNotEmpty(str)) {// 可能是没有配置文件
			return str;
		}
		return "login.zhtml";
	}

	/**
	 * @return 应用全局字符集
	 */
	public static String getGlobalCharset() {
		if (globalCharset == null) {
			ConfigLoader.load();
		}
		if (globalCharset == null) {// 不存在charset.config
			globalCharset = "UTF-8";
		}
		return globalCharset;
	}

	/**
	 * @return 当前运行环境是否是一个插件上下文。如果不是插件上下文，则插件和扩展不会加载。
	 */
	public static boolean isPluginContext() {
		return isPluginContext;
	}

	/**
	 * 设置当前运行环境是否是一个插件上下文。如果不是插件上下文，则插件和扩展不会加载。
	 */
	public static void setPluginContext(boolean isPluginContext) {
		Config.isPluginContext = isPluginContext;
	}

	/**
	 * 设置是否是前置部署
	 */
	public static void setFrontDeploy(boolean flag) {// NO_UCD
		isFrontDeploy = flag;
	}

	/**
	 * 返回操作系统名称
	 */
	public static String getOSName() {
		return Config.getValue("System.OSName");
	}

	public static boolean isInstalled() {
		return isInstalled;
	}

	public static boolean isAllowLogin() {
		return isAllowLogin;
	}

	public static void setAllowLogin(boolean isAllowLogin) {
		Config.isAllowLogin = isAllowLogin;
	}

	public static void setInstalled(boolean isInstalled) {
		Config.isInstalled = isInstalled;
	}

	public static int getOnlineUserCount() {
		return HttpSessionListenerFacade.getMap().size();
	}

	public static int getServletMajorVersion() {
		return servletMajorVersion;
	}

	public static int getServletMinorVersion() {
		return servletMinorVersion;
	}

	public static Boolean getIsDebugMode() {
		return isDebugMode;
	}

	public static void setServletMajorVersion(int servletMajorVersion) {
		Config.servletMajorVersion = servletMajorVersion;
	}

	public static void setServletMinorVersion(int servletMinorVersion) {
		Config.servletMinorVersion = servletMinorVersion;
	}
}
