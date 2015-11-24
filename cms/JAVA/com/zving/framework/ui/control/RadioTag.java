package com.zving.framework.ui.control;

import com.zving.framework.expression.ExpressionException;

/**
 * 单选框标签　
 * 
 * @Author 王育春
 * @Date 2010-11-23
 * @Mail wyuch@zving.com
 */
public class RadioTag extends CheckboxTag {
	@Override
	public void init() throws ExpressionException {
		super.init();
		type = "radio";
	}

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "radio";
	}

	@Override
	public String getDescription() {
		return "@{Framework.RadioTag.Desc}";
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.RadioTag.Name}";
	}

}
