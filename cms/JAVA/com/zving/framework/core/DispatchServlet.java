package com.zving.framework.core;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.zving.framework.Config;
import com.zving.framework.Constant;
import com.zving.framework.Current;
import com.zving.framework.SessionListener;
import com.zving.framework.User;
import com.zving.framework.User.UserData;
import com.zving.framework.collection.ConcurrentMapx;
import com.zving.framework.config.DefaultServletName;
import com.zving.framework.config.SetRequestEncoding;
import com.zving.framework.config.SetResponseEncoding;
import com.zving.framework.core.Dispatcher.DispatchException;
import com.zving.framework.core.Dispatcher.HandleEndException;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.utility.Errorx;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.framework.xml.XMLElement;
import com.zving.framework.xml.XMLParser;
import com.zving.preloader.PreClassLoader;
import com.zving.preloader.facade.HttpSessionListenerFacade;

/**
 * 框架分发Servlet，执行页面的字符集串设置、会话检查、线程全局对象初始化等动作，<br>
 * 然后遍历所有IURLHandler，如果有handler处理了URL则结束处理；<br>
 * 如果没有handler处理，则调用中间件的默认servlet处理.<br>
 * <br>
 * 注意：默认支持从Tomcat\Jetty\JBoss\GlassFish\Resin\blogic\WebSphere，<br>
 * 如果是其他中间件，则 有可能需要配置DefaultServletName
 * 
 * @Author 王育春
 * @Date 2006-6-18
 * @Mail wyuch@zving.com <br>
 */
public class DispatchServlet extends HttpServlet {
	private static final String DEFAULT_SERVLET_COMMON = "default";// Tomcat, Jetty, JBoss, and GlassFish
	private static final String DEFAULT_SERVLET_GAE = "_ah_default";// Google App Engine
	private static final String DEFAULT_SERVLET_RESIN = "resin-file";// Resin
	private static final String DEFAULT_SERVLET_WEBLOGIC = "FileServlet";// Weblogic
	private static final String DEFAULT_SERVLET_WEBSPHERE = "SimpleFileServlet";// WebSphere

	private static final long serialVersionUID = 1L;
	protected static boolean initFlag = true;
	private static ServletContext context = null;
	protected static ConcurrentMapx<Thread, ClassLoader> httpThreads = new ConcurrentMapx<Thread, ClassLoader>();
	protected static long lastThreadCheckTime = 0;
	private static DispatchServlet instance;
	private ConcurrentMapx<String, Integer> fileMap = new ConcurrentMapx<String, Integer>();

	private String[] notFilterPaths;
	private String defaultServletName;
	private String[] welcomeFiles;
	RequestDispatcher defaultServlet = null;

	/**
	 * 返回全局实例.<br>
	 */
	public static DispatchServlet getInstance() {
		return instance;
	}

	/**
	 * 返回ServletContext
	 * 
	 * @return
	 */
	public static ServletContext getContext() {// NO_UCD
		return context;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		if (Config.getServletMajorVersion() == 0) {
			context = config.getServletContext();
			Config.getMapx().put("System.ContainerInfo", context.getServerInfo());// 连接池需要这个属性，所以先置
			Config.setPluginContext(true);
			Config.getJBossInfo();// 注意：Jboss下这个值是ApacheTomcat/5.x之类的，且MailFilter会后于Config执行
			Config.setServletMajorVersion(context.getMajorVersion());
			Config.setServletMinorVersion(context.getMinorVersion());
			Config.setValue("App.Uptime", "" + System.currentTimeMillis());
			ExtendManager.getInstance().start();
			setServletContext(context);

			// 读取welcome-file-list
			String xml = FileUtil.readText(Config.getWEBINFPath() + "web.xml");
			XMLParser p = new XMLParser(xml);
			List<XMLElement> list = p.parse().elements("web-app.welcome-file-list.welcome-file");
			welcomeFiles = new String[list.size()];
			int i = 0;
			for (XMLElement ele : list) {
				welcomeFiles[i++] = ele.getText().trim();
			}
			instance = this;
			LogUtil.info("----" + Config.getAppCode() + "(" + LangUtil.get(Config.getAppName()) + "): Filter Initialized----");
		}
		String paths = config.getInitParameter("noFilterPath");
		if (StringUtil.isNotEmpty(paths)) {
			notFilterPaths = paths.split(",");
			for (int i = 0; i < notFilterPaths.length; i++) {
				String path = notFilterPaths[i];
				if (!path.startsWith("/")) {
					path = "/" + path;
				}
				if (!path.endsWith("/")) {
					path = path + "/";
				}
				notFilterPaths[i] = path;
			}
		}
		for (IURLHandler up : URLHandlerService.getInstance().getAll()) {
			up.init();
		}
	}

