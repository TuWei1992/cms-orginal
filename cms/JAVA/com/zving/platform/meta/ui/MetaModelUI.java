package com.zving.platform.meta.ui;

import java.util.Date;

import com.zving.framework.Current;
import com.zving.framework.UIFacade;
import com.zving.framework.User;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.i18n.Lang;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.meta.IMetaModelType;
import com.zving.platform.meta.MetaModel;
import com.zving.platform.meta.MetadataService;
import com.zving.platform.meta.bl.MetaModelBL;
import com.zving.platform.point.BeforeMetaModelDelete;
import com.zving.platform.point.BeforeMetaModelSave;
import com.zving.platform.privilege.MetadataPriv;
import com.zving.platform.util.NoUtil;
import com.zving.platform.util.PlatformCache;
import com.zving.schema.ZDMetaColumn;
import com.zving.schema.ZDMetaColumnGroup;
import com.zving.schema.ZDMetaModel;
import com.zving.schema.ZDMetaValue;
import com.zving.schema.ZDModelTemplate;

/*
 * @Author 王育春
 * @Date 2010-10-14
 * @Mail wyuch@zving.com
 */
@Alias("MetaModel")
public class MetaModelUI extends UIFacade {

	@Priv
	public void init() {
		long id = $L("ID");
		long cipyID = $L("CopyID");
		if (cipyID != 0) {
			id = cipyID;
		}
		if (id != 0) {
			MetaModel mm = PlatformCache.getMetaModel(id);
			Response.putAll(mm.getDAO().toMapx());
			Response.remove("ID");
		} else {
			$S("OwnerID", "0");
			$S("TargetTable", "ZDMetaValue");
		}
	}

	@Priv
	public DataTable getTypes() {
		DataTable dt = new DataTable();
		dt.insertColumn("ID");
		dt.insertColumn("Name");
		for (IMetaModelType type : MetadataService.getInstance().getAll()) {
			if ("Y".equals($V("extend")) && type.isSystemModel()) {
				continue;
			}
			dt.insertRow(new Object[] { type.getExtendItemID(), type.getExtendItemName() });
		}
		return dt;
	}

	@Priv
	public void bindGrid(DataGridAction dga) {
		Q q = new Q().select("*").from("ZDMetaModel");
		if (StringUtil.isNotEmpty(dga.getParam("SearchName"))) {
			q.where().like("Name", dga.getParam("SearchName"));
		}
		if (StringUtil.isNotEmpty(dga.getParam("Type"))) {
			q.where("OwnerType", dga.getParam("Type"));
		}
		q.orderby("AddTime desc");
		dga.bindData(q);
	}

