package com.zving.framework;

import javax.servlet.http.HttpServletResponse;

import com.zving.framework.collection.CaseIgnoreMapx;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataCollection;

/**
 * 封装一次http请求中的响应数据，可以在JAVA代码中直接创建实例。
 * 
 * @Author 王育春
 * @Date 2007-6-19
 * @Mail wyuch@zving.com
 */
public class ResponseData extends DataCollection {
	private static final long serialVersionUID = 1L;
	/**
	 * 成功状态
	 */
	public static final int SUCCESS = 1;
	/**
	 * 失败状态
	 */
	public static final int FAILED = 0;
	/**
	 * HTTP头，不区分大消息
	 */
	private Mapx<String, String> headers = new CaseIgnoreMapx<String, String>();

	protected HttpServletResponse servletResponse;

	private int status = SUCCESS;

	private String message = "";

	/**
	 * 获取本次后台方法返回给JavaScript的消息
	 * 
	 * @return
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * 设置本次后台方法返回给JavaScript的消息，并将状态设为0
	 */
	public void setFailedMessage(String message) {
		setStatusAndMessage(FAILED, message);
	}

	@Deprecated
	public void setError(String message) {
		setFailedMessage(message);
	}

	/**
	 * 设置本次后台方法返回给JavaScript的消息
	 */
	@Deprecated
	public void setMessage(String message) {
		setSuccessMessage(message);
	}

	/**
	 * 将状态置为成功，并设置响应消息
	 */
	public void setSuccessMessage(String message) {
		setStatusAndMessage(SUCCESS, message);
	}

	/**
	 * 获取本次后台方法返回给JavaScript的状态码，一般情况下0表示执行异常，1表示执行成功
	 * 
	 * @return
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * 设置本次后台方法返回给JavaScript的状态码
	 * 
	 * @param status
	 */
	public void setStatus(int status) {
		this.status = status;
		put(Constant.ResponseStatusAttrName, status);
	}

	/**
	 * 设置本次后台方法返回给JavaScript的消息及状态码
	 * 
	 * @param status
	 * @param message
	 */
	public void setStatusAndMessage(int status, String message) {
		this.status = status;
		put(Constant.ResponseStatusAttrName, status);
		this.message = message;
		put(Constant.ResponseMessageAttrName, message);
	}

	@Deprecated
	public void setLogInfo(int status, String message) {
		setStatusAndMessage(status, message);
	}

	/**
	 * 将ResponseImpl转化成XML
	 * 
	 * @see com.zving.framework.data.DataCollection#toXML()
	 */
	@Override
	public String toXML() {
		put(Constant.ResponseStatusAttrName, status);
		return super.toXML();
	}

	/**
	 * 将ResponseImpl转化成XML
	 * 
	 * @see com.zving.framework.data.DataCollection#toJSON()
	 */
	@Override
	public String toJSON() {
		put(Constant.ResponseStatusAttrName, status);
		return super.toJSON();
	}

	/**
	 * 获取设置过的HTTP头
	 * 
	 * @return
	 */
	public Mapx<String, String> getHeaders() {
		return headers;
	}

	/**
	 * 设置HTTP头
	 * 
	 * @param headers
	 */
	public void setHeader(String name, String value) {
		headers.put(name, value);
		if (servletResponse != null) {
			servletResponse.setHeader(name, value);
		}
	}

	/**
	 * 清除数据以便复用
	 */
	@Override
	public void clear() {
		if (headers.getEntryTableLength() > 64) {
			headers = new CaseIgnoreMapx<String, String>();
		} else {
			headers.clear();
		}
		servletResponse = null;
		status = SUCCESS;
		message = "";
		super.clear();
	}

	protected void setServletResponse(HttpServletResponse servletResponse) {
		this.servletResponse = servletResponse;
	}

}
