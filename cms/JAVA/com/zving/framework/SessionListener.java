package com.zving.framework;

import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.zving.framework.User.UserData;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.extend.action.AfterSessionCreateAction;
import com.zving.framework.extend.action.BeforeSessionDestroyAction;
import com.zving.framework.utility.FileUtil;
import com.zving.preloader.facade.HttpSessionListenerFacade;

/**
 * Session监听器，监听用户的增减情况，并提供用户相关的数据。
 * 
 * @Author 王育春
 * @Date 2007-7-16
 * @Mail wyuch@zving.com
 */
public class SessionListener implements HttpSessionListener {

	/**
	 * 会话创建时执行本方法
	 * 
	 * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
	 */
	@Override
	public void sessionCreated(HttpSessionEvent arg0) {
		ExtendManager.invoke(AfterSessionCreateAction.ExtendPointID, new Object[] { arg0.getSession() });
	}

	/**
	 * 会话失效时执行本方法
	 * 
	 * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
	 */
	@Override
	public void sessionDestroyed(HttpSessionEvent arg0) {
		UserData u = SessionListener.getUserDataFromSession(arg0.getSession());
		if (u != null) {
			if (Config.isDebugMode()) {
				FileUtil.delete(Config.getContextRealPath() + "WEB-INF/cache/" + u.getSessionID());
			}
		}
		ExtendManager.invoke(BeforeSessionDestroyAction.ExtendPointID, new Object[] { arg0.getSession() });
	}

	/**
	 * 踢出除自己以外的其他所有用户
	 */
	public static void forceExit() {
		Map<String, HttpSession> map = HttpSessionListenerFacade.getMap();
		for (Object k : map.keySet().toArray()) {
			if (k.equals(User.getSessionID())) {
				continue;
			}
			map.get(k).invalidate();
		}
	}

	/**
	 * 获取所有状态的用户
	 * 
	 * @return
	 */
	public static UserData[] getUsers() {
		ArrayList<UserData> arr = new ArrayList<UserData>();
		for (HttpSession session : HttpSessionListenerFacade.getMap().values()) {
			UserData u = SessionListener.getUserDataFromSession(session);
			if (u != null) {
				arr.add(u);
			}
		}
		return arr.toArray(new UserData[arr.size()]);
	}

	/**
	 * 获取指定用户名的User对象
	 * 
	 * @param userName
	 *            用户名
	 * @return
	 */
	public static UserData getUser(String userName) {
		UserData[] users = getUsers();
		for (UserData user : users) {
			if (userName.equals(user.getUserName())) {
				return user;
			}
		}
		return null;
	}

	/**
	 * 获取指定用户名的User对象
	 * 
	 * @param userName
	 *            用户名
	 * @return
	 */
	public static UserData[] getUsers(String userName) {
		ArrayList<UserData> result = new ArrayList<UserData>(2);
		UserData[] users = getUsers();
		for (UserData user : users) {
			if (userName.equals(user.getUserName())) {
				result.add(user);
			}
		}
		return result.toArray(new UserData[result.size()]);
	}

	/**
	 * 主要是为了防止类重新加载后Session中的UserData对象报ClassCastException
	 */
	public static UserData getUserDataFromSession(HttpSession session) {
		if (session != null) {
			Object o = session.getAttribute(Constant.UserAttrName);
			if (o instanceof UserData) {
				return (UserData) o;
			}
		}
		return null;
	}
}
