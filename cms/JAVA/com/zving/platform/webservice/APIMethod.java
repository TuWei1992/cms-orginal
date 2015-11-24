package com.zving.platform.webservice;

import com.zving.platform.api.APIUtil;

/**
 * 本类供XFire发布成WSDL并接受WebService调用
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-5-12
 */
public class APIMethod {

	/**
	 * 调用API方法
	 * 
	 * @param methodID API方法ID
	 * @param APIUserName API用户名
	 * @param APIPassword API密码
	 * @param language 语言(多语言时会影响返回数据中的文本数据)
	 * @param dataFormat 返回数据格式（json或xml）
	 * @param params 参数，参数的格式要求和 dataFormat保持一致
	 * @return 调用结果，是APIResponse按dataFormat指定的格式输出的字符串
	 */
	public String invoke(String methodID, String APIUserName, String APIPassword, String language, String dataFormat, String parameters) {// NO_UCD
		try {
			return APIUtil.invoke(methodID, APIUserName, APIPassword, null, language, dataFormat, parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
