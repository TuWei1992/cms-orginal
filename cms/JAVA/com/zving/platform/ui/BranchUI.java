package com.zving.platform.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.zving.framework.UIFacade;
import com.zving.framework.User;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.cache.CacheManager;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.i18n.Lang;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.ui.control.TreeAction;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.bl.BranchBL;
import com.zving.platform.code.YesOrNo;
import com.zving.platform.meta.MetaUtil;
import com.zving.platform.meta.SystemModelType.SystemMetaModel;
import com.zving.platform.point.AfterBranchAddAction;
import com.zving.platform.point.AfterBranchDeleteAction;
import com.zving.platform.point.AfterBranchModifyAction;
import com.zving.platform.privilege.BranchPriv;
import com.zving.platform.util.NoUtil;
import com.zving.platform.util.PlatformCache;
import com.zving.platform.util.PlatformUtil;
import com.zving.schema.ZDBranch;

/**
 * @Author 王育春
 * @Date 2007-7-20
 * @Mail wyuch@zving.com
 */
@Alias("Branch")
public class BranchUI extends UIFacade {
	@Priv(BranchPriv.MenuID)
	public void initDialog() {
		String branchInnerCode = $V("BranchInnerCode");
		if (StringUtil.isNotEmpty(branchInnerCode)) {
			ZDBranch branch = new ZDBranch();
			branch.setBranchInnerCode(branchInnerCode);
			branch.fill();
			Response.putAll(branch.toMapx());
			String parentName = new Q().select("Name").from("ZDBranch").where("BranchInnerCode", branch.getParentInnerCode())
					.executeString();
			parentName = LangUtil.decode(parentName);
			if (branch.getParentInnerCode().equals(branch.getBranchInnerCode())) {
				$S("ParentInnerCode", "");
			}
			$S("ParentName", parentName);
			$S("ManagerName", getManagersRealName(branch.getManager()));
			Request.putAll(MetaUtil.getExtendData(branch.getBranchInnerCode(), SystemMetaModel.getValue(SystemMetaModel.Branch)));
		} else {
			$S("Type", "1");
		}
		$S("MetaModelControlHtml", BranchBL.getSystemMetaModel(SystemMetaModel.Branch));
	}

	private static String getManagersRealName(String managers) {
		if (StringUtil.isEmpty(managers)) {
			return "";
		}
		String[] arr = managers.split("\\,");
		StringBuilder sb = new StringBuilder();
		for (String user : arr) {
			if (PlatformCache.getUser(user) == null) {
				continue;
			}
			if (sb.length() != 0) {
				sb.append(",");
			}
			if (StringUtil.isNotEmpty(PlatformUtil.getUserRealName(user))) {
				if (!LangUtil.get(PlatformUtil.getUserRealName(user)).equals("")) {
					sb.append(LangUtil.get(PlatformUtil.getUserRealName(user)));
				} else {
					sb.append(user);
				}
			} else {
				sb.append(user);
			}

		}
		return sb.toString();
	}

	@Priv
	public DataTable getBranchTable() {
		DataTable dt = new Q().select("BranchInnerCode", "Name", "TreeLevel", "ParentInnerCode").from("ZDBranch").where()
				.likeRight("BranchInnerCode", User.getBranchInnerCode()).orderby("OrderFlag,BranchInnerCode").fetch();
		LangUtil.decode(dt, "Name");
		PlatformUtil.indentDataTable(dt, 1, 2, 1);
		return dt;
	}

	@Priv(BranchPriv.MenuID)
	public void dg1DataBind(DataGridAction dga) {
		Q q = new Q().select("*").from("ZDBranch").where();
		if ($V("ParentID") != null) {
			q.likeRight("BranchInnerCode", $V("ParentID"));
		} else {
			q.likeRight("BranchInnerCode", User.getBranchInnerCode());
		}
		q.orderby("OrderFlag");
		DataTable dt = q.fetch();
		dt.insertColumn("ManagerName");
		for (DataRow dr : dt) {
			if (ObjectUtil.notEmpty(dr.getString("Manager"))) {
				dr.set("ManagerName", getManagersRealName(dr.getString("Manager")));
			}
		}
		LangUtil.decode(dt, "Name");
		LangUtil.decode(dt, "ManagerName");
		dga.bindData(dt);
	}

