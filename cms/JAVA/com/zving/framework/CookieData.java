package com.zving.framework;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zving.framework.CookieData.CookieObject;
import com.zving.framework.utility.LogUtil;

/**
 * 对Servlet中的Cookie对象的封装
 * 
 * @Author 兰军
 * @Date 2008-12-31
 * @Mail lanjun@zving.com
 */
public class CookieData extends ArrayList<CookieObject> {
	private static final long serialVersionUID = 1L;

	public static final String CookieCharset = "UTF-8";

	/**
	 * 构造一个空的Cookie对象
	 */
	public CookieData() {

	}

	/**
	 * 从HttpServletRequest中建立一个Cookie对象
	 */
	public CookieData(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return;
		}
		for (Cookie cookie : cookies) {
			CookieObject c = new CookieObject();
			c.name = cookie.getName();
			try {
				c.value = URLDecoder.decode(cookie.getValue(), CookieCharset);
			} catch (Exception e) {
				LogUtil.warn("Invalide cookie value:" + cookie.getValue());
			}
			c.domain = cookie.getDomain();
			c.maxAge = cookie.getMaxAge();
			c.path = cookie.getPath();
			c.path = normalizePath(c.path);
			c.secure = cookie.getSecure();
			c.comment = cookie.getComment();
			c.version = cookie.getVersion();
			add(c);
		}
	}

	/**
	 * 设置Cookie的值
	 * 
	 * @param name Cookie名称
	 * @param value Cookie值
	 * @param domain 所属域
	 * @param maxAge 最大存活时长
	 * @param path 所属路径
	 * @param secure 是否是https才能存取
	 * @param comment Cookie注释
	 */
	public void setCookie(String name, String value, String domain, int maxAge, String path, boolean secure, String comment) {
		path = normalizePath(path);
		for (int i = 0; i < size(); i++) {
			CookieObject c = get(i);
			if (c.name.equals(name) && (c.domain == null || c.domain.equals(domain)) && (c.path == null || c.path.equals(path))
					&& c.secure == secure) {
				if (c.value != null && !c.value.equals(value)) {
					c.changed = true;
					c.value = value;
					c.comment = comment;
					c.maxAge = maxAge;
					write();
					return;
				}
			}
		}
		CookieObject c = new CookieObject();
		c.name = name;
		c.value = value;
		c.comment = comment;
		c.domain = domain;
		c.maxAge = maxAge;
		c.path = path;
		c.secure = secure;
		c.version = 0;
		c.changed = true;
		add(c);
		write();
	}

	/**
	 * @return 所有的Cookie，如果多级路径下存在同名Cookie，则会返回两个实例
	 */
	public CookieObject[] getArray() {
		CookieObject[] cos = new CookieObject[size()];
		for (int i = 0; i < cos.length; i++) {
			cos[i] = get(i);
		}
		return cos;
	}

	/**
	 * 设置Cookie的值
	 * 
	 * @param name Cookie名称
	 * @param value Cookie值
	 * @param maxAge 最大存活时长
	 */
	public void setCookie(String name, String value, int maxAge) {
		setCookie(name, value, null, maxAge, getDefaultPath(), false, null);
	}

	/**
	 * 设置Cookie的值
	 * 
	 * @param name Cookie名称
	 * @param value Cookie值
	 */
	public void setCookie(String name, String value) {
		setCookie(name, value, null, Integer.MAX_VALUE, getDefaultPath(), false, null);
	}

	/**
	 * 设置Cookie的值
	 * 
	 * @param name Cookie名称
	 * @param value Cookie值
	 * @param path 所属路径
	 */
	public void setCookie(String name, String value, String path) {
		setCookie(name, value, null, Integer.MAX_VALUE, path, false, null);
	}

	/**
	 * @param name Cookie名称
	 * @return Cookie的值
	 */
	public String getCookie(String name) {
		return getCookie(name, null);
	}

	/**
	 * @return Cookie默认所属路径
	 */
	private static String getDefaultPath() {
		String path = Config.getContextPath();
		return normalizePath(path);
	}

	/**
	 * 重整Cookie中的path，修正双重斜杠之类的不规范的写法
	 */
	private static String normalizePath(String path) {
		if (path == null) {
			path = "/";
		}
		if (!path.endsWith("/")) {
			path += "/";
		}
		return path;
	}

	/**
	 * @param name Cookie名称
	 * @param path 所属路径
	 * @return Cookie值
	 */
	public String getCookie(String name, String path) {
		path = normalizePath(path);
		ArrayList<String> arr = new ArrayList<String>();
		for (int i = 0; i < size(); i++) {
			CookieObject c = get(i);
			if (c.name.equals(name)) {
				if (path != null) {
					if (c.path.equals(path)) {
						arr.add(c.value);
					}
				} else {
					arr.add(c.value);
				}
			}
		}
		if (arr.size() == 0) {
			return null;
		}
		if (arr.size() == 1) {
			return arr.get(0) == null ? null : String.valueOf(arr.get(0));
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arr.size(); i++) {
			if (i != 0) {
				sb.append(",");
			}
			if (arr.get(i) == null) {
				sb.append("");
			} else {
				sb.append(arr.get(i));
			}
		}
		return sb.toString();
	}

	/**
	 * 将有改动的Cookie的值写入到当前的HttpServletResponse
	 */
	private void write() {
		HttpServletResponse response = null;
		if (Current.getResponse() != null) {
			response = Current.getResponse().servletResponse;
		}
		if (response == null) {
			return;
		}
		try {
			for (int j = 0; j < size(); j++) {
				CookieObject co = get(j);
				if (!co.changed) {
					continue;
				}
				String v = "";
				if (co.value != null) {
					v = URLEncoder.encode(co.value, CookieCharset);
				}
				Cookie cookie = new Cookie(co.name, v);
				cookie.setMaxAge(co.maxAge);
				cookie.setPath(co.path);
				if (co.domain != null) {
					cookie.setDomain(co.domain);
				}
				cookie.setSecure(co.secure);
				response.addCookie(cookie);
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (CookieObject c : this) {
			sb.append(c.name);
			sb.append("=");
			sb.append(c.value);
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * 表示一个Cookie名值对
	 * 
	 * @author 王育春
	 * @date 2009-11-15
	 * @email wangyc@zving.com
	 */
	public static class CookieObject {
		/**
		 * Cookie名称
		 */
		public String name;

		/**
		 * Cookie值
		 */
		public String value;

		/**
		 * Cookie备注
		 */
		public String comment;

		/**
		 * Cookie所在域
		 */
		public String domain;

		/**
		 * Cookie存活时长
		 */
		public int maxAge = -1;

		/**
		 * Cookie路径
		 */
		public String path;

		/**
		 * 是不是SSL下的Cookie
		 */
		public boolean secure;

		/**
		 * Cookie版本
		 */
		public int version = 0;

		/**
		 * 是否修改过
		 */
		public boolean changed = false;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

		public String getDomain() {
			return domain;
		}

		public void setDomain(String domain) {
			this.domain = domain;
		}

		public int getMaxAge() {
			return maxAge;
		}

		public void setMaxAge(int maxAge) {
			this.maxAge = maxAge;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public boolean isSecure() {
			return secure;
		}

		public void setSecure(boolean secure) {
			this.secure = secure;
		}

		public int getVersion() {
			return version;
		}

		public void setVersion(int version) {
			this.version = version;
		}

		public boolean isChanged() {
			return changed;
		}

		public void setChanged(boolean changed) {
			this.changed = changed;
		}
	}
}
