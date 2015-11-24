package com.zving.framework.ui.control;

import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Priv;
import com.zving.framework.i18n.LangMapping;
import com.zving.framework.utility.StringUtil;

/**
 * 长时间任务UI类，用于向前台的进度条控件提供进度数据。
 * 
 * @Author 王育春
 * @Date 2008-7-28
 * @Mail wyuch@zving.com
 */
public class LongTimeTaskUI extends UIFacade {
	@Priv(login = false)
	public void getInfo() {
		long id = $L("TaskID");
		LongTimeTask ltt = LongTimeTask.getInstanceById(id);
		if (ltt != null && ltt.isAlive()) {
			$S("CurrentInfo", StringUtil.isNotEmpty(ltt.getCurrentInfo()) ? ltt.getCurrentInfo() + "..." : "");
			$S("Messages", StringUtil.join(ltt.getMessages()));
			$S("Percent", "" + ltt.getPercent());
		} else {
			$S("CompleteFlag", "1");
			String finishInfo = LangMapping.get("Framework.TaskFinished");
			if (ltt != null) {
				String errors = ltt.getAllErrors();
				if (StringUtil.isNotEmpty(errors)) {
					$S("CurrentInfo", errors);
					$S("ErrorFlag", "1");
				} else {
					finishInfo = StringUtil.isNotEmpty(ltt.getFinishedInfo()) ? ltt.getFinishedInfo() : finishInfo;
					$S("CurrentInfo", finishInfo);
				}
			} else {
				$S("CurrentInfo", finishInfo);
			}
			LongTimeTask.removeInstanceById(id);
		}
	}

	@Priv(login = false)
	public void stop() {
		long id = $L("TaskID");
		LongTimeTask ltt = LongTimeTask.getInstanceById(id);
		if (ltt != null) {
			ltt.stopTask();
		}
	}

	@Priv(login = false)
	public void stopComplete() {
		long id = $L("TaskID");
		LongTimeTask ltt = LongTimeTask.getInstanceById(id);
		if (ltt == null || !ltt.isAlive()) {
			LongTimeTask.removeInstanceById(id);
		} else {
			Response.setStatus(0);
		}
	}
}
