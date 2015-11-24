package com.zving.framework;

import java.io.Serializable;
import java.util.Map;

import com.zving.framework.User.UserData;
import com.zving.framework.collection.Mapx;

/**
 * 存取前台会员用户的会话信息，实际数据存储在User对象中。
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-7-5
 */
public class Member {

	public static class MemberData extends Mapx<String, Object> implements Serializable {
		private static final long serialVersionUID = 1L;
		/**
		 * 唯一ID
		 */
		public long UID;
		/**
		 * 用户名
		 */
		public String UserName;
		/**
		 * 是否已经登录
		 */
		public boolean isLogin;
		/**
		 * 真实姓名
		 */
		public String RealName;
		/**
		 * 会员类型
		 */
		public String Type;
	}

	/**
	 * @return 当前会员的UID
	 */
	public static long getUID() {
		MemberData md = getCurrent();
		return md == null ? null : md.UID;
	}

	/**
	 * 设置当前会员的UID
	 * 
	 * @param uid
	 */
	public static void setUID(long uid) {
		getCurrent(true).UID = uid;
	}

	/**
	 * 获得会员用户名
	 */
	public static String getUserName() {
		MemberData md = getCurrent();
		return md == null ? null : md.UserName;
	}

	/**
	 * 设置会员用户名
	 * 
	 * @param username
	 */
	public static void setUserName(String username) {
		getCurrent(true).UserName = username;
	}

	/**
	 * 获取会员真实姓名
	 * 
	 * @return
	 */
	public static String getRealName() {
		MemberData md = getCurrent();
		return md == null ? null : md.RealName;
	}

	/**
	 * 设置会员真实姓名
	 * 
	 * @param realName
	 */
	public static void setRealName(String realName) {
		getCurrent(true).RealName = realName;
	}

	/**
	 * 获取会员类型
	 * 
	 * @return
	 */
	public static String getType() {
		MemberData md = getCurrent();
		return md == null ? null : md.Type;
	}

	/**
	 * 设置会员类型
	 * 
	 * @param type
	 */
	public static void setType(String type) {
		getCurrent(true).Type = type;
	}

	/**
	 * 是否已经登录
	 * 
	 * @return
	 */
	public static boolean isLogin() {
		MemberData md = getCurrent();
		return md == null ? null : md.isLogin;
	}

	/**
	 * 设置登录状态
	 * 
	 * @param flag
	 */
	public static void setLogin(boolean flag) {
		getCurrent(true).isLogin = flag;
	}

	/**
	 * 获取属性值
	 * 
	 * @param key
	 * @return
	 */
	public static Object getValue(String key) {
		MemberData md = getCurrent();
		return md == null ? null : md.get(key);
	}

	/**
	 * 设置属性值
	 * 
	 * @param key
	 * @param value
	 */
	public static void setValue(String key, Object value) {
		getCurrent(true).put(key, value);
	}

	/**
	 * 获取全部属性
	 * 
	 * @return
	 */
	public static Map<String, Object> getValues() {// NO_UCD
		return getCurrent();
	}

	/**
	 * 销毁
	 */
	public static void destory() {
		User.setMemberData(new MemberData());
	}

	/**
	 * 获取当前会员数据
	 */
	public static MemberData getCurrent() {
		return getCurrent(false);
	}

	// 此时如果Current中没有值且create=true，则置一个MemberData到Current
	protected static MemberData getCurrent(boolean create) {
		UserData ud = User.getCurrent(create);
		MemberData md = ud.getMemberData();
		if (md == null) {
			md = new MemberData();
			ud.setMemberData(md);
		}
		return md;
	}
}
