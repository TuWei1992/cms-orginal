package com.zving.framework.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zving.framework.Config;
import com.zving.framework.CookieData;
import com.zving.framework.Current;
import com.zving.framework.RequestData;
import com.zving.framework.collection.Mapx;
import com.zving.framework.json.JSON;

/**
 * Servlet/http工具类
 * 
 * @Author 王育春
 * @Date 2006-12-3
 * @Mail wyuch@midding.com
 */
public class ServletUtil {
	/**
	 * <p>
	 * 获取HttpServletRequest中所有的Parameter及Attribute以Mapx<String,String>的方式返回
	 * </p>
	 * 注意：
	 * <ul>
	 * <li>如果Attribute和Parameter包含相同Name的值，只会得到Attribute中数据值</li>
	 * <li>如果一个Name下有一组值，则将会使用','对这组值进行分隔并拼接为一个字符串</li>
	 * </ul>
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Mapx<String, String> getParameterMap(HttpServletRequest request) {
		Mapx<String, String> map = new Mapx<String, String>();
		Map<String, String[]> tmap = request.getParameterMap();
		boolean tryDecode = request.getMethod().equals("GET") && request.getQueryString() != null
				&& request.getQueryString().indexOf('%') > 0;
		String charset = null;
		if (tryDecode) {
			charset = request.getCharacterEncoding();
		}
		for (String key : tmap.keySet()) {
			Object value = tmap.get(key);
			map.put(key, getParameterValue(key, value, charset));
		}
		Enumeration<String> e = request.getAttributeNames();
		while (e.hasMoreElements()) {
			String key = e.nextElement();
			Object value = request.getAttribute(key);
			map.put(key, getParameterValue(key, value, charset));
		}
		return map;
	}

	private static String getParameterValue(String key, Object value, String charset) {
		if (value == null) {
			return null;
		}
		if (!value.getClass().isArray()) {
			return String.valueOf(value);
		} else {
			Object[] arr = ObjectUtil.toObjectArray(value);
			if (arr.length == 1) {
				String s = String.valueOf(arr[0]);
				if (charset != null) {
					s = tryDecodeParameterValue(key, s, charset);
				}
				return s;
			} else {
				if (charset != null) {
					for (int i = 0; i < arr.length; i++) {
						arr[i] = tryDecodeParameterValue(key, (String) arr[i], charset);
					}
				}
				return StringUtil.join(arr);
			}
		}
	}

	private static String tryDecodeParameterValue(String key, String value, String charset) {
		try {
			return new String(value.getBytes("iso8859-1"), charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * 获取URL中QueryString部分提交的值(注意：如果有相同name的值有多个则只会保留最后一个)
	 * 
	 * @param url
	 * @return
	 */
	public static Mapx<String, String> getMapFromQueryString(String url) {
		Mapx<String, String> map = new Mapx<String, String>();
		int index = url.indexOf('?');
		if (index < 0) {
			return map;
		} else {
			String str = url.substring(url.indexOf('?') + 1);
			int index2 = str.lastIndexOf('#');
			if (index2 != -1) {
				str = str.substring(0, index2);
			}
			String[] arr = str.split("\\&");
			for (String element : arr) {
				String[] arr2 = element.split("\\=");
				if (arr2.length == 2) {
					map.put(arr2[0], arr2[1]);
				} else if (arr2.length > 0) {
					map.put(arr2[0], "");
				}
			}
			return map;
		}
	}

	/**
	 * 将Map转换为QueryString的字符串表现形式(默认Map中的value不采用urlEncode编码)
	 * 
	 * @param map 要转换的Map
	 * @return Map的QueryString的字符串表现形式
	 */
	public static String getQueryStringFromMap(Mapx<?, ?> map) {
		return getQueryStringFromMap(map, false);
	}

	/**
	 * 将Map转换为QueryString的字符串表现形式
	 * 
	 * @param map 要转换的Map
	 * @param encodeFlag Map中的value是否采用urlEncode编码
	 * @return Map的QueryString的字符串表现形式
	 */
	public static String getQueryStringFromMap(Mapx<?, ?> map, boolean encodeFlag) {
		FastStringBuilder sb = new FastStringBuilder();
		sb.append("?");
		boolean first = true;
		for (Object k : map.keySet()) {
			Object v = map.get(k);
			if (v == null) {
				continue;
			}
			if (!first) {
				sb.append("&");
			}
			sb.append(k);
			sb.append("=");
			if (encodeFlag) {
				sb.append(StringUtil.urlEncode(v.toString()));
			} else {
				sb.append(v.toString());
			}
			first = false;
		}
		return sb.toStringAndClose();
	}

