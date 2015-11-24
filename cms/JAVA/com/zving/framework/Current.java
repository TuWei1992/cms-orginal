package com.zving.framework;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.zving.framework.User.UserData;
import com.zving.framework.collection.Mapx;
import com.zving.framework.core.Dispatcher;
import com.zving.framework.core.IURLHandler;
import com.zving.framework.core.method.IMethodLocator;
import com.zving.framework.data.Transaction;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.ui.zhtml.ZhtmlExecuteContext;
import com.zving.framework.ui.zhtml.ZhtmlManagerContext;
import com.zving.framework.utility.Errorx;
import com.zving.framework.utility.ServletUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 当前数据类，访问当前http请求中的相关对象的便捷方式
 * 
 * @author 王育春
 * @date 2009-9-30
 * @email wyuch@zving.com
 */
public class Current {
	/**
	 * 各线程数据分离
	 */
	private static ThreadLocal<CurrentData> current = new ThreadLocal<CurrentData>();

	/**
	 * 清除当前数据
	 */
	public static void clear() {
		if (current.get() != null) {
			current.get().clear();
		}
	}

	/**
	 * 设置线程上下文有效的变量
	 */
	public static void put(String key, Object value) {
		CurrentData data = current.get();
		if (data == null) {
			data = new CurrentData();
			data.values = new Mapx<String, Object>();
			current.set(data);
		} else if (data.values == null) {
			data.values = new Mapx<String, Object>();
		}
		if (value instanceof Map) {
			Map<?, ?> vmap = (Map<?, ?>) value;
			for (Object k : vmap.keySet()) {
				data.values.put(key + "." + k, vmap.get(k));
			}
		}
		data.values.put(key, value);
	}

	/**
	 * 获得线程上下文有效的变量
	 */
	public static Object get(String key) {
		CurrentData data = current.get();
		if (data == null) {
			return null;
		}
		return data.values.get(key);
	}

	/**
	 * 获得线程上下文有效的所有变量
	 */
	public static Map<String, Object> getValues() {// NO_UCD
		CurrentData data = current.get();
		if (data == null) {
			return null;
		}
		return data.values;
	}

	/**
	 * 准备Current数据
	 */
	public static void prepare(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		CurrentData cd = current.get();
		if (cd == null) {
			cd = new CurrentData();
			current.set(cd);
		}
		if (cd.handler == null && servletRequest != null) {
			RequestData request = Current.getRequest();
			String data = servletRequest.getParameter(Constant.Data);

			request.setServletRequest(servletRequest);
			Current.getResponse().setServletResponse(servletResponse);

			request.setQueryString(servletRequest.getQueryString());
			request.setHttpMethod(servletRequest.getMethod());
			request.putAll(ServletUtil.getParameterMap(servletRequest));
			if (StringUtil.isNotEmpty(data)) {
				request.setURL(servletRequest.getParameter(Constant.URL));
				request.remove(Constant.Data);
				request.remove(Constant.URL);
				request.remove(Constant.Method);
				request.remove(Constant.DataFormat);
				if (data.startsWith("<?xml")) {
					request.parseXML(data);
				} else if (data.startsWith("{") || data.startsWith("[")) {
					request.parseJSON(data);
				}
			} else {
				if (StringUtil.isEmpty(servletRequest.getQueryString())) {
					request.setURL(servletRequest.getRequestURL().toString());
				} else {
					request.setURL(servletRequest.getRequestURL().append("?").append(servletRequest.getQueryString()).toString());
				}
			}
			request.setClientIP(ServletUtil.getRealIP(servletRequest));
			request.setServerName(servletRequest.getServerName());
			request.setPort(servletRequest.getServerPort());
			request.setScheme(servletRequest.getScheme());
			HttpSession session = servletRequest.getSession(false);
			if (session != null) {
				request.setSessionID(session.getId());
			}
		}
	}

	/**
	 * 获取当前线程中的事务
	 * 
	 * @return
	 */
	public static Transaction getTransaction() {
		CurrentData data = current.get();
		if (data == null) {
			data = new CurrentData();
			current.set(data);
		}
		Transaction tran = data.transaction;
		if (tran == null) {
			tran = new Transaction();
			data.transaction = tran;
		}
		return tran;
	}

	/**
	 * 获取当前请求中的RequestImpl对象
	 * 
	 * @return
	 */
	public static RequestData getRequest() {
		CurrentData data = current.get();
		if (data == null) {
			return null;
		}
		return data.request;
	}

	/**
	 * 获取当前请求中的UIFacade对象
	 * 
	 * @return
	 */
	public static UIFacade getUIFacade() {
		CurrentData data = current.get();
		if (data == null) {
			return null;
		}
		return data.facade;
	}

	/**
	 * 设置当前的UIFacade对象
	 */
	public static void setUIFacade(UIFacade facade) {
		CurrentData data = current.get();
		if (data == null) {
			data = new CurrentData();
			current.set(data);
		}
		data.facade = facade;
	}

