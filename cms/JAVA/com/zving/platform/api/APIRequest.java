package com.zving.platform.api;

import com.zving.framework.collection.Mapx;
import com.zving.framework.utility.StringUtil;

/**
 * 表示一次接口调用的请求
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-4-3
 */
public class APIRequest {
	String MethodID;
	String UserName;
	String Password;
	String ClientIP;
	String Language;
	String DataFormat;
	Mapx<String, Object> Params;

	public String getClientIP() {
		return ClientIP;
	}

	public void setClientIP(String clientIP) {
		ClientIP = clientIP;
	}

	public Mapx<String, Object> getParameters() {
		return Params;
	}

	public void setParameters(Mapx<String, Object> parameters) {
		Params = parameters;
	}

	public String getLanguage() {
		return Language;
	}

	public void setLanguage(String language) {
		Language = language;
	}

	public String getMethodID() {
		return MethodID;
	}

	public void setMethodID(String methodID) {
		MethodID = methodID;
	}

	public String getDataFormat() {
		return DataFormat;
	}

	public void setDataFormat(String dataFormat) {
		DataFormat = dataFormat;
	}

	public String getUserName() {
		return UserName;
	}

	public void setUserName(String userName) {
		UserName = userName;
	}

	public String getPassword() {
		return Password;
	}

	public void setPassword(String password) {
		Password = password;
	}

	public String hash() {
		StringBuilder sb = new StringBuilder();
		sb.append(MethodID);
		sb.append(UserName);
		sb.append(Password);
		// sb.append(ClientIP);
		sb.append(Language);
		sb.append(DataFormat);
		sb.append(Params.hashCode());
		return StringUtil.md5Hex(sb.toString());
	}
}
