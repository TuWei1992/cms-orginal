package com.zving.platform.meta.control;

import com.zving.framework.utility.StringUtil;
import com.zving.platform.meta.AbstractMetaDataColumnControlType;
import com.zving.platform.meta.MetaUtil;
import com.zving.platform.meta.MetadataService;
import com.zving.schema.ZDMetaColumn;

/**
 * author: 欧阳晓亮
 * Email: oyxl@zving.com
 * Date: 2013-3-12
 */
public class MetadataTextAreaColumn extends AbstractMetaDataColumnControlType {

	public static final String ID = "TextArea";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "@{Platform.Textarea}";
	}

	@Override
	public String getHtml(ZDMetaColumn mc, String value) {
		String code = MetadataService.ControlPrefix + mc.getCode();
		if (StringUtil.isEmpty(value)) {
			value = MetaUtil.getValue(mc);
		}
		String styleText = MetaUtil.getClass(mc);
		String verify = MetaUtil.getVerify(mc);
		return "<textarea style=\"width:494px\" id=\"" + code + "\" name=\"" + code + "\" " + styleText + verify + ">" + value + "</textarea>\n<br />";
	}

}
