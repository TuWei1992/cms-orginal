package com.zving.platform.meta.ui;

import java.util.Date;
import java.util.List;

import com.zving.framework.Current;
import com.zving.framework.UIFacade;
import com.zving.framework.User;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.i18n.Lang;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.utility.NumberUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.code.DataType;
import com.zving.platform.code.YesOrNo;
import com.zving.platform.meta.IMetadataColumnControlType;
import com.zving.platform.meta.MetaModel;
import com.zving.platform.meta.MetadataColumnControlTypeService;
import com.zving.platform.meta.bl.MetaModelBL;
import com.zving.platform.meta.bl.MetaModelColumnBL;
import com.zving.platform.privilege.MetadataPriv;
import com.zving.platform.util.NoUtil;
import com.zving.platform.util.OrderUtil;
import com.zving.platform.util.PlatformCache;
import com.zving.platform.util.PlatformUtil;
import com.zving.schema.ZDMetaColumn;
import com.zving.schema.ZDMetaModel;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2012-1-13
 */
@Alias("MetaColumn")
public class MetaColumnUI extends UIFacade {

	@Priv
	public void bindGrid(DataGridAction dga) {
		DataTable dt = null;
		long id = $L("ID");
		if (id != 0) {
			dt = PlatformCache.getMetaModel(id).getColumns().toDataTable();
			dt.sort("OrderFlag", "ASC");
			dt.decodeColumn("MandatoryFlag", PlatformUtil.getCodeMap("YesOrNo"));
			dga.bindData(dt);
		}
	}

	@Priv
	public void init() {
		List<IMetadataColumnControlType> list = MetadataColumnControlTypeService.getInstance().getAll();
		String sdt = "var sdt = [];";
		for (IMetadataColumnControlType mcc : list) {
			if (ObjectUtil.notEmpty(mcc.getSaveDataType())) {
				sdt = sdt + "\n" + "sdt[\"" + mcc.getExtendItemID() + "\"] =\"" + mcc.getSaveDataType() + "\";";
			}
		}
		$S("sdt", sdt);

		long id = $L("ID");
		if (id != 0) {
			ZDMetaColumn mcg = new ZDMetaColumn();
			mcg.setID(id);
			if (mcg.fill()) {
				Response.putAll(mcg.toMapx());
				if (StringUtil.isNotNull(mcg.getListOptions())) {
					Response.put("ListOptions", mcg.getListOptions().replaceAll("\n", "<br>"));
				}
			}
		} else {
			$S("MandatoryFlag", "N");
			DataTable groupsdt = getGroups();
			if (groupsdt != null && groupsdt.getRowCount() > 0) {
				$S("GroupID", groupsdt.get(0, "ID"));
			}
			$S("DataType", DataType.ShortText);
			DataTable controldt = MetadataColumnControlTypeService.getColumnControlTypeNameMap().toDataTable();
			if (controldt != null && controldt.getRowCount() > 0) {
				$S("ControlType", controldt.get(0, 0));
			}
		}
	}

