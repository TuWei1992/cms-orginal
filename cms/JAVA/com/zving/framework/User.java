package com.zving.framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.zving.framework.Member.MemberData;
import com.zving.framework.i18n.LangMapping;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.security.Privilege;
import com.zving.framework.utility.LogUtil;

/**
 * 用户数据全局访问类，一个线程内的所有代码都可以直接访问用户数据<br>
 * 本类替代了HttpSession的作用，便利于单元测试。
 * 
 * @Author 王育春
 * @Date 2007-6-18
 * @Mail wyuch@zving.com
 */
public class User {

	/**
	 * 获取当前用户名
	 * 
	 * @return
	 */
	public static String getUserName() {
		UserData ud = getCurrent();
		return ud == null ? null : ud.getUserName();
	}

	/**
	 * 设置当前用户名
	 */
	public static void setUserName(String username) {
		getCurrent(true).setUserName(username);
	}

	/**
	 * 获取当前用户的真实名称
	 */
	public static String getRealName() {
		UserData ud = getCurrent();
		return ud == null ? null : ud.getRealName();
	}

	/**
	 * 设置当前用户的真实名称
	 */
	public static void setRealName(String realName) {
		getCurrent(true).setRealName(realName);
	}

	/**
	 * 获取当前用户的分支机构内部编码
	 */
	public static String getBranchInnerCode() {
		UserData ud = getCurrent();
		return ud == null ? null : ud.getBranchInnerCode();
	}

	/**
	 * 设置当前用户的分支机构内部编码
	 */
	public static void setBranchInnerCode(String branchInnerCode) {
		getCurrent(true).setBranchInnerCode(branchInnerCode);
	}

	/**
	 * 当前用户是否是机构管理员
	 */
	public static boolean isBranchAdministrator() {
		UserData ud = getCurrent();
		return ud == null ? false : ud.isBranchAdministrator();
	}

	/**
	 * 设置当前用户的分支机构内部编码
	 */
	public static void setBranchAdministrator(boolean flag) {
		getCurrent(true).setBranchAdministrator(flag);
	}

	/**
	 * 获取当前用户的用户类型
	 */
	public static String getType() {
		UserData ud = getCurrent();
		return ud == null ? null : ud.getType();
	}

	/**
	 * 设置当前用户的用户类型
	 */
	public static void setType(String type) {
		getCurrent(true).setType(type);
	}

	/**
	 * 按key获取指定数据项
	 */
	public static Object getValue(Object key) {
		UserData ud = getCurrent();
		return ud == null ? null : ud.get(key);
	}

	/**
	 * 设置当前用户指定数据项
	 */
	public static void setValue(String key, Object value) {
		Map<String, Object> map = getCurrent(true);
		map.put(key, value);
	}

	/**
	 * 当前用户是否己登录
	 */
	public static boolean isLogin() {
		UserData ud = getCurrent();
		return ud == null ? false : ud.isLogin();
	}

	/**
	 * 设置当前用户的登录状态
	 */
	public static void setLogin(boolean isLogin) {
		getCurrent(true).setLogin(isLogin);
	}

