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
import com.zving.framework.ui.control.datalist.DataListBodyManager;
import com.zving.framework.utility.LogUtil;

/**
 * DataList服务器端响应UI类
 * 
 * @Author 王育春
 * @Date 2008-1-28
 * @Mail wyuch@zving.com
 */
public class DataListUI extends UIFacade {

	@Verify(ignoreAll = true)
	@Priv(login = false)
	public void doWork() {
		try {
			DataListAction dla = new DataListAction();
			dla.setPageEnabled($B(Constant.Page));
			String method = $V(Constant.Method);
			dla.setMethod(method);
			dla.setID($V(Constant.ID));
			dla.setSortEnd($V(Constant.SortEnd));
			dla.setDragClass($V(Constant.DragClass));
			dla.setParams(Request);
			dla.setPageSize($I(Constant.Size));
			if (dla.getPageSize() > DataGridUI.MaxPageSize) {
				dla.setPageSize(DataGridUI.MaxPageSize);
			}
			if (dla.isPageEnabled()) {
				dla.setPageIndex(0);
				if (Request.get(Constant.DataGridPageIndex) != null && !Request.get(Constant.DataGridPageIndex).equals("")) {
					dla.setPageIndex($I(Constant.DataGridPageIndex));
				}
				if (dla.getPageIndex() < 0) {
					dla.setPageIndex(0);
				}
				if (dla.getPageIndex() != 0) {
					dla.setTotal($I(Constant.DataGridPageTotal));
				}
			}
			dla.setAjaxRequest(true);
			dla.setTagBody(DataListBodyManager.get(Request.getString(Constant.TagBody)));
			IMethodLocator m = MethodLocatorUtil.find(method);
			PrivCheck.check(m);
			// 参数检查
			if (!VerifyCheck.check(m)) {
				String message = "Verify check failed:method=" + method + ",data=" + Current.getRequest();
				LogUtil.warn(message);
				Current.getResponse().setFailedMessage(message);
				return;
			}
			m.execute(dla);
			$S("HTML", dla.getResult());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
