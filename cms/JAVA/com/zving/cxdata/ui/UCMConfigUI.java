package com.zving.cxdata.ui;

import com.zving.adapter.config.UCMConfigLoad;
import com.zving.cxdata.UCMConfig;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.utility.Errorx;

@Alias("UCMConfig")
public class UCMConfigUI extends UIFacade {
	
	@Priv
	public void bindUCMConfig(DataGridAction dga) {
		dga.bindData(UCMConfig.getMapx().toDataTable());
	}
	
	@Priv
	public void  refreshUCMConfig() {
		UCMConfigLoad.refresh();
		UCMConfig.loadUCMConfig();
		UCMConfig.addToConfig();
		if (Errorx.hasError()) {
			fail(Errorx.getAllMessage());
		} else {
			success("刷新UCM配置成功！");
		}
	}
	
}
