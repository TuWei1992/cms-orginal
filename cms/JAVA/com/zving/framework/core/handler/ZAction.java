package com.zving.framework.core.handler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zving.framework.Config;
import com.zving.framework.Constant;
import com.zving.framework.CookieData;
import com.zving.framework.core.Dispatcher;
import com.zving.framework.utility.StringUtil;

/**
 * 对应前台的一次表单提交或者一次URL访问的封装
 * 
 * @Author 王育春
 * @Date 2010-11-22
 * @Mail wyuch@zving.com
 */
public class ZAction {
	private StringBuilder sb;
	private boolean binaryMode;
	private boolean closeStringBuilder = false;
	private CookieData cookies;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private String contentType;

	public CookieData getCookies() {
		return cookies;
	}

	public void setCookies(CookieData cookies) {
		this.cookies = cookies;
	}

	public void forward(String forwardURL) {// NO_UCD
		Dispatcher.forward(forwardURL);
	}

	public void redirect(String redirectURL) {
		Dispatcher.redirect(redirectURL);
	}

	public boolean isBinaryMode() {
		return binaryMode;
	}

	public void setBinaryMode(boolean flag) {
		binaryMode = flag;
	}

	public void writeHTML(String html) {
		if (!binaryMode) {
			contentType = "text/html";
			write(html);
		} else {
			throw new RuntimeException("Can't invoke writeHTML in binary mode!");
		}
	}

	private void write(String str) {
		if (sb == null) {
			sb = new StringBuilder();
		}
		if (sb.length() == 0) {
			closeStringBuilder = false;
		}
		if (closeStringBuilder && sb.length() > 0) {
			throw new RuntimeException("Can't append string before clear StringBuilder!");
		}
		sb.append(str);
	}

	public void writeJS(String js) {
		if (!binaryMode) {
			setContentType("text/javascript");
			write(js);
		} else {
			throw new RuntimeException("Can't invoke writeJS in binary mode!");
		}
	}

	// 根据请求返回四种格式的数据，并且禁止再向sb写入内容
	// resultDataFormat可能为以下几个值之一
	// json,jsonp,html,xml,text,script,setWindowName,postWindowMessage
	public void end(String str) {
		if (sb == null) {
			sb = new StringBuilder();
		}
		String method = request.getMethod();
		String resultDataFormat = request.getParameter(Constant.DataFormat);
		String callback = request.getParameter("callback");
		// 返回简单JSONP
		if (method.equals("GET") && StringUtil.isNotEmpty(callback)) {
			contentType = "text/javascript";
			sb.append(callback);
			sb.append("(");
			sb.append(str);
			sb.append(");");
		}
		// 返回修改window.name的html片断
		else if (resultDataFormat != null) {
			if (resultDataFormat.equalsIgnoreCase("setWindowName")) {
				contentType = "text/html";
				sb.append("<html><head>");
				sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=");
				sb.append(Config.getGlobalCharset());
				sb.append("\">");
				sb.append("</head><body>");
				sb.append("<script id=\"jsonData\" type=\"text/json\">");
				sb.append(str);
				sb.append("</script>");
				sb.append("<script>window.name=document.getElementById('jsonData').innerHTML;</script>");
				sb.append("</body></html>");
			}
			// JSON数据
			else if (resultDataFormat.equals("json")) {
				contentType = "application/json";
				sb.append(str);
			}
			// 普通HTML片断
			else {
				contentType = "text/html";
				sb.append(str);
			}
		} else {
			contentType = "application/json";
			sb.append(str);
		}
		closeStringBuilder = true;
	}

	/**
	 * 跨域返回
	 * 
	 * @param json
	 */
	public void jsonp(String json) {
		if (sb == null) {
			sb = new StringBuilder();
		}
		String callback = request.getParameter("callback");
		if (StringUtil.isNotEmpty(callback)) {
			contentType = "text/javascript";
			sb.delete(0, sb.length());
			sb.append(callback);
			sb.append("(");
			sb.append(json);
			sb.append(");");
		}
	}

	@Deprecated
	public String getHTML() {
		return getContent();
	}

	public String getContent() {
		return sb == null ? "" : sb.toString();
	}

	public void writeByte(byte[] arr) {
		if (binaryMode) {
			try {
				response.getOutputStream().write(arr);
				response.getOutputStream().flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			throw new RuntimeException("Can't invoke writeByte in html mode!");
		}
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setSkipPage(boolean skip) {
		request.setAttribute("ZACTION_SKIPPAGE", String.valueOf(skip));
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
		this.response.setContentType(contentType);
	}
}