	@Priv(MetadataPriv.Save)
	public void save() {
		long id = $L("ID");
		long modelID = $L("ModelID");
		if (NumberUtil.isNumber(StringUtil.subString($V("Code"), 1))) {
			fail(Lang.get("Platform.Code.Error2"));
			return;
		}
		Transaction tran = new Transaction();
		if (id != 0) {
			DAOSet<ZDMetaColumn> set = new ZDMetaColumn().query(new Q().where("Code", $V("Code")).and().eq("ModelID", modelID).and()
					.ne("ID", id));
			if (set != null && set.size() > 0) {
				fail(Lang.get("Platform.CodeExists"));
				return;
			}
			ZDMetaColumn mcg = new ZDMetaColumn();
			mcg.setID(id);
			mcg.fill();
			String dataType = mcg.getDataType();
			String targetField = mcg.getTargetField();
			mcg.setValue(Request);
			mcg.setModifyTime(new Date());
			mcg.setModifyUser(User.getUserName());
			if (!dataType.equals(mcg.getDataType())) {
				try {
					String oldTargetField = targetField;
					targetField = MetaModelColumnBL.arrangeTargetField(modelID, mcg.getDataType());
					// 尝试将原字段值复制到新字段
					if (YesOrNo.Yes.equals($V("copy"))) {
						Q q = new Q().update("ZDMetaValue").set().eq2(targetField, oldTargetField).where("ModelID", modelID);
						tran.add(q);
					}
					tran.add(new Q().update("ZDMetaValue").set().eq2(oldTargetField, "null").where("ModelID", modelID));
				} catch (Exception e) {
					fail(Lang.get("Platform.NoMoreColumnCanBeUse"));
					Response.setStatus(2);
					return;
				}
			}
			mcg.setTargetField(targetField);
			tran.update(mcg);
		} else {
			DAOSet<ZDMetaColumn> set = new ZDMetaColumn().query(new Q().where("Code", $V("Code")).and().eq("ModelID", modelID));
			if (set != null && set.size() > 0) {
				fail(Lang.get("Platform.CodeExists"));
				Response.setStatus(2);
				return;
			}
			ZDMetaColumn mcg = new ZDMetaColumn();
			mcg.setID(NoUtil.getMaxID(MetaModelBL.MetaColumnID));
			mcg.setValue(Request);
			mcg.setOrderFlag(new Q().select("count(1)").from("ZDMetaColumn").where("ModelID", modelID).executeLong());
			mcg.setAddTime(new Date());
			mcg.setAddUser(User.getUserName());
			String targetField = null;
			try {
				targetField = MetaModelColumnBL.arrangeTargetField(modelID, mcg.getDataType());
			} catch (Exception e) {
				fail(Lang.get("Platform.NoMoreColumnCanBeUse"));
				Response.setStatus(2);
				return;
			}
			mcg.setTargetField(targetField);
			tran.insert(mcg);
		}
		if (tran.commit()) {
			MetaModel mm = MetaModel.load(modelID);
			PlatformCache.setMetaModel(mm);
			success(Lang.get("Common.ExecuteSuccess"));
		} else {
			fail(Lang.get("Common.ExecuteFailed") + ":" + tran.getExceptionMessage());
		}
	}

	@Priv(MetadataPriv.Delete)
	public void delete() {
		DAOSet<ZDMetaColumn> set = new ZDMetaColumn().query(new Q().where().in("ID", $V("IDs")));
		if (set.deleteAndBackup()) {
			ZDMetaModel mm = null;
			for (ZDMetaColumn schema : set) {
				mm = new ZDMetaModel();
				mm.setID(schema.getModelID());
				if (mm.fill()) {
					PlatformCache.removeMetaModel(mm);
				}
			}
		}
		success(Lang.get("Common.DeleteSuccess"));
	}

	@Priv
	public DataTable getGroups() {
		long id = $L("ModelID");
		if (ObjectUtil.notEmpty(id)) {
			return new Q("select ID,Name from ZDMetaColumnGroup where ModelID=?", id).fetch();
		}
		return null;
	}

	@Priv(MetadataPriv.Save)
	public void sortColumn() {
		long targetOrderFlag = $L("TargetOrderFlag");
		long id = $L("ID");
		if (id == 0 || targetOrderFlag < 0) {
			fail(Lang.get("Common.ExecuteFailed"));
			return;
		}
		ZDMetaColumn schema = new ZDMetaColumn();
		schema.setID(id);
		if (!schema.fill()) {
			fail(Lang.get("Common.ExecuteFailed"));
			return;
		}
		boolean refreshSort = false;
		Q wherePart = new Q().and().eq("ModelID", schema.getModelID());
		if (schema.getOrderFlag() == targetOrderFlag) {// 如果原排序位置和目标位置相同
			String newRowIndex = $V("newRowIndex");
			if (newRowIndex != null && !newRowIndex.equals($V("oldRowIndex"))) {// 并且原行号和目标行号相同
				refreshSort = true;
				OrderUtil.refreshSort(schema.table(), "ID", "OrderFlag", wherePart, Current.getTransaction());
			}
		} else {
			OrderUtil.updateOrder(new ZDMetaColumn().table(), "OrderFlag", targetOrderFlag, wherePart, schema, Current.getTransaction());
		}
		if (Current.getTransaction().commit()) {
			ZDMetaModel mm = new ZDMetaModel();
			mm.setID(schema.getModelID());
			if (mm.fill()) {
				PlatformCache.removeMetaModel(mm);
			}
			success(refreshSort ? Lang.get("Platform.OrderFlagErrorAndRefreshed") : Lang.get("Common.ExecuteSuccess"));
		} else {
			fail(Lang.get("Common.ExecuteFailed"));
		}
	}

	@Priv
	public DataTable getControlTypes() {
		return MetadataColumnControlTypeService.getColumnControlTypeNameMap().toDataTable();
	}

}
