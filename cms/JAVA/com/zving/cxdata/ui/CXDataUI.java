package com.zving.cxdata.ui;

import com.zving.cxdata.service.CXDataConditionService;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.data.DataTable;

@Alias("CXData")
public class CXDataUI extends UIFacade {
	
	/**
	 * 所有数据获取条件
	 * @return
	 */
	@Priv
	public DataTable getAllCXDataConditions() {
		return CXDataConditionService.getAllCXDataConditions();
	}
}