	/**
	 * 设置当前用户对象
	 */
	public static void setCurrent(UserData user) {
		Current.setUser(user);
		if (user == null || Current.getRequest() == null) {
			return;
		}
		HttpServletRequest request = Current.getRequest().servletRequest;
		if (request != null) {
			request.getSession(true);// 开启session
			try {
				String lang = LangUtil.getLanguage(request);
				Map<String, String> map = LangUtil.getSupportedLanguages();
				if (map.size() == 0) {
					throw new RuntimeException("No supportd language found");
				}
				if (map.containsKey(lang)) {
					user.setLanguage(lang);
				} else {
					user.setLanguage(map.keySet().iterator().next());
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	/**
	 * 获取当前用户对象
	 */
	public static UserData getCurrent() {
		return getCurrent(false);
	}

	protected static UserData getCurrent(boolean create) {// 如果Current中没有值且create为true，则置一个UserData到Current
		UserData ud = Current.getUser();
		if (ud == null) {
			ud = new UserData();
			if (create) {
				setCurrent(ud);
			}
		}
		return ud;
	}

	/**
	 * 缓存当前用户对象到文件系统
	 */
	public static void tryCacheCurrentUserData() {
		UserData u = getCurrent();
		if (u == null) {
			return;
		}
		if (u.needCache && Config.isDebugMode()) {
			try {
				if (u.getSessionID() == null) {
					return;
				}
				File dir = new File(Config.getContextRealPath() + "WEB-INF/cache/");
				if (!dir.exists()) {
					dir.mkdirs();
				}
				FileOutputStream f = new FileOutputStream(Config.getContextRealPath() + "WEB-INF/cache/" + u.getSessionID());
				ObjectOutputStream s = new ObjectOutputStream(f);
				s.writeObject(u);
				s.flush();
				s.close();
				u.needCache = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 根据会话ID从文件系统中返回己缓存的用户对象，如果没有则返回null
	 */
	public static UserData getCachedUser(String sessionID) {
		try {
			File in = new File(Config.getContextRealPath() + "WEB-INF/cache/" + sessionID);
			if (in.exists()) {
				ObjectInputStream s = new ObjectInputStream(new FileInputStream(in));
				Object o = s.readObject();
				if (UserData.class.isInstance(o)) {
					s.close();
					in.delete();
					return (UserData) o;
				}
				s.close();
			}
		} catch (Exception e) {
			LogUtil.warn("getCachedUser() failed");
		}
		return null;
	}

	/**
	 * 销毁当前用户对象
	 */
	public static void destory() {
		if (Config.isDebugMode()) {
			File f = new File(Config.getContextRealPath() + "WEB-INF/cache/" + User.getSessionID());
			if (f.exists()) {
				f.delete();
			}
		}
		User.setCurrent(null);
	}

	/**
	 * 返回当前用户的会话ID
	 */
	public static String getSessionID() {
		UserData ud = getCurrent();
		return ud == null ? null : ud.getSessionID();
	}

	/**
	 * 获得当前用户使用的语言
	 */
	public static String getLanguage() {
		UserData ud = getCurrent();
		return ud == null ? null : ud.getLanguage();
	}

	/**
	 * 设置当前用户使用的语言
	 */
	public static void setLanguage(String language) {
		getCurrent(true).setLanguage(language);
	}

	/**
	 * 获得当前用户的权限集合
	 */
	public static Privilege getPrivilege() {
		UserData ud = getCurrent();
		return ud == null ? null : ud.getPrivilege();
	}

	/**
	 * 设置当前用户的权限集合
	 */
	public static void setPrivilege(Privilege priv) {
		getCurrent(true).setPrivilege(priv);
	}

	/**
	 * 获取会员数据
	 */
	protected static MemberData getMemberData() {
		UserData ud = getCurrent();
		return ud == null ? null : ud.getMemberData();
	}

	/**
	 * 设置会员数据
	 */
	protected static void setMemberData(MemberData memberData) {
		getCurrent(true).setMemberData(memberData);
	}

	/**
	 * @date 2010-4-22 <br>
	 * @author 王育春 <br>
	 * @email wangyc@zving.com <br>
	 */
	public static class UserData extends HashMap<String, Object> implements Serializable {
		private static final long serialVersionUID = 1L;

		/**
		 * 用户类型
		 */
		private String type;

		/**
		 * 用户状态
		 */
		private String status;

		/**
		 * 用户名
		 */
		private String userName;

		/**
		 * 用户真实姓名
		 */
		private String realName;

		/**
		 * 所属分支机构
		 */
		private String branchInnerCode;

		/**
		 * 是否是机构管理员
		 */
		private boolean branchAdminFlag;

		/**
		 * 是否己登录
		 */
		private boolean isLogin = false;

		/**
		 * 会话ID
		 */
		private String sessionID;

		/**
		 * 会员数组
		 */
		private MemberData memberData = null;

		/**
		 * 数据是否需要缓存
		 */
		private boolean needCache = false;

		/**
		 * 会话语言，默认为中文
		 */
		private String language = LangMapping.getInstance().getDefaultLanguage();
		/**
		 * 权限
		 */
		private Privilege priv = new Privilege();

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
			needCache = true;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
			needCache = true;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
			needCache = true;
		}

		public String getRealName() {
			return realName;
		}

		public void setRealName(String realName) {
			this.realName = realName;
			needCache = true;
		}

		public String getBranchInnerCode() {
			return branchInnerCode;
		}

		public void setBranchInnerCode(String branchInnerCode) {
			this.branchInnerCode = branchInnerCode;
			needCache = true;
		}

		public boolean isBranchAdministrator() {
			return branchAdminFlag;
		}

		public void setBranchAdministrator(boolean flag) {
			this.branchAdminFlag = flag;
			needCache = true;
		}

		public boolean isLogin() {
			return isLogin;
		}

		public void setLogin(boolean isLogin) {
			this.isLogin = isLogin;
			needCache = true;
		}

		public String getSessionID() {
			return sessionID;
		}

		public void setSessionID(String sessionID) {
			this.sessionID = sessionID;
			needCache = true;
		}

		public String getLanguage() {
			return language;
		}

		public void setLanguage(String language) {
			this.language = language;
			needCache = true;
		}

		public Privilege getPrivilege() {
			return priv;
		}

		public void setPrivilege(Privilege priv) {
			this.priv = priv;
			needCache = true;
		}

		public MemberData getMemberData() {
			return memberData;
		}

		public void setMemberData(MemberData memberData) {
			this.memberData = memberData;
			needCache = true;
		}
	}
}
