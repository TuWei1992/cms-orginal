package com.zving.platform.meta.bl;

import java.util.Date;
import java.util.List;

import com.zving.framework.User;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.i18n.Lang;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.utility.Errorx;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.meta.IMetaModelTemplateType;
import com.zving.platform.meta.IMetaModelType;
import com.zving.platform.meta.MetadataService;
import com.zving.platform.point.BeforeMetaModelDelete;
import com.zving.platform.point.BeforeMetaModelSave;
import com.zving.platform.util.NoUtil;
import com.zving.schema.ZDMetaColumn;
import com.zving.schema.ZDMetaColumnGroup;
import com.zving.schema.ZDMetaModel;
import com.zving.schema.ZDMetaValue;
import com.zving.schema.ZDModelTemplate;

public class MetaModelBL {

	public final static String MetaModelID = "MetaModelID";
	public final static String MetaColumnID = "MetaColumnID";
	public final static String MetaColumnGroupID = "MetaColumnGroupID";

	public static boolean isNameExists(String modelName, String modelCode, long modelID) {
		Q q = new Q().select("count(1)").from("ZDMetaModel").where().braceLeft().eq("Name", modelName).or().eq("Code", modelCode)
				.braceRight();
		if (modelID != 0) {
			q.and().ne("ID", modelID);
		}
		return q.executeInt() > 0;
	}

	public static void insert(Mapx<String, Object> params, Transaction trans) {
		ZDMetaModel mm = new ZDMetaModel();
		mm.setValue(params);
		mm.setID(NoUtil.getMaxID(MetaModelID));
		trans.add(mm, Transaction.INSERT);
		mm.setAddTime(new Date());
		mm.setAddUser(User.getUserName());
		
		// 如果是类似创建
		if (ObjectUtil.notEmpty(params.get("CopyID"))) {
			Q q = new Q().where("ModelID", params.getLong("CopyID"));
			DAOSet<ZDMetaColumn> columnSet = new ZDMetaColumn().query(q);
			for (ZDMetaColumn c : columnSet) {
				c.setID(NoUtil.getMaxID(MetaColumnID));
				c.setModelID(mm.getID());
				c.setAddTime(new Date());
				c.setAddUser(User.getUserName());
				c.setModifyTime(c.getAddTime());
				c.setModifyUser(c.getAddUser());
			}
			DAOSet<ZDMetaColumnGroup> groupSet = new ZDMetaColumnGroup().query(q);
			for (ZDMetaColumnGroup g : groupSet) {
				g.setID(NoUtil.getMaxID("MetaColumnGroupID"));
				g.setModelID(mm.getID());
				g.setAddTime(new Date());
				g.setAddUser(User.getUserName());
				g.setModifyTime(g.getAddTime());
				g.setModifyUser(g.getAddUser());
			}
			DAOSet<ZDModelTemplate> templateSet = new ZDModelTemplate().query(q);
			for (ZDModelTemplate t : templateSet) {
				t.setModelID(mm.getID());
				t.setAddTime(new Date());
				t.setAddUser(User.getUserName());
				t.setModifyTime(t.getAddTime());
				t.setModifyUser(t.getAddUser());
			}
			trans.insert(columnSet);
			trans.insert(groupSet);
			trans.insert(templateSet);
		}else{
			IMetaModelType metaType = MetadataService.getInstance().get(mm.getOwnerType());
			List<IMetaModelTemplateType> ts = metaType.getTemplateTypes();
			for (IMetaModelTemplateType mtt : ts){
				ZDModelTemplate template = new ZDModelTemplate();
				template.setModelID(mm.getID());
				template.setAddTime(new Date());
				template.setAddUser(User.getUserName());
				template.setTemplateType(mtt.getID());
				template.setTemplateContent(metaType.getDefautlTemplateHtml());
				trans.add(template,Transaction.INSERT);
			}
		}
	}

	public static void save(Mapx<String, Object> params, Transaction trans) {
		ZDMetaModel mm = new ZDMetaModel();
		mm.setID(params.getLong("ID"));
		mm.fill();
		String oldModeCode = mm.getCode();
		trans.add(mm.clone(), Transaction.BACKUP);

		mm.setValue(params);
		trans.add(mm, Transaction.UPDATE);
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
				Errorx.addError(sb.toString());
				return;
			}
		}
	}

	public static DAOSet<ZDMetaModel> delete(String ids, Transaction trans) {
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
				Errorx.addError(sb.toString());
				return null;
			}
		}
		return set;
	}
}
