package com.zving.framework.misc;

import java.util.HashMap;

import com.zving.framework.Constant;
import com.zving.framework.RequestData;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Priv;
import com.zving.framework.data.DataCollection;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ServletUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 框架内置一些UI方法
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2007-5-10
 */
public class FrameworkUI extends UIFacade {

	/**
	 * 用于调试模式下收集前台页面的在浏览器端耗费的时间
	 */
	@Priv(login = false)
	public void sendPageCost() {
		String url = $V("URL");
		url = url.substring(url.indexOf("/", 8));
		int cost = Integer.parseInt($V("Cost"));
		int readyCost = Integer.parseInt($V("ReadyCost"));
		if (cost > 100) {
			LogUtil.info("ClientCost\tReady=" + $V("ReadyCost") + "ms" + (readyCost > 1000 ? "!" : "") + "\tLoad=" + $V("Cost") + "ms"
					+ (cost > 3000 ? "!" : "") + "\t" + url);
		}
	}

	/**
	 * 调用远程应用上的UI方法
	 * 
	 * @param url 远程应用根地址
	 * @param method UI方法别名
	 * @param request 请求参数
	 * @return 请求结果
	 */
	public static DataCollection callRemoteMethod(String url, String method, RequestData request) {
		if (!url.endsWith("/")) {
			url = StringUtil.concat(url, "/");
		}
		if (!url.startsWith("http://")) {
			url = StringUtil.concat("http://", url);
		}
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(Constant.Method, method);
		params.put(Constant.Data, request.toJSON());
		params.put(Constant.URL, url);
		params.put(Constant.DataFormat, "json");
		url = url + "ajax/invoke";
		String result = ServletUtil.postURLContent(url, params, "UTF-8");
		if (result != null) {
			DataCollection dc = new DataCollection();
			dc.parseJSON(result);
			return dc;
		}
		LogUtil.warn("Framework.callRemoteMethod() error,URL=" + url);
		return null;
	}
}
