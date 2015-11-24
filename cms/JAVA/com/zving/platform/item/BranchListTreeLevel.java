package com.zving.platform.item;

import com.zving.framework.User;
import com.zving.framework.utility.NumberUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.platform.AbstractUserPreferences;

public class BranchListTreeLevel extends AbstractUserPreferences {

	public final static String ID = "BranchListTreeLevel";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "@{Platform.BranchListTreeLevel}";
	}

	public static int getValue() {
		Object v = User.getValue(ID);
		if (User.isLogin() && ObjectUtil.notEmpty(v)) {
			return Integer.valueOf(v + "");
		}
		return 2;
	}

	@Override
	public boolean validate(String value) {
		return NumberUtil.isInt(value) && Integer.valueOf(value) > 0;
	}

	@Override
	public String defaultValue() {
		return "2";
	}
}
