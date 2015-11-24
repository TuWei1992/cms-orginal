package com.zving.platform.api;

public class APIParam {
	// 参数名称
	private String name;
	// 参数说明
	private String memo;
	// 数据类型
	private int type;
	// 是否必填
	private boolean allowNull;

	public APIParam(String name, String memo, int type, boolean allowNull) {
		this.name = name;
		this.memo = memo;
		this.type = type;
		this.allowNull = allowNull;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean isAllowNull() {
		return allowNull;
	}

	public void setAllowNull(boolean allowNull) {
		this.allowNull = allowNull;
	}
}