	/**
	 * 读取URL资源,并以指定编码解析为文本
	 * 
	 * @param url 要读取的URL
	 * @param encoding 将数据解析为文本时使用的编码
	 * @throws Exception 当流操作异常时将会抛出异常
	 */
	public static String getURLContent(String url, String encoding) throws Exception {
		if (url.substring(0, 8).equals("https://")) {
			return ServletUtil.getHttpsURLContent(url, "GET", null, encoding);
		}
		FastStringBuilder sb = new FastStringBuilder();
		InputStreamReader isr = null;
		if (StringUtil.isNotEmpty(encoding)) {
			isr = new InputStreamReader(new URL(url).openStream(), encoding);
		} else {
			isr = new InputStreamReader(new URL(url).openStream());
		}
		BufferedReader br = new BufferedReader(isr);
		String s = null;
		while ((s = br.readLine()) != null) {
			sb.append(s);
			sb.append("\n");
		}
		br.close();
		return sb.toStringAndClose();
	}

	/**
	 * 通过https方式获取数据
	 * 
	 * @param url 要读取的URL
	 * @param method 使用POST或GET
	 * @param postData POST提交的数据
	 * @param encoding 编码
	 */
	public static String getHttpsURLContent(String url, String method, String postData, String encoding) {
		try {
			return getHttpsURLContentNoCatch(url, method, postData, encoding);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 通过https方式获取数据(未对异常进行处理)
	 * 
	 * @param url 要读取的URL
	 * @param method 使用POST或GET
	 * @param postData POST提交的数据
	 * @param encoding 编码
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public static String getHttpsURLContentNoCatch(String url, String method, String postData, String encoding)
			throws GeneralSecurityException, IOException {
		FastStringBuilder buffer = new FastStringBuilder();
		HttpsURLConnection conn = null;
		try {
			TrustManager[] tm = { new EmptyX509TrustManager() };
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
			SSLSocketFactory ssf = sslContext.getSocketFactory();
			URL _url = new URL(url);
			conn = (HttpsURLConnection) _url.openConnection();
			conn.setSSLSocketFactory(ssf);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod(method);
			if ("GET".equalsIgnoreCase(method)) {
				conn.connect();
			}
			if (ObjectUtil.notEmpty(postData)) {
				OutputStream outputStream = conn.getOutputStream();
				outputStream.write(postData.getBytes(encoding));
				outputStream.close();
			}

			// 将返回的输入流转换成字符串
			InputStream inputStream = conn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, encoding);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			bufferedReader.close();
			inputStreamReader.close();
			// 释放资源
			inputStream.close();
			inputStream = null;
			return buffer.toString();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	/**
	 * 使用Post发送http请求
	 */
	public static String postURLContent(String url, Map<String, String> params, String encoding) {
		if (url.substring(0, 8).equals("https://")) {
			return ServletUtil.getHttpsURLContent(url, "POST", JSON.toJSONString(params), encoding);
		}
		StringBuilder sb = null;
		String result = null;
		HttpURLConnection conn = null;
		try {
			for (Entry<String, String> entry : params.entrySet()) {
				if (sb == null) {
					sb = new StringBuilder();
				} else {
					sb.append('&');
				}
				sb.append(entry.getKey()).append('=');
				sb.append(URLEncoder.encode(entry.getValue(), encoding));
			}
			byte[] entity = sb.toString().getBytes(encoding);
			URL _url = new URL(url);
			conn = (HttpURLConnection) _url.openConnection();
			conn.setConnectTimeout(5 * 1000);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setDoOutput(true);// 允许输出数据
			conn.setDoInput(true);
			conn.connect();
			OutputStream outStream = conn.getOutputStream();
			outStream.write(entity);
			outStream.flush();
			outStream.close();
			if (conn.getResponseCode() == 200) {
				InputStream is = conn.getInputStream();
				result = FileUtil.readText(is, encoding);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return result;
	}

	/**
	 * 获取request中的Cookie值
	 * 
	 * @param request
	 * @param cookieName
	 * @return
	 */
	public static String getCookieValue(HttpServletRequest request, String cookieName) {
		return getCookieValue(request, cookieName, "");
	}

	public static String getCookieValue(HttpServletRequest request, String cookieName, String defaultValue) {
		Cookie[] cookies = request.getCookies();
		for (int i = 0; cookies != null && i < cookies.length; i++) {
			Cookie cookie = cookies[i];
			if (cookieName.equalsIgnoreCase(cookie.getName())) {
				try {
					return URLDecoder.decode(cookie.getValue(), CookieData.CookieCharset);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					return defaultValue;
				}
			}
		}
		return defaultValue;
	}

	/**
	 * 设置Cookie
	 * 
	 * @param request Request
	 * @param response Response
	 * @param cookieName CookieName
	 * @param maxAge Cookie的有效期(单位：秒,如果为负数则在浏览器进程有效、如果为0则删除Cookie)
	 * @param cValue Cookie值
	 */
	public static void setCookieValue(HttpServletRequest request, HttpServletResponse response, String cookieName, int maxAge, String cValue) {
		Cookie[] cookies = request.getCookies();
		boolean cookieexistflag = false;
		String contextPath = Config.getContextPath();
		contextPath = contextPath.substring(0, contextPath.length() - 1);
		try {
			cValue = URLEncoder.encode(cValue, CookieData.CookieCharset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		for (int i = 0; cookies != null && i < cookies.length; i++) {
			Cookie cookie = cookies[i];
			if (cookieName.equalsIgnoreCase(cookie.getName())) {
				cookieexistflag = true;
				cookie.setValue(cValue);
				cookie.setPath(contextPath);
				cookie.setMaxAge(maxAge);
				response.addCookie(cookie);
			}
		}
		if (!cookieexistflag) {
			Cookie cookie = new Cookie(cookieName, cValue);
			cookie.setPath(contextPath);
			cookie.setMaxAge(maxAge);
			response.addCookie(cookie);
		}
	}

	/**
	 * 返回URL中的文件扩展名。文件扩展名带有"."
	 */
	public static String getUrlExtension(String url) {
		if (StringUtil.isEmpty(url)) {
			return "";
		}
		int index = url.indexOf('?');
		if (index > 0) {
			url = url.substring(0, index);
		}
		int i1 = url.lastIndexOf('/');
		int i2 = url.lastIndexOf('.');
		if (i1 >= i2) {
			return "";
		} else {
			return url.substring(i2).toLowerCase();
		}
	}

	public static String getFileName(String url) {
		int last = 0;
		boolean colonFlag = false;
		String fileName = url;
		for (int i = 0; i < url.length(); i++) {
			char c = url.charAt(i);
			if (c == '?' || c == '#') {
				fileName = url.substring(0, i);
				break;
			} else if (c == '/') {
				last = i;
			} else if (c == ':') {
				colonFlag = true;
			}
		}
		if (colonFlag && (fileName.startsWith("http://") || fileName.startsWith("https://"))) {
			if (last < 8) {// URL直接以域名结束
				fileName = "";
			} else {
				fileName = fileName.substring(last + 1);
			}
		} else if (fileName.startsWith("/")) {
			fileName = fileName.substring(1);
		}
		fileName = FileUtil.normalizePath(fileName);
		return fileName;
	}

	/**
	 * 获取URL中的Host部分(仅支持http和https的URL)
	 * 
	 * @param url
	 * @return
	 */
	public static String getHost(String url) {
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			LogUtil.error("Invalid URL：" + url);
			return null;
		}
		int index = url.indexOf("//") + 2;
		int index2 = url.indexOf("/", index);
		if (index2 <= 0) {
			index2 = url.length();
		}
		return url.substring(index, index2);
	}

	/**
	 * 尝试读取HttpServletRequest的真实来源IP
	 */
	public static String getRealIP(HttpServletRequest request) {
		String ip = request.getHeader("Nginx-Forwarded-For");
		if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("X-Forwarded-For");
		}
		if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("X-Real-IP");
		}
		if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (StringUtil.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
			/**
			 * 获取到的地址可能包含多个IP, 第一个IP为访问者的初始IP,有可能为内网IP
			 * 检测并取IP中的第一个非保留IP为访问者的外网IP
			 */
			return IPUtil.checkFirstInternetIP(ip);
		} else {
			return request.getRemoteAddr();
		}
	}

	/**
	 * 解决因浏览器编码不正确导致得到的参数是问号的问题
	 */
	public static String getChineseParameter(String queryString, String key) {
		if (queryString == null) {
			return null;
		}
		Mapx<String, String> map = StringUtil.splitToMapx(queryString, "&", "=");
		String v = map.getString(key);
		if (v != null) {
			v = StringUtil.replaceEx(v, "+", "%20");
			if (v.indexOf('%') >= 0) {
				if (v.indexOf("%u") >= 0) {
					v = StringUtil.unescape(v);
				} else {
					// 必须逐字判断，有可能有未编码的字符和编码的字符混排的情况，例如<script>alert(1);</script>
					FastStringBuilder sb = new FastStringBuilder();
					int escapeIndex = -1;
					for (int i = 0; i < v.length(); i++) {
						char c = v.charAt(i);
						if (c == '%') {
							escapeIndex = i;
						} else {
							if (escapeIndex >= 0 && i - escapeIndex == 3) {
								if (c == '%') {
									escapeIndex = i;
								} else {
									escapeIndex = -1;
								}
							}
						}
						if (escapeIndex < 0) {
							sb.append('%').append(StringUtil.hexEncode(new byte[] { (byte) c }));
						} else {
							sb.append(c);
						}
					}
					v = sb.toStringAndClose();
					byte[] bs = StringUtil.hexDecode(StringUtil.replaceEx(v, "%", ""));
					try {
						String result = null;
						if (bs.length >= 3 && StringUtil.isUTF8(bs)) {
							result = new String(bs, "UTF-8");
							if (result.indexOf('?') >= 0) {
								result = new String(bs, "GBK");
							}
						} else {
							result = new String(bs, "GBK");
							if (result.indexOf('?') >= 0) {
								result = new String(bs, "UTF-8");
							}
						}
						v = result;
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return v;
	}

	public static String getChineseParameter(String key) {
		RequestData req = Current.getRequest();
		if (req != null) {
			String v = getChineseParameter(req.getQueryString(), key);
			if (v == null) {
				v = req.getString(key);
			}
			if (v == null) {
				v = Current.getResponse().getString(key);
			}
			return v;
		}
		return null;
	}

	/**
	 * 内部类，供https连接使用
	 */
	public static class EmptyX509TrustManager implements X509TrustManager {
		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}

}
