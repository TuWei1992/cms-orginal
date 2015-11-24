package com.zving.platform.log.type;

import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataTable;
import com.zving.platform.ILogType;

/**
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-3-30
 */
public class CronLog implements ILogType {
	public static final String ID = "CronLog";
	public static final String SubType_Start = "Start";
	public static final String SubType_Blocked = "Blocked";
	public static final String SubType_End = "End";

	private Mapx<String, String> map;

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "@{Logs.CronLogMenu}";
	}

	@Override
	public Mapx<String, String> getSubTypes() {
		if (map == null) {
			map = new Mapx<String, String>();
			map.put(SubType_Start, "@{Platform.TaskStart}");
			map.put(SubType_End, "@{Platform.TaskEnd}");
		}
		return map;
	}

	@Override
	public void decodeMessage(DataTable dt) {
	}

}
