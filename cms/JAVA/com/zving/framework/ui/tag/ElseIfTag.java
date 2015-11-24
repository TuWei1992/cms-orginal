package com.zving.framework.ui.tag;

import com.zving.framework.template.exception.TemplateRuntimeException;

/**
 * else if条件分支标签.<br>
 * 
 * @Author 王育春
 * @Date 2010-11-19
 * @Mail wyuch@zving.com
 */
public class ElseIfTag extends IfTag {

	@Override
	public String getTagName() {
		return "elseif";
	}

	@Override
	public int doStartTag() throws TemplateRuntimeException {
		if (ElseTag.isSkip(this, pageContext)) {// 如果if/或其他elseif成立，则本elseif不成立
			return SKIP_BODY;
		}
		return super.doStartTag();
	}

	@Override
	public String getDescription() {
		return "@{Framework.ZIfTagDescription}";
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.ZIfTagName}";
	}

}