	@Priv(BranchPriv.Add)
	public void add() {
		if (isNameOrBranchCodeExists($V("Name"), $V("BranchCode"), null, $V("ParentInnerCode"))) {
			fail(Lang.get("Platform.DuplicateNameOrBranchCode"));
			return;
		}
		String parentInnerCode = $V("ParentInnerCode");
		Transaction tran = new Transaction();
		ZDBranch branch = null;
		if (StringUtil.isEmpty(parentInnerCode)) {
			parentInnerCode = "0";
			branch = new ZDBranch();
			branch.setValue(Request);
			branch.setBranchInnerCode(NoUtil.getMaxNo("BranchInnerCode", 4));
			branch.setParentInnerCode(parentInnerCode);
			branch.setTreeLevel(1);
			branch.setType("0");
			branch.setIsLeaf(YesOrNo.Yes);

			long orderFlag = new Q().select("max(OrderFlag)").from("ZDBranch").executeLong();
			tran.add(new Q().update("ZDBranch").set().self("OrderFlag", "+", 1).where().gt("OrderFlag", orderFlag));
			branch.setOrderFlag(orderFlag + 1);
			branch.setAddTime(new Date());
			branch.setAddUser(User.getUserName());
		} else {
			ZDBranch parent = new ZDBranch();
			parent.setBranchInnerCode(parentInnerCode);
			parent.fill();

			branch = new ZDBranch();
			branch.setValue(Request);
			branch.setBranchInnerCode(NoUtil.getMaxNo("BranchInnerCode", parent.getBranchInnerCode(), 4));
			branch.setParentInnerCode(parent.getBranchInnerCode());
			branch.setTreeLevel(parent.getTreeLevel() + 1);
			branch.setType("0");
			branch.setIsLeaf(YesOrNo.Yes);
			branch.setAddTime(new Date());
			branch.setAddUser(User.getUserName());

			long orderFlag = new Q().select("max(OrderFlag)").from("ZDBranch").where()
					.likeRight("BranchInnerCode", parent.getBranchInnerCode()).executeLong();
			branch.setOrderFlag(orderFlag + 1);
			tran.add(new Q().update("ZDBranch").set("IsLeaf", "N").where("BranchInnerCode", parent.getBranchInnerCode()));
			tran.add(new Q().update("ZDBranch").set().self("OrderFlag", "+", 1).where().gt("OrderFlag", orderFlag));
		}
		branch.setName(LangUtil.getI18nFieldValue("Name"));
		tran.add(branch, Transaction.INSERT);
		MetaUtil.saveExtendData(Request, branch.getBranchInnerCode(), SystemMetaModel.getValue(SystemMetaModel.Branch));
		if (tran.commit()) {
			CacheManager.set(PlatformCache.ProviderID, PlatformCache.Type_Branch, branch.getBranchInnerCode(), branch);
			ExtendManager.invoke(AfterBranchAddAction.ExtendPointID, new Object[] { branch });
			success(Lang.get("Common.AddSuccess"));
		} else {
			fail(Lang.get("Common.AddFailed"));
		}
	}

	@Priv(BranchPriv.Edit)
	public void save() {
		String branchInnerCode = $V("BranchInnerCode");
		Transaction tran = new Transaction();
		if (StringUtil.isEmpty(branchInnerCode)) {
			fail("BranchInnerCode is empty!");
			return;
		}
		if (branchInnerCode.length() == 4) {
			Request.put("ParentInnerCode", branchInnerCode);// 顶级机构的上级机构就是自己
		}
		if (isNameOrBranchCodeExists($V("Name"), $V("BranchCode"), branchInnerCode, $V("ParentInnerCode"))) {
			fail(Lang.get("Platform.DuplicateNameOrBranchCode"));
			return;
		}
		ZDBranch branch = new ZDBranch();
		branch.setBranchInnerCode(branchInnerCode);
		if (!branch.fill()) {
			fail(branchInnerCode + " is not found!");
			return;
		}

		branch.setValue(Request);
		branch.setModifyUser(User.getUserName());
		branch.setModifyTime(new Date());
		branch.setName(LangUtil.getI18nFieldValue("Name"));

		tran.add(branch, Transaction.UPDATE);
		MetaUtil.saveExtendData(Request, branch.getBranchInnerCode(), SystemMetaModel.getValue(SystemMetaModel.Branch));
		if (tran.commit()) {
			CacheManager.set(PlatformCache.ProviderID, PlatformCache.Type_Branch, branch.getBranchInnerCode(), branch);
			ExtendManager.invoke(AfterBranchModifyAction.ExtendPointID, new Object[] { branch });
			success(Lang.get("Common.ExecuteSuccess"));
		} else {
			fail(Lang.get("Common.ExecuteFailed"));
		}
	}

	@Priv(BranchPriv.Delete)
	public void del() {
		String[] ids = $V("IDs").split(",");
		Transaction trans = new Transaction();
		ZDBranch branch = new ZDBranch();
		for (String innerCode : ids) {
			branch.setBranchInnerCode(innerCode);
			Q q1 = new Q().select("count(1)").from("ZDUser").where("BranchInnerCode", innerCode);
			Q q2 = new Q().select("count(1)").from("ZDRole").where("BranchInnerCode", innerCode);
			if (q1.executeInt() > 0 || q2.executeInt() > 0) {
				fail(Lang.get("Platform.Branch.DeleteExistsUserOrRole"));
				return;
			}
			if (branch.fill()) {
				if ("0".equals(branch.getParentInnerCode())) {
					fail(Lang.get("Branch.DeleteRootMessage"));
					return;
				}
				Q q = new Q().where().likeRight("BranchInnerCode", branch.getBranchInnerCode() + "%");
				trans.add(branch.query(q), Transaction.DELETE_AND_BACKUP);
				trans.add(new Q().delete().from("ZDPrivilege").where("OwnerType", "Branch").and().eq("Owner", innerCode));
			}
		}
		// MetaUtil.batchDeleteExtendData(trans, IDs, "Organ");
		if (trans.commit()) {
			ExtendManager.invoke(AfterBranchDeleteAction.ExtendPointID, new Object[] { ids });
			for (String id : ids) {
				CacheManager.remove(PlatformCache.ProviderID, PlatformCache.Type_Branch, id);
			}
			success(Lang.get("Common.DeleteSuccess"));
		} else {
			fail(Lang.get("Common.DeleteFailed"));
		}
	}

