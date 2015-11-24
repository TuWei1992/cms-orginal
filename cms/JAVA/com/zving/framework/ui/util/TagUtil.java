package com.zving.framework.ui.util;

import com.zving.framework.Current;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.ui.zhtml.ZhtmlExecuteContext;
import com.zving.framework.utility.ObjectUtil;

/**
 * 标签工具类
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2011-9-12
 */
public class TagUtil {

	private static final String PageContextAttribte_TagID = "ZVING_TAGID_";

	public static String getTagID(AbstractExecuteContext pageContext, String prefix) {
		if (prefix == null) {
			prefix = "";
		}
		if (ObjectUtil.empty(pageContext.getRootVariable(PageContextAttribte_TagID))) {
			pageContext.addRootVariable(PageContextAttribte_TagID, 0);
		}
		int tagid = Integer.valueOf(pageContext.getRootVariable(PageContextAttribte_TagID).toString());
		pageContext.addRootVariable(PageContextAttribte_TagID, ++tagid);
		String uri = "";
		if (pageContext instanceof ZhtmlExecuteContext) {
			uri = Current.getRequest().getURL();
			if (uri.indexOf("?") > -1) {
				uri = uri.substring(0, uri.indexOf("?"));
			}
			if (uri.lastIndexOf("/") + 1 < uri.lastIndexOf(".")) {
				uri = uri.substring(uri.lastIndexOf("/") + 1, uri.lastIndexOf("."));
				uri = uri.replaceAll("[^\\w]", "_");
			} else {
				return "";
			}
		}
		return uri + "_" + prefix + tagid;
	}
}
