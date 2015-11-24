package com.zving.framework.ui.tag;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.BodyTag;

import com.zving.framework.core.handler.ActionHandler;
import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.extend.plugin.PluginManager;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.ui.zhtml.ZhtmlExecuteContext;
import com.zving.framework.utility.ObjectUtil;

/**
 * 在页面中调用ZAction方法　
 * 
 * @Author 王育春
 * @Date 2007-6-23
 * @Mail wyuch@zving.com
 */
public class ActionTag extends AbstractTag {
	private String method;

	public String getMethod() {
		return method;
	}

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "action";
	}

	public void setMethod(String method) {
		this.method = method;
	}

	@Override
	public int doStartTag() throws TemplateRuntimeException {
		try {
			if (pageContext instanceof ZhtmlExecuteContext) {
				ZhtmlExecuteContext httpContext = (ZhtmlExecuteContext) pageContext;
				HttpServletRequest request = httpContext.getRequest();
				if (ObjectUtil.notEmpty(method)) {
					String requestURI = request.getRequestURI();
					String context = request.getContextPath();
					String url = requestURI.substring(context.length(), requestURI.length());// 以/开头
					ActionHandler up = (ActionHandler) PluginManager.getInstance().getPluginConfig(FrameworkPlugin.ID).getExtendItems()
							.get(ActionHandler.ID).getInstance();
					up.handle(url, request, httpContext.getResponse());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return BodyTag.EVAL_BODY_BUFFERED;
	}

	@Override
	public int doEndTag() throws TemplateRuntimeException {
		if (pageContext instanceof ZhtmlExecuteContext) {
			ZhtmlExecuteContext httpContext = (ZhtmlExecuteContext) pageContext;
			HttpServletRequest request = httpContext.getRequest();
			if (ObjectUtil.equal("true", request.getAttribute("ZACTION_SKIPPAGE"))) {
				return SKIP_PAGE;
			}
		}
		return EVAL_PAGE;

	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("method", true));
		return list;
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.Tag.ActionTagName}";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public String getPluginID() {
		return FrameworkPlugin.ID;
	}

}