	@Priv(BranchPriv.Edit)
	public void sortBranch() {
		String orderBranch = $V("OrderBranch");
		String nextBranch = $V("NextBranch");
		String orderType = $V("OrderType");
		if (StringUtil.isEmpty(orderBranch) || StringUtil.isEmpty(nextBranch) || StringUtil.isEmpty(orderType)) {
			return;
		}

		Transaction tran = new Transaction();
		DataTable allBranchs = new Q().select("*").from("ZDBranch").orderby("orderflag").fetch();
		List<DataRow> branchList = new ArrayList<DataRow>();

		// 需要排序的机构所在的树（该机构及其子机构）
		DataTable orderDT = new Q().select("*").from("ZDBranch").where().likeRight("BranchInnerCode", orderBranch).orderby("OrderFlag")
				.fetch();

		// 要放置（机构前或机构后）机构所对应的树
		DataTable nextDT = new Q().select("*").from("ZDBranch").where().likeRight("BranchInnerCode", nextBranch).orderby("OrderFlag")
				.fetch();

		// 从下往上拉
		if ("before".equalsIgnoreCase(orderType)) {
			for (int i = 0; i < allBranchs.getRowCount(); i++) {
				if (allBranchs.getString(i, "BranchInnerCode").equals(nextBranch)) {
					for (int m = 0; orderDT != null && m < orderDT.getRowCount(); m++) {
						branchList.add(orderDT.getDataRow(m));
					}
				} else if (allBranchs.getString(i, "BranchInnerCode").equals(orderBranch)) {
					// 跳过排序机构树
					i = i - 1 + orderDT.getRowCount();
					continue;
				}
				branchList.add(allBranchs.getDataRow(i));
			}

			// 从上往下拉
		} else if ("after".equalsIgnoreCase(orderType)) {
			for (int i = 0; allBranchs != null && i < allBranchs.getRowCount(); i++) {
				if (allBranchs.getString(i, "BranchInnerCode").equals(orderBranch)) {
					// 跳过排序机构树
					i = i - 1 + orderDT.getRowCount();
					continue;
				} else if (allBranchs.getString(i, "BranchInnerCode").equals(nextBranch) && nextDT != null) {
					// 先排 选择树，再排 排序机构树
					for (int m = 0; m < nextDT.getRowCount(); m++) {
						branchList.add(nextDT.getDataRow(m));
					}
					for (int j = 0; orderDT != null && j < orderDT.getRowCount(); j++) {
						branchList.add(orderDT.getDataRow(j));
					}
					// 继续循环排序
					i = i - 1 + nextDT.getRowCount();
				} else {
					branchList.add(allBranchs.getDataRow(i));
				}
			}
		}

		for (int i = 0; branchList != null && i < branchList.size(); i++) {
			DataRow dr = branchList.get(i);
			tran.add(new Q().update("ZDBranch").set("OrderFlag", i).where("BranchInnerCode", dr.getString("BranchInnerCode")));
		}
		if (tran.commit()) {
			success(Lang.get("Common.ExecuteSuccess"));
		} else {
			fail(Lang.get("Common.ExecuteFailed"));
		}
	}

	private boolean isNameOrBranchCodeExists(String name, String branchCode, String innerCode, String parentInnerCode) {
		if (StringUtil.isEmpty(parentInnerCode)) {
			parentInnerCode = "0";
		}
		Map<String, Object> map = CacheManager.getMapx(PlatformCache.ProviderID, PlatformCache.Type_Branch);
		for (Object obj : map.values()) {
			ZDBranch branch = (ZDBranch) obj;
			if (branch.getBranchInnerCode().equals(innerCode)) {
				continue;
			}
			// 相同机构编码
			if (ObjectUtil.notEmpty(branch.getBranchCode()) && branch.getBranchCode().equals(branchCode)) {
				return true;
			}
			// 相同机构名称并且相同父级机构
			if (name.equals(LangUtil.get(branch.getName())) && branch.getParentInnerCode().equals(parentInnerCode)) {
				return true;
			}
		}
		return false;
	}

	@Priv
	public void branchTree(TreeAction ta) {
		DataTable dt = BranchBL.loadTreeTable(Request.getString("ParentID"));
		ta.setRootText("@{Platform.Plugin.Branch}");
		ta.setRootIcon("icons/icon042a1.png");
		ta.bindData(dt);
	}
}
