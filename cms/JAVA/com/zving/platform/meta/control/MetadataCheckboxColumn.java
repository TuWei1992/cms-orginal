package com.zving.platform.meta.control;

import com.zving.framework.collection.Mapx;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.code.YesOrNo;
import com.zving.platform.meta.AbstractMetaDataColumnControlType;
import com.zving.platform.meta.MetaUtil;
import com.zving.platform.meta.MetadataService;
import com.zving.schema.ZDMetaColumn;

/**
 * author: 欧阳晓亮
 * Email: oyxl@zving.com
 * Date: 2013-3-12
 */
public class MetadataCheckboxColumn extends AbstractMetaDataColumnControlType {

	public static final String ID = "Checkbox";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "@{Platform.CheckBox}";
	}

	@Override
	public String getHtml(ZDMetaColumn mc, String value) {
		String code = MetadataService.ControlPrefix + mc.getCode();
		if (StringUtil.isEmpty(value)) {
			value = MetaUtil.getValue(mc);
		}
		StringBuilder sb = new StringBuilder();
		String options = mc.getListOptions();

		if (StringUtil.isNotEmpty(options)) {
			Mapx<String, Object> map = MetaUtil.options2Mapx(options);
			int i = 0;
			String[] values = StringUtil.splitEx(value, ",");
			for (String key : map.keySet()) {
				String checked = ObjectUtil.in(key, values) ? " checked=\"true\"" : "";
				sb.append("<input type=\"checkbox\" id=\"" + code + "_" + i + "\" value=\"" + key + "\" name=\"" + code + "\" " + checked
						+ (YesOrNo.isYes(mc.getMandatoryFlag()) ? "verify=\"NotNull\"" : "") + "/>\n");
				sb.append("<label for=\"" + code + "_" + i + "\">" + map.getString(key) + "</label>");
				i++;
			}
		}
		return sb.toString();
	}

}
