package com.zving.framework;

import com.zving.framework.core.Dispatcher;
import com.zving.framework.i18n.Lang;
import com.zving.framework.json.JSONArray;
import com.zving.framework.json.JSONObject;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 一个页面或模块中的所有后台方法的集合。<br>
 * 所有响应JavaScript中Server.sendRequest()方法的后台类都必须继承本类<br>
 * 
 * @author 王育春
 * @date 2009-11-15
 * @email wangyc@zving.com
 */
public abstract class UIFacade {
	/**
	 * 响应本次请求的数据容器，放在Response中的数据在JavaScript中可以用Response.get()获取到
	 */
	protected ResponseData Response;

	/**
	 * 本次请求的所有参数，包括URL和表单参数，以及部分Http头
	 */
	protected RequestData Request;

	/**
	 * 本次请求发送的所有Cookie
	 */
	protected CookieData Cookies;

	/**
	 * 从Current中初始化内置的Request,Response,Cookie的值。<br>
	 * （ 本方法通常用于从UI类A中调用UI类B中的方法时初始化B中的内置对象）
	 */
	public UIFacade() {
		Request = Current.getRequest();
		Cookies = Current.getCookies();
		Response = Current.getResponse();
		if (Response == null) {
			Response = new ResponseData();
		}
		if (Cookies == null) {
			Cookies = new CookieData();
		}
		if (Request == null) {
			Request = new RequestData();
		}
	}

	/**
	 * 从Request中获取一个字符串，如果Request中没有，则尝试从Response中获取
	 */
	public String $V(String id) {
		Object v = Request.get(id);
		if (ObjectUtil.empty(v)) {
			v = Response.get(id);
		}
		if (v == null) {
			return null;
		}
		if (v instanceof JSONArray) {
			return StringUtil.join((JSONArray) v);
		}
		return v.toString();
	}

	/**
	 * 设置Response中的变量，相当于Response.put()
	 */
	public UIFacade $S(String id, Object value) {
		Response.put(id, value);
		return this;
	}

	/**
	 * 获取当前响应数据
	 */
	public ResponseData getResponse() {
		return Response;
	}

	/**
	 * 获取Cookie数据
	 */
	public CookieData getCookies() {
		return Cookies;
	}

	/**
	 * URL重定向
	 * 
	 * @param url
	 */
	public void redirect(String url) {
		Dispatcher.redirect(url);
	}

	/**
	 * 默认的成功消息
	 */
	public void success() {
		success(Lang.get("Common.Success"));
	}

	/**
	 * 成功消息
	 */
	public void success(String message) {
		Response.setSuccessMessage(message);
	}

	/**
	 * 失败消息
	 * 
	 * @param message
	 */
	public void fail(String message) {
		Response.setFailedMessage(message);
	}

	/**
	 * 从Request中获取一个JSONArray对象
	 */
	public JSONArray $A(String id) {
		Object o = Request.get(id);
		if (o instanceof JSONArray) {
			return (JSONArray) o;
		}
		return null;
	}

	/**
	 * 从Request中获取一个JSONObject对象
	 */
	public JSONObject $O(String id) {// NO_UCD
		Object o = Request.get(id);
		if (o instanceof JSONObject) {
			return (JSONObject) o;
		}
		return null;
	}

	/**
	 * 从Request中获取一个long值，如果Request中没有，则尝试从Response中获取
	 */
	public long $L(String id) {
		if (Request.containsKey(id)) {
			return Request.getLong(id);
		}
		return Response.getLong(id);
	}

	/**
	 * 从Request中获取一个int值，如果Request中没有，则尝试从Response中获取
	 */
	public int $I(String id) {
		if (Request.containsKey(id)) {
			return Request.getInt(id);
		}
		return Response.getInt(id);
	}

	/**
	 * 从Request中获取一个float值，如果Request中没有，则尝试从Response中获取
	 */
	public float $F(String id) {
		if (Request.containsKey(id)) {
			return Request.getFloat(id);
		}
		return Response.getFloat(id);
	}

	/**
	 * 从Request中获取一个double值，如果Request中没有，则尝试从Response中获取
	 */
	public double $D(String id) {
		if (Request.containsKey(id)) {
			return Request.getDouble(id);
		}
		return Response.getDouble(id);
	}

	/**
	 * 从Request中获取一个boolean值，如果Request中没有，则尝试从Response中获取
	 */
	public boolean $B(String id) {
		if (Request.containsKey(id)) {
			return Request.getBoolean(id);
		}
		return Response.getBoolean(id);
	}
}