	/**
	 * 获取当前请求中的ResponseImpl对象
	 * 
	 * @return
	 */
	public static ResponseData getResponse() {
		CurrentData data = current.get();
		if (data == null) {
			return null;
		}
		return data.response;
	}

	/**
	 * 获取当前请求中的CookieImpl对象
	 * 
	 * @return
	 */
	public static CookieData getCookies() {
		CurrentData data = current.get();
		if (data == null) {
			return null;
		}
		return data.request.getCookies();
	}

	/**
	 * 获取当前请求中的单个Cookie值
	 * 
	 * @return
	 */
	public static String getCookie(String name) {
		CookieData cc = getCookies();
		if (cc == null) {
			return null;
		}
		return cc.getCookie(name);
	}

	/**
	 * 获取http变量解析
	 * 
	 * @return
	 */
	public static AbstractExecuteContext getExecuteContext() {
		CurrentData data = current.get();
		if (data == null) {
			return null;
		}
		if (data.executeContext == null) {
			data.executeContext = new ZhtmlExecuteContext(ZhtmlManagerContext.getInstance(), null, null);
		}
		return data.executeContext;
	}

	/**
	 * 设置当前的模板执行上下文
	 */
	public static void setExecuteContext(AbstractExecuteContext context) {
		CurrentData data = current.get();
		if (data == null) {
			data = new CurrentData();
			current.set(data);
		}
		data.executeContext = context;
	}

	/**
	 * 设置用户
	 */
	public static void setUser(UserData ud) {
		CurrentData data = current.get();
		if (data == null) {
			data = new CurrentData();
			current.set(data);
		}
		data.userData = ud;
	}

	/**
	 * 获取用户
	 */
	public static UserData getUser() {
		CurrentData data = current.get();
		if (data == null) {
			return null;
		}
		return data.userData;
	}

	/**
	 * 获取调度
	 * 
	 * @return
	 */
	public static Dispatcher getDispatcher() {
		CurrentData data = current.get();
		if (data == null) {
			return null;
		}
		return data.dispatcher;
	}

	/**
	 * 设置URLHandler
	 * 
	 * @param handler
	 */
	public static void setURLHandler(IURLHandler handler) {
		CurrentData data = current.get();
		if (data == null) {
			data = new CurrentData();
			current.set(data);
		}
		data.handler = handler;
	}

	/**
	 * 获取URLHandler
	 * 
	 * @return
	 */
	public static IURLHandler getURLHandler() {
		CurrentData data = current.get();
		if (data == null) {
			return null;
		}
		return data.handler;
	}

	/**
	 * 设置URLHandler
	 * 
	 * @param handler
	 */
	public static void setMethod(IMethodLocator method) {
		CurrentData data = current.get();
		if (data == null) {
			data = new CurrentData();
			current.set(data);
		}
		data.method = method;
	}

	/**
	 * 获取当前方法，注意，一次请求可能会调用多个方法，因此在一次http请求中本方法可能会返回不同的结果
	 * 
	 * @return
	 */
	public static IMethodLocator getMethod() {
		CurrentData data = current.get();
		if (data == null) {
			return null;
		}
		return data.method;
	}

	/**
	 * 获取错误
	 * 
	 * @return
	 */
	public static Errorx getErrorx() {
		CurrentData data = current.get();
		if (data == null) {
			data = new CurrentData();
			current.set(data);
		}
		if (data.errorx == null) {
			data.errorx = new Errorx();
		}
		return data.errorx;
	}

	/**
	 * 获取当前数据
	 * 
	 * @return
	 */
	public static CurrentData getCurrentData() {
		return current.get();
	}

	public static class CurrentData {
		public IURLHandler handler;
		public AbstractExecuteContext executeContext;
		public UIFacade facade;
		public IMethodLocator method;
		public UserData userData;

		public RequestData request = new RequestData();
		public ResponseData response = new ResponseData();
		public Transaction transaction = new Transaction();
		public Dispatcher dispatcher = new Dispatcher();
		public Mapx<String, Object> values = new Mapx<String, Object>();
		public Errorx errorx = new Errorx();

		/**
		 * 清空数据
		 */
		public void clear() {
			handler = null;
			facade = null;
			method = null;
			executeContext = null;
			userData = null;
			transaction = null;
			if (errorx != null) {
				Errorx.clear();
			}
			if (values != null) {
				if (values.getEntryTableLength() < 64) {
					values.clear();
				} else {
					values = new Mapx<String, Object>();
				}
			}
			if (dispatcher != null) {
				dispatcher.clear();
			}
			if (request != null) {
				if (request.getEntryTableLength() < 64) {
					request.clear();
				} else {
					request = new RequestData();
				}
			}
			if (response != null) {
				if (response.getEntryTableLength() < 64) {
					response.clear();
				} else {
					response = new ResponseData();
				}
			}
		}
	}

}
