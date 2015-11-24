package com.zving.platform.bl;

import com.zving.framework.User;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.meta.MetaModel;
import com.zving.platform.meta.ModelTemplateService;
import com.zving.platform.meta.SystemModelTemplateType;
import com.zving.platform.meta.SystemModelType;
import com.zving.platform.meta.SystemModelType.SystemMetaModel;
import com.zving.schema.ZDModelTemplate;

public class BranchBL {

	/**
	 * 系统元数据，支持Branch和User
	 */
	public static String getSystemMetaModel(SystemMetaModel smm) {
		if (!SystemMetaModel.isValidate(smm)) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		long modelID = new Q().select("ID").from("ZDMetaModel").where("Code", SystemMetaModel.getValue(smm)).and()
				.eq("OwnerType", SystemModelType.ID).executeLong();
		if (modelID != 0) {
			MetaModel mm = MetaModel.load(modelID);
			ZDModelTemplate mt = mm.getTemplateByType(SystemModelTemplateType.ID);
			String tempate = mt == null || StringUtil.isEmpty(mt.getTemplateContent()) ? ModelTemplateService.DefaultTemplate : mt
					.getTemplateContent();
			sb.append(ModelTemplateService.parseModelTemplate(mm.getDAO().getID(), tempate));
		}
		return sb.toString();
	}

	public static DataTable loadTreeTable(String parentInnerCode) {
		Q q = new Q().select("BranchInnerCode", "Name", "TreeLevel", "ParentInnerCode").from("ZDBranch");
		if (ObjectUtil.notEmpty(parentInnerCode)) {
			q.where().likeRight("ParentInnerCode", parentInnerCode);
		} else {
			q.where().likeRight("BranchInnerCode", User.getBranchInnerCode());
		}
		q.orderby("OrderFlag,BranchInnerCode");
		DataTable dt = q.fetch();
		dt.insertColumn("ParentID");
		dt.insertColumn("ID");
		for (DataRow dr : dt) {
			dr.set("ParentID", dr.getString("ParentInnerCode"));
			dr.set("ID", dr.getString("BranchInnerCode"));
		}
		return dt;
	}
}
