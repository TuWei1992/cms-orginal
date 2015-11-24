package com.zving.platform.ui;

import java.util.Date;

import com.zving.framework.Current;
import com.zving.framework.UIFacade;
import com.zving.framework.User;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.annotation.Verify;
import com.zving.framework.collection.Filter;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.i18n.Lang;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.ServletUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.FixedCodeType;
import com.zving.platform.code.YesOrNo;
import com.zving.platform.privilege.CodePriv;
import com.zving.platform.service.CodeService;
import com.zving.platform.util.CodeCache;
import com.zving.platform.util.OrderUtil;
import com.zving.schema.ZDCode;

/**
 * @Author 王育春
 * @Date 2007-6-19
 * @Mail wyuch@zving.com
 */
@Alias("Code")
public class CodeUI extends UIFacade {

	@Priv(CodePriv.MenuID)
	public void bindCodeTypeGrid(DataGridAction dga) {
		final String searchCodeType = $V("SearchCodeType");
		Q q = new Q().select("*").from("ZDCode").where("ParentCode", "System");
		q.orderby("CodeType,ParentCode");
		DataTable dt = q.fetch();
		LangUtil.decode(dt, "CodeName");
		if (StringUtil.isNotEmpty(searchCodeType)) {
			dt = dt.filter(new Filter<DataRow>() {
				@Override
				public boolean filter(DataRow dr) {
					return dr.getString("CodeType").indexOf(searchCodeType) >= 0 || dr.getString("CodeName").indexOf(searchCodeType) >= 0;
				}
			});
		}
		dt.insertColumn("Fixed");
		dt.insertColumn("ID");
		for (DataRow dr : dt) {
			FixedCodeType fct = CodeService.getInstance().get(dr.getString("CodeType"));
			if (fct != null) {
				dr.set("Fixed", YesOrNo.Yes);
			} else {
				dr.set("Fixed", YesOrNo.No);
			}
			dr.set("ID", dr.getString("CodeType") + dr.getString("ParentCode") + dr.getString("CodeValue"));
		}
		YesOrNo.decodeYesOrNoIcon(dt, "Fixed");
		dga.setTotal(dt.getRowCount());
		dga.bindData(dt.getPagedDataTable(dga.getPageSize(), dga.getPageIndex()));
	}

	@Priv(CodePriv.MenuID)
	public void bindCodeListGrid(DataGridAction dga) {
		Q q = new Q().select("*").from("ZDCode").where("ParentCode", $V("CodeType"));
		q.orderby("CodeOrder,CodeType,ParentCode");
		DataTable dt = q.fetch();
		LangUtil.decode(dt, "CodeName");
		dt.insertColumn("Fixed");
		for (DataRow dr : dt) {
			FixedCodeType fct = CodeService.getInstance().get(dr.getString("CodeType"));
			if (fct != null) {
				dr.set("Fixed", fct.contains(dr.getString("CodeValue")) ? YesOrNo.Yes : YesOrNo.No);
			} else {
				dr.set("Fixed", YesOrNo.No);
			}
		}
		YesOrNo.decodeYesOrNoIcon(dt, "Fixed");
		dga.setTotal(dt.getRowCount());
		dga.bindData(dt);
	}

	@Priv(CodePriv.MenuID)
	public void initDialog() {
		String codeType = $V("CodeType");
		String parentCode = $V("ParentCode");
		String codeValue = ServletUtil.getChineseParameter("CodeValue");
		$S("CodeValue", codeValue);
		if (ObjectUtil.empty(codeValue) || ObjectUtil.empty(codeType)) {
			$S("CodeName", "");
			return;
		}
		ZDCode code = new ZDCode();
		code.setCodeType(codeType);
		code.setParentCode(parentCode);
		code.setCodeValue(codeValue);
		code.fill();
		Response.putAll(code.toMapx());
		FixedCodeType fct = CodeService.getInstance().get(codeType);
		if (fct != null) {
			$S("Fixed", true);
		}
	}

