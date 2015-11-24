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
public class MetadataTextColumn extends AbstractMetaDataColumnControlType {

	public static final String ID = "Text";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "@{Platform.Input}";
	}

	@Override
	public String getHtml(ZDMetaColumn mc, String value) {
		String code = MetadataService.ControlPrefix + mc.getCode();
		if (StringUtil.isEmpty(value)) {
			value = MetaUtil.getValue(mc);
		}
		value = StringUtil.quickHtmlEncode(value);

		String styleText = MetaUtil.getClass(mc);
		String verify = MetaUtil.getVerify(mc);
		return "<input type=\"text\" id=\"" + code + "\" value=\"" + value + "\" name=\"" + code + "\" " + styleText + verify + " />\n";
	}

}
