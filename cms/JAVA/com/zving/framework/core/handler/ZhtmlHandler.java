package com.zving.framework.core.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zving.framework.Config;
import com.zving.framework.Current;
import com.zving.framework.core.Dispatcher;
import com.zving.framework.data.BlockingTransaction;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.extend.action.AfterZhtmlExecuteAction;
import com.zving.framework.template.exception.TemplateNotFoundException;
import com.zving.framework.ui.zhtml.ZhtmlExecuteContext;
import com.zving.framework.ui.zhtml.ZhtmlManagerContext;

/**
 * Zhtml页面处理者
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-5
 */
public class ZhtmlHandler extends AbstractHtmlHandler {
	public static final String ID = "com.zving.framework.core.ZhtmlHandler";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public boolean match(String url) {
		int i = url.indexOf("?");
		if (i > 0) {
			url = url.substring(0, i);
		}
		return url.endsWith(".zhtml");
	}

	@Override
	public String getExtendItemName() {
		return "Zhtml URL Processor";
	}

	@Override
	public boolean execute(String url, HttpServletRequest request, HttpServletResponse response) {
		int i = url.indexOf("?");
		if (i > 0) {
			url = url.substring(0, i);
		}
		if (!Config.isInstalled() && url.indexOf("install.zhtml") < 0 && url.indexOf("ajax/invoke") < 0) {
			Dispatcher.forward("/install.zhtml");
			return true;
		}
		if (url.indexOf("/ajax/invoke") > 0 && !url.equals("/ajax/invoke")) {// 页面初始化时会有这种情况
			Dispatcher.forward("/ajax/invoke");
			return true;
		}
		ZhtmlExecuteContext context = new ZhtmlExecuteContext(ZhtmlManagerContext.getInstance(), request, response);
		Current.setExecuteContext(context);
		try {
			if (!context.execute(url)) {
				return false;
			}
		} catch (TemplateNotFoundException e) {
			return false;
		} finally {
			BlockingTransaction.clearTransactionBinding();// 检测是否有未被关闭的阻塞型事务连接
		}
		ExtendManager.invoke(AfterZhtmlExecuteAction.ExtendPointID, new Object[] { request, response });
		return true;
	}

	@Override
	public int getOrder() {
		return 9999;
	}
}
