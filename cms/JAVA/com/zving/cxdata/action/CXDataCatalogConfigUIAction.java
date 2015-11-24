package com.zving.cxdata.action;

import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.action.ZhtmlContext;
import com.zving.framework.extend.action.ZhtmlExtendAction;

/**
 * 
 * @author v_zhouquan
 * 栏目扩展配置管理UI扩展类，引入cxdata/cxDataCatalogConfigExtend.zhtml
 */
public class CXDataCatalogConfigUIAction extends ZhtmlExtendAction {

	@Override
	public boolean isUsable() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void execute(ZhtmlContext context) throws ExtendException {
		context.include("cxdata/cxDataCatalogConfigExtend.zhtml");

	}

}
