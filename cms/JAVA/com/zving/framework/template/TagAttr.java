package com.zving.framework.template;

import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataTypes;

/**
 * 标签属性描述类
 */
public class TagAttr {

	String Name;
	boolean isMandatory = false;
	String Usage;
	int DataType = DataTypes.STRING;

	Mapx<String, String> Options = new Mapx<String, String>();

	/**
	 * 属性名称
	 */
	public String getName() {
		return Name;
	}

	/**
	 * 是否必填
	 */
	public boolean isMandatory() {
		return isMandatory;
	}

	/**
	 * 属性用法
	 */
	public String getUsage() {
		return Usage;
	}

	/**
	 * 数据类型，见DataTypes
	 */
	public int getDataType() {
		return DataType;
	}

	public TagAttr(String name) {
		Name = name;
	}

	public TagAttr(String name, boolean mandatory) {
		Name = name;
		isMandatory = mandatory;
	}

	public TagAttr(String name, int dataType) {
		Name = name;
		DataType = dataType;
	}

	public TagAttr(String name, boolean mandatory, int dataType, String usage) {
		Name = name;
		isMandatory = mandatory;
		DataType = dataType;
		Usage = usage;
	}

	public TagAttr(String name, int dataType, String usage) {
		Name = name;
		DataType = dataType;
		Usage = usage;
	}

	public TagAttr(String name, Mapx<String, String> options, String usage) {
		Name = name;
		Options = options;
		Usage = usage;
	}

	public TagAttr(String name, Mapx<String, String> options) {
		Name = name;
		Options = options;
	}

	public Mapx<String, String> getOptions() {
		return Options;
	}

	public void setOptions(Mapx<String, String> options) {
		Options = options;
	}

	public static Mapx<String, String> BOOL_OPTIONS = new Mapx<String, String>();

	static {
		BOOL_OPTIONS.put("true", "true");
		BOOL_OPTIONS.put("false", "false");
	}
}