	@Priv(CodePriv.MenuID)
	public void initList() {
		String codeType = $V("CodeType");
		ZDCode code = new ZDCode();
		code.setCodeType(codeType);
		code.setParentCode("System");
		code.setCodeValue("System");
		code.fill();
		Response.putAll(code.toMapx());
		FixedCodeType fct = CodeService.getInstance().get(codeType);
		if (fct != null) {
			$S("AllowAddItem", fct.allowAddItem());
		} else {
			$S("AllowAddItem", true);
		}
	}

	@Priv(CodePriv.Add + "||" + CodePriv.Edit)
	@Verify
	public void addOrSave() {
		ZDCode code = new ZDCode();
		code.setValue(Request);
		boolean updateFlag = "Edit".equals($V("Action"));
		if (updateFlag) {
			code.setCodeValue($V("OldCodeValue"));
		}
		if (code.fill()) {// 如果是新增并且代码存在
			if (!updateFlag) {
				if (!"System".equals($V("ParentCode"))) {
					fail(code.getCodeValue() + " " + Lang.get("Common.Exists"));
				} else {
					fail(Lang.get("Code.CodeType") + " " + code.getCodeType() + Lang.get("Common.Exists"));
				}
				return;
			}
		}
		code.setValue(Request);
		code.setCodeName(LangUtil.getI18nFieldValue("CodeName"));
		Transaction tran = new Transaction();
		if (!updateFlag) {
			code.setCodeOrder(System.currentTimeMillis());
			code.setAddTime(new Date());
			code.setAddUser(User.getUserName());
			tran.insert(code);
		} else {
			code.setModifyTime(new Date());
			code.setModifyUser(User.getUserName());
			tran.update(code);
		}
		if (tran.commit()) {
			CodeCache.setCode(code); // 更新缓存
			success(Lang.get("Common.ExecuteSuccess"));
		} else {
			fail(Lang.get("Common.ExecuteFailed"));
		}
	}

	@Priv(CodePriv.Delete)
	public void del() {
		DataTable dt = (DataTable) Request.get("DT");
		DAOSet<ZDCode> set = new DAOSet<ZDCode>();
		for (int i = 0; i < dt.getRowCount(); i++) {
			ZDCode code = new ZDCode();
			code.setValue(dt.getDataRow(i));
			if (code.fill()) {
				FixedCodeType fct = CodeService.getInstance().get(code.getCodeType());
				if (fct != null && ("System".equals(code.getCodeValue()) || fct.contains(code.getCodeValue()))) {
					success(Lang.get("Common.DeleteFailed") + "," + Lang.get("Platform.Code.fixedCodecantDelete"));
					return;
				}
				set.add(code);
			}
			if ("System".equals(code.getParentCode())) {
				ZDCode child = new ZDCode();
				child.setParentCode(code.getCodeType());
				set.addAll(child.query());
			}
		}
		if (set.deleteAndBackup()) {
			for (int i = 0; i < set.size(); i++) {
				CodeCache.removeCode(set.get(i));
			}
			success(Lang.get("Common.DeleteSuccess"));
		} else {
			fail(Lang.get("Common.DeleteFailed"));
		}
	}

	@Priv(CodePriv.Edit)
	public void sortColumn() {
		long targetOrderFlag = $L("TargetOrderFlag");
		if (StringUtil.isNull($V("CodeType")) || StringUtil.isNull($V("CodeValue")) || StringUtil.isNull($V("ParentCode"))
				|| targetOrderFlag <= 0) {
			fail(Lang.get("Common.ExecuteFailed"));
			return;
		}
		ZDCode code = new ZDCode();
		code.setCodeType($V("CodeType"));
		code.setParentCode($V("ParentCode"));
		code.setCodeValue($V("CodeValue"));
		if (!code.fill()) {
			fail(Lang.get("Common.ExecuteFailed"));
			return;
		}
		Q wherePart = new Q().and().eq("ParentCode", $V("ParentCode"));
		OrderUtil.updateOrder(new ZDCode().table(), "CodeOrder", targetOrderFlag, wherePart, code, Current.getTransaction());

		if (Current.getTransaction().commit()) {
			success(Lang.get("Common.ExecuteSuccess"));
		} else {
			fail(Lang.get("Common.ExecuteFailed"));
		}
	}
}
