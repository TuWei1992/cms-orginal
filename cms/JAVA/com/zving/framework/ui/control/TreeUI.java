package com.zving.framework.ui.control;

import com.zving.framework.Constant;
import com.zving.framework.Current;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Priv;
import com.zving.framework.annotation.Verify;
import com.zving.framework.core.method.IMethodLocator;
import com.zving.framework.core.method.MethodLocatorUtil;
import com.zving.framework.security.PrivCheck;
import com.zving.framework.security.VerifyCheck;
import com.zving.framework.ui.control.tree.TreeBodyManager;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ObjectUtil;

/**
 * 树标签服务器端响应UI类
 * 
 * @Author 王育春
 * @Date 2008-1-28
 * @Mail wyuch@zving.com
 */
public class TreeUI extends UIFacade { // NO_UCD
	@Priv(login = false)
	@Verify(ignoreAll = true)
	public void doWork() {
		try {
			TreeAction ta = new TreeAction();
			String method = $V(Constant.Method);
			ta.setMethod(method);

			if ("true".equals($V(Constant.TreeLazy))) {
				if (!"false".equals($V(Constant.TreeExpand))) {
					ta.setExpand(true);
				}
				ta.setLazy(true);
			}

			if (ObjectUtil.notEmpty($V("ParentLevel"))) {
				ta.setParentLevel(Request.getInt("ParentLevel"));
				ta.setLazyLoad(true);
			}

			if (ObjectUtil.notEmpty($V("ParentID"))) {
				ta.setParentID($V("ParentID"));
				ta.setLazyLoad(true);
			}

			if ($V(Constant.TreeCheckbox) != null && !"".equals($V(Constant.TreeCheckbox))) {
				ta.setCheckbox($V(Constant.TreeCheckbox));
				if ("false".equals($V(Constant.TreeCascade))) {
					ta.setCascade(false);
				}
			} else if ($V(Constant.TreeRadio) != null && !"".equals($V(Constant.TreeRadio))) {
				ta.setRadio($V(Constant.TreeRadio));
			}

			ta.setID($V(Constant.ID));
			ta.setParams(Request);

			int level = $I(Constant.TreeLevel);
			String style = $V(Constant.TreeStyle);
			if (level <= 0) {
				level = 999;
			}
			ta.setLevel(level);
			ta.setStyle(style);

			ta.setAjaxRequest(true);
			ta.setTagBody(TreeBodyManager.get(Request.getString(Constant.TagBody)));

			IMethodLocator m = MethodLocatorUtil.find(method);
			PrivCheck.check(m);
			// 参数检查
			if (!VerifyCheck.check(m)) {
				String message = "Verify check failed:method=" + method + ",data=" + Current.getRequest();
				LogUtil.warn(message);
				Current.getResponse().setFailedMessage(message);
				return;
			}
			m.execute(ta);
			ta.bindData();// 这是为了兼容旧的写法，setRootText()和setRootIcon()经常写在bindData()之后

			String html = ta.getResult();
			$S("HTML", html);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