	/**
	 * Copy from Spring
	 */
	public void setServletContext(ServletContext servletContext) {
		if (servletContext.getNamedDispatcher(DEFAULT_SERVLET_COMMON) != null) {
			defaultServletName = DEFAULT_SERVLET_COMMON;
		} else if (servletContext.getNamedDispatcher(DEFAULT_SERVLET_GAE) != null) {
			defaultServletName = DEFAULT_SERVLET_GAE;
		} else if (servletContext.getNamedDispatcher(DEFAULT_SERVLET_RESIN) != null) {
			defaultServletName = DEFAULT_SERVLET_RESIN;
		} else if (servletContext.getNamedDispatcher(DEFAULT_SERVLET_WEBLOGIC) != null) {
			defaultServletName = DEFAULT_SERVLET_WEBLOGIC;
		} else if (servletContext.getNamedDispatcher(DEFAULT_SERVLET_WEBSPHERE) != null) {
			defaultServletName = DEFAULT_SERVLET_WEBSPHERE;
		} else if (ObjectUtil.notEmpty(DefaultServletName.getValue())) {
			defaultServletName = DefaultServletName.getValue();
		} else {
			throw new IllegalStateException("Unable to locate the default servlet for serving static content. "
					+ "Please set the 'defaultServletName' property explicitly.");
		}
	}

