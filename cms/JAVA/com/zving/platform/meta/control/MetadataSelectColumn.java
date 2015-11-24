package com.zving.platform.meta.control;

import com.zving.framework.collection.Mapx;
import com.zving.framework.utility.HtmlUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.code.ControlType;
import com.zving.platform.meta.AbstractMetaDataColumnControlType;
import com.zving.platform.meta.MetaUtil;
import com.zving.platform.meta.MetadataService;
import com.zving.schema.ZDMetaColumn;

/**
 * author: 欧阳晓亮
 * Email: oyxl@zving.com
 * Date: 2013-3-12
 */
public class MetadataSelectColumn extends AbstractMetaDataColumnControlType {

	public static final String ID = "Select";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "@{Platform.Select}";
	}

	@Override
	public String getHtml(ZDMetaColumn mc, String value) {
		String code = MetadataService.ControlPrefix + mc.getCode();
		if (StringUtil.isEmpty(value)) {
			value = MetaUtil.getValue(mc);
		}
		String styleText = MetaUtil.getClass(mc);
		String verify = MetaUtil.getVerify(mc);

		StringBuilder sb = new StringBuilder();
		String options = mc.getListOptions();
		if (StringUtil.isNotEmpty(options)) {
			Mapx<String, Object> map = MetaUtil.options2Mapx(options);
			if (mc.getControlType().equals(ControlType.Selector)) {
				sb.append("<select id=\"" + code + "\" name=\"" + code + "\" " + styleText + verify + ">\n");
				sb.append(HtmlUtil.mapxToOptions(map, "option", value, true));
				sb.append("</select>\n");
			}
		}
		return sb.toString();
	}

}
