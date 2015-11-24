package com.zving.platform.api;

public class APIOutput extends APIParam {

	public APIOutput(String parentName, String name, String memo, int type, boolean allowNull) {
		super(name, memo, type, allowNull);
		this.parentName = parentName;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	private String parentName;

}