	@Priv(MetadataPriv.Add + "||" + MetadataPriv.Save)
	public void save() {
		Transaction tran = new Transaction();
		String name = $V("Name");
		long id = $L("ID");
		if (id != 0) {
			if (isNameExists(name, id)) {
				fail(Lang.get("Common.Name") + Lang.get("Common.Exists"));
				return;
			}
			DAOSet<ZDMetaModel> set = new ZDMetaModel().query(new Q().where("Code", $V("Code")).and().ne("ID", id));
			if (set != null && set.size() > 0) {
				fail(Lang.get("Platform.CodeExists"));
				return;
			}
			ZDMetaModel mm = new ZDMetaModel();
			mm.setID(id);
			mm.fill();
			String oldModeCode = mm.getCode();
			tran.add(mm.clone(), Transaction.BACKUP);
			mm.setValue(Request);
			tran.add(mm, Transaction.UPDATE);
			mm.setModifyTime(new Date());
			mm.setModifyUser(User.getUserName());
			// 修改校验扩展点，返回校验内容
			Object[] objs = ExtendManager.invoke(BeforeMetaModelSave.ExtendPointID, new Object[] { mm, oldModeCode });
			if (ObjectUtil.notEmpty(objs)) {
				StringBuilder sb = new StringBuilder();
				for (Object obj : objs) {
					sb.append(ObjectUtil.empty(obj) ? "" : Lang.get(obj.toString()));
				}
				if (StringUtil.isNotNull(sb.toString().trim())) {
					fail(sb.toString());
					return;
				}
			}
		} else {
			if (isNameExists(name, 0)) {
				fail(Lang.get("Common.Name") + Lang.get("Common.Exists"));
				return;
			}
			DAOSet<ZDMetaModel> set = new ZDMetaModel().query(new Q().where("Code", $V("Code")));
			if (set != null && set.size() > 0) {
				fail(Lang.get("Platform.CodeExists"));
				return;
			}
			ZDMetaModel mm = new ZDMetaModel();
			id = NoUtil.getMaxID(MetaModelBL.MetaModelID);
			mm.setValue(Request);
			mm.setID(id);
			tran.add(mm, Transaction.INSERT);
			mm.setAddTime(new Date());
			mm.setAddUser(User.getUserName());

			// 如果是类似创建
			if (ObjectUtil.notEmpty($V("CopyID"))) {
				Q q = new Q().where("ModelID", $L("CopyID"));
				DAOSet<ZDMetaColumn> columnSet = new ZDMetaColumn().query(q);
				for (ZDMetaColumn c : columnSet) {
					c.setID(NoUtil.getMaxID(MetaModelBL.MetaColumnID));
					c.setModelID(id);
					c.setAddTime(new Date());
					c.setAddUser(User.getUserName());
					c.setModifyTime(c.getAddTime());
					c.setModifyUser(c.getAddUser());
				}
				DAOSet<ZDMetaColumnGroup> groupSet = new ZDMetaColumnGroup().query(q);
				for (ZDMetaColumnGroup g : groupSet) {
					g.setID(NoUtil.getMaxID(MetaModelBL.MetaColumnGroupID));
					g.setModelID(id);
					g.setAddTime(new Date());
					g.setAddUser(User.getUserName());
					g.setModifyTime(g.getAddTime());
					g.setModifyUser(g.getAddUser());
				}
				tran.insert(columnSet);
				tran.insert(groupSet);
			}
		}
		if (tran.commit()) {
			success(Lang.get("Common.ExecuteSuccess"));
			MetaModel mm = MetaModel.load(id);
			PlatformCache.setMetaModel(mm);
		} else {
			fail(Lang.get("Common.ExecuteFailed") + ":" + tran.getExceptionMessage());
		}
	}

	@Priv(MetadataPriv.Delete)
	public void del() {
		String ids = $V("IDs");
		Transaction trans = Current.getTransaction();
		DAOSet<ZDMetaModel> set = new ZDMetaModel().query(new Q().where().in("ID", ids));
		trans.deleteAndBackup(set);
		// 删除关联数据
		DAOSet<ZDMetaValue> values = new ZDMetaValue().query(new Q().where().in("ModelID", ids));
		trans.deleteAndBackup(values);
		// 删除关联字段
		DAOSet<ZDMetaColumn> columns = new ZDMetaColumn().query(new Q().where().in("ModelID", ids));
		trans.deleteAndBackup(columns);
		// 删除关联字段分组
		DAOSet<ZDMetaColumnGroup> columnGroups = new ZDMetaColumnGroup().query(new Q().where().in("ModelID", ids));
		trans.deleteAndBackup(columnGroups);
		// 删除关联模板
		DAOSet<ZDModelTemplate> templates = new ZDModelTemplate().query(new Q().where().in("ModelID", ids));
		trans.deleteAndBackup(templates);

		// 删除校验扩展点，返回校验内容
		Object[] objs = ExtendManager.invoke(BeforeMetaModelDelete.ExtendPointID, new Object[] { set });
		if (ObjectUtil.notEmpty(objs)) {
			StringBuilder sb = new StringBuilder();
			for (Object obj : objs) {
				sb.append(ObjectUtil.empty(obj) ? "" : obj.toString());
			}
			if (sb.length() > 0) {
				fail(sb.toString());
				return;
			}
		}
		if (trans.commit()) {
			for (int i = 0; i < set.size(); i++) {
				PlatformCache.removeMetaModel(set.get(i));
			}
			success(Lang.get("Common.DeleteSuccess"));
		} else {
			success(Lang.get("Common.DeleteSuccess"));
		}
	}

	@Priv
	public DataTable getCodeTypes() {
		Q q = new Q().select("CodeType,CodeName").from("ZDCode").where("ParentCode", "System");
		DataTable dt = q.fetch();
		LangUtil.decode(dt, "CodeName");
		return dt;
	}

	private boolean isNameExists(String name, long id) {
		Q q = new Q().select("count(1)").from("ZDMetaModel").where("Name", name);
		if (id != 0) {
			q.and().ne("ID", id);
		}
		int count = q.executeInt();
		if (count > 0) {
			return true;
		}
		return false;
	}
}
