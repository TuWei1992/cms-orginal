package com.zving.platform.log.type;

import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.platform.ILogType;

/**
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-3-30
 */
public class SQLLog implements ILogType {
	public static final String ID = "SQLLog";
	public static final String SubType_Slow = "Slow";
	public static final String SubType_Error = "Error";
	public static final String SubType_Timeout = "Timeout";

	private Mapx<String, String> map;

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "@{Logs.SQLLogMenu}";
	}

	@Override
	public Mapx<String, String> getSubTypes() {
		if (map == null) {
			map = new Mapx<String, String>();
			map.put(SubType_Slow, "@{Platform.SlowSQL}");
			map.put(SubType_Error, "@{Platform.SQLError}");
			map.put(SubType_Timeout, "@{Platform.SQLTimeout}");
		}
		return map;
	}

	@Override
	public void decodeMessage(DataTable dt) {
		dt.insertColumn("CostTime");
		for (DataRow dr : dt) {
			String message = dr.getString("LogMessage");
			int costTimeIndex = message.indexOf("CostTime=");
			int index = message.indexOf(",");
			if (costTimeIndex != -1 && index > costTimeIndex + 9) {
				dr.set("CostTime", message.substring(costTimeIndex + 9, index));
			}
			index = message.indexOf("Message=");
			if (index != -1) {
				dr.set("LogMessage", message.substring(index + 8));
			} else {
				dr.set("LogMessage", message);
			}
		}
	}

}
