package com.zving.platform.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-2-15
 */
public class SystemModelType implements IMetaModelType {
	public static final String ID = "System";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "@{Platform.SystemMetadata}";
	}

	@Override
	public boolean isSystemModel() {
		return true;
	}

	@Override
	public List<IMetaModelTemplateType> getTemplateTypes() {
		ArrayList<IMetaModelTemplateType> list = new ArrayList<IMetaModelTemplateType>();
		list.add(new SystemModelTemplateType());
		return list;
	}

	@Override
	public String getDefautlTemplateHtml() {
		return ModelTemplateService.DefaultTemplate;
	}

	public enum SystemMetaModel {
		Branch, User;

		public static String getValue(SystemMetaModel smm) {
			if (Branch == smm) {
				return "branch";
			} else if (User == smm) {
				return "user";
			}
			return null;
		}

		public static boolean isValidate(SystemMetaModel smm) {
			return Branch == smm || User == smm;
		}
	}
}
