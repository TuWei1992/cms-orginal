package com.zving.platform.log.type;

import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataTable;
import com.zving.platform.ILogType;

/**
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-3-30
 */
public class SecurityLog implements ILogType {
	public static final String ID = "SecurityLog";
	public static final String SubType_Entrust = "Entrust";
	public static final String SubType_PrivCheck = "PrivCheck";
	public static final String SubType_Verify = "Verify";
	public static final String SubType_XSS = "XSS";

	private Mapx<String, String> map;

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "@{Logs.SecurityLogMenu}";
	}

	@Override
	public Mapx<String, String> getSubTypes() {
		if (map == null) {
			map = new Mapx<String, String>();
			map.put(SubType_Entrust, "@{Platform.EntrustManagement}");
			map.put(SubType_PrivCheck, "@{Platform.Unauthorizedaccess}");
			map.put(SubType_Verify, "@{Platform.SQLInjection}");
			map.put(SubType_XSS, "@{Platform.ScriptAttack}");
		}
		return map;
	}

	@Override
	public void decodeMessage(DataTable dt) {
	}

}
