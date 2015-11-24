package com.zving.platform.service;

import com.zving.platform.AbstractUserPreferences;

public class ShortcutPreference extends AbstractUserPreferences {

	public final static String ID = "Shortcut";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "@{Platform.UserPreferences.Shortcut}";
	}

	@Override
	public boolean validate(String value) {
		return true;
	}

	@Override
	public String defaultValue() {
		return null;
	}

}
