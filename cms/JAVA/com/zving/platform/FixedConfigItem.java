package com.zving.platform;

import java.util.List;

import com.zving.framework.Config;
import com.zving.framework.collection.Mapx;
import com.zving.framework.extend.IExtendItem;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.FixedCodeType.FixedCodeItem;
import com.zving.platform.service.CodeService;

/**
 * 系统中不可删除、对配置值有要求的配置项
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-11-17
 */
public abstract class FixedConfigItem implements IExtendItem {
	private String Code;
	private String DataType;
	private String ControlType;
	private String Memo;
	private String PluginID;
	private Mapx<String, String> Options;

	public FixedConfigItem(String code, String dataType, String controlType, String memo, String pluginID) {
		Code = code;
		DataType = dataType;
		ControlType = controlType;
		Memo = memo;
		PluginID = pluginID;
	}

	public void addOption(String key, String value) {
		if (Options == null) {
			Options = new Mapx<String, String>();
		}
		Options.put(key, value);
	}

	/**
	 * 根据代码项ID填充配置项
	 * 
	 * @param id
	 */
	public void setOptionsByCode(String id) {
		List<FixedCodeItem> items = CodeService.getInstance().get(id).fixedItems;
		for (FixedCodeItem item : items) {
			addOption(item.getValue(), item.getName());
		}
	}

	public String getCode() {
		return Code;
	}

	public String getDataType() {
		return DataType;
	}

	public String getControlType() {
		return ControlType;
	}

	public Mapx<String, String> getOptions() {
		return Options;
	}

	@Override
	public String getExtendItemID() {

		return getCode();
	}

	@Override
	public String getExtendItemName() {
		return getMemo();
	}

	public String getMemo() {
		return Memo;
	}

	public String getPluginID() {
		return PluginID;
	}

	public void setPluginID(String pluginID) {
		PluginID = pluginID;
	}

	/**
	 * 将一个配置项值中的${Parent}替换为当前应用的父路径，将${Self}替换为当前应用路径
	 */
	public static String replacePathHolder(String v) {
		String path = Config.getContextRealPath();
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		v = StringUtil.replaceEx(v, "${Parent}", path.substring(0, path.lastIndexOf("/")));
		v = StringUtil.replaceEx(v, "${Self}", path);
		v = FileUtil.normalizePath(v);
		return v;
	}
}
