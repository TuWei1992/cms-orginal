package com.zving.platform.log.ui;

import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.utility.log.LogAppender;

@Alias("RealTimeLog")
public class RealTimeLogUI extends UIFacade {
	@Priv
	public void getMessage() {
		long id = $L("ID");
		$S("Log", LogAppender.getLog(id).toString());
		$S("LastID", LogAppender.getMaxId());
	}
}