	public void forwardToDefaultServlet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (defaultServlet == null) {
			defaultServlet = context.getNamedDispatcher(defaultServletName);
			if (defaultServlet == null) {
				throw new IllegalStateException("A RequestDispatcher could not be located for the default servlet '" + defaultServletName
						+ "'");
			}
		}
		defaultServlet.forward(request, response);
	}

	public boolean isNoFilterPath(String url) {
		if (notFilterPaths == null) {
			return false;
		}
		for (String noFilterPath : notFilterPaths) {
			if (url.indexOf(noFilterPath) >= 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void service(ServletRequest req, ServletResponse rep) throws IOException, ServletException {
		long t = System.currentTimeMillis();
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) rep;
		String requestURI = request.getRequestURI();
		String context = request.getContextPath();
		String url = requestURI.substring(context.length(), requestURI.length());// 以/开头
		url = url.replace("//", "/");
		if (ObjectUtil.empty(url)) {
			response.sendRedirect(context + "/");
			return;
		}

		if (isNoFilterPath(url)) {
			forwardToDefaultServlet(request, response);
			return;
		}

		// 初始化路径，需要考虑集群的情况，需要考虑内外网路径不一致的情况
		// 注意如果遇到以下情况的时候cookie设值得到的path会有所不同：本地访问路径是http://IP/ZCMS，而外网访问路径是:http://域名
		if (Config.isComplexDepolyMode() || initFlag) {
			if (context.length() == 0 || context.charAt(context.length() - 1) != '/') {
				context = context + "/";
			}
			User.setValue("App.ContextPath", context);
			if (initFlag) {
				Config.setValue("App.ContextPath", context);
				initFlag = false;
			}
		}
		if (SetRequestEncoding.getValue()) {
			request.setCharacterEncoding(Config.getGlobalCharset());
		}
		if (SetResponseEncoding.getValue()) {
			if (Config.getServletMajorVersion() == 2 && Config.getServletMinorVersion() == 3) {
				response.setContentType("text/html;charset=" + Config.getGlobalCharset());
			} else {
				response.setCharacterEncoding(Config.getGlobalCharset());
			}
		}
		Current.prepare(request, response);
		try {
			if (Thread.currentThread().getContextClassLoader() != PreClassLoader.getInstance()) {
				httpThreads.put(Thread.currentThread(), Thread.currentThread().getContextClassLoader());
				Thread.currentThread().setContextClassLoader(PreClassLoader.getInstance());
				if (System.currentTimeMillis() - lastThreadCheckTime > 300000) {// 5分钟检查一次
					for (Thread thread : httpThreads.keySet()) {
						if (!thread.isAlive()) {
							httpThreads.remove(thread);
						}
					}
					lastThreadCheckTime = System.currentTimeMillis();
				}
			}

			tryRestoreSession(request, response);

			handleURL(url, request, response);

			try {
				if (!"true".equals(request.getParameter(Constant.NoSession)) && !"true".equals(request.getAttribute(Constant.NoSession))) {
					UserData ud = User.getCurrent();
					if (ud != null) {// 已经置了值了
						HttpSession session = request.getSession(false);
						if (session != null && session.getAttribute(Constant.UserAttrName) != ud) {
							ud.setSessionID(session.getId());
							session.setAttribute(Constant.UserAttrName, ud);// 必须在此重置，因此User对象可能被重置了
						}
					}
				}
			} catch (Exception e) {
				// 有可能已经被invalidate了
			}
			if (!Errorx.hasDealed()) {
				LogUtil.warn("Error not dealed:" + Errorx.printString());
			}
			User.tryCacheCurrentUserData();
		} catch (RuntimeException e) {// 集中异常处理
			boolean catched = false;
			for (IExceptionCatcher ec : ExceptionCatcherService.getInstance().getAll()) {
				for (Class<?> c : ec.getTargetExceptionClass()) {
					if (c.isInstance(e)) {
						ec.doCatch(e, request, response);
						catched = true;
					}
				}
			}
			if (!catched) {
				e.printStackTrace();
				throw e;
			}
		} finally {
			Current.clear();// 确保Current中的数据被清空
			t = System.currentTimeMillis() - t;
			if (t > 100) {
				System.out.println("URL " + url + " cost " + t + "ms.");
			}
		}
	}

	private void tryRestoreSession(HttpServletRequest request, HttpServletResponse response) {
		// 准备用户会话数据
		if ("true".equals(request.getParameter(Constant.NoSession))) {
			return;
		}
		HttpSession session = request.getSession(false);
		UserData u = null;
		if (session != null) {
			u = SessionListener.getUserDataFromSession(session);
			if (u == null) {
				if (Config.isDebugMode()) {
					u = getCachedUserData(request);
					if (u != null) {
						HttpSessionListenerFacade.setSession(session.getId(), session);
					}
				}
			}
		} else {
			if (Config.isDebugMode()) {
				u = getCachedUserData(request);
				if (u != null) {
					session = request.getSession(true);
					HttpSessionListenerFacade.setSession(session.getId(), session);
					u.setSessionID(session.getId());
				}
			}
		}
		User.setCurrent(u);
	}

	private UserData getCachedUserData(HttpServletRequest request) {
		if (Config.isDebugMode()) {
			Cookie[] cs = request.getCookies();
			if (cs != null) {
				for (Cookie element : cs) {
					if (element.getName().equals(Constant.SessionIDCookieName)) {
						return User.getCachedUser(element.getValue());
					}
				}
			}
		}
		return null;
	}

	private void handleURL(String url, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
			for (IURLHandler up : URLHandlerService.getInstance().getAll()) {
				if (up.match(url)) {
					Current.setURLHandler(up);
					try {
						if (up.handle(url, request, response)) {
							return;
						}
					} catch (HandleEndException e) {
						return;// 不再继续
					}

				}
			}
			// 判断是否是目录,需要按照welcome-file-list逐个试探
			String fullName = context.getRealPath(url);
			Integer i = fileMap.get(fullName);
			if (i != null) {
				if (i == 1) {// 表示文件
					forwardToDefaultServlet(request, response);
				} else {
					forwardWelcome(url, request, response);
				}
			} else {
				File f = new File(fullName);
				if (f.exists()) {
					if (f.isFile()) {
						forwardToDefaultServlet(request, response);
						fileMap.put(fullName, 1);
					} else {
						forwardWelcome(url, request, response);
						fileMap.put(fullName, 2);
					}
				}
			}
		} catch (DispatchException e) {// 此异常仅用于指示跳转
			Dispatcher d = Current.getDispatcher();
			if (d != null) {
				if (d.forwardURL != null) {
					url = d.forwardURL;
					d.forwardURL = null;
					handleURL(url, request, response);
				} else if (d.redirectURL != null) {
					url = d.redirectURL;
					d.redirectURL = null;
					response.sendRedirect(url);
				}
			}
		}
	}

	private void forwardWelcome(String url, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		if (!url.endsWith("/")) {
			response.sendRedirect(request.getContextPath() + url + "/");
			return;
		}
		for (String str : welcomeFiles) {
			for (IURLHandler up : URLHandlerService.getInstance().getAll()) {
				if (up.match(url + str)) {
					Current.setURLHandler(up);
					try {
						if (up.handle(url + str, request, response)) {
							return;
						}
					} catch (HandleEndException e) {
						break;
					}
				}
			}
		}
	}

	@Override
	public void destroy() {
		try {
			for (IURLHandler up : URLHandlerService.getInstance().getAll()) {
				up.destroy();
			}
		} finally {
			for (Thread t : httpThreads.keySet()) {
				if (t.isAlive()) {
					try {
						t.setContextClassLoader(httpThreads.get(t));
					} catch (Throwable e) {
					}
				}
			}
		}
	}

}
