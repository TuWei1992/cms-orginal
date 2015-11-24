package com.zving.platform.meta;

/**
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-2-16
 */
public class SystemModelTemplateType implements IMetaModelTemplateType {
	public static final String ID = "System";

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public String getName() {
		return "@{Platform.DefaultShowTemplate}";
	}

}
