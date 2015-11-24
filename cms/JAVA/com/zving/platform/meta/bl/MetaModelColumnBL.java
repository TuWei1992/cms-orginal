package com.zving.platform.meta.bl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.zving.framework.User;
import com.zving.framework.collection.ConcurrentMapx;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.i18n.Lang;
import com.zving.framework.orm.DAOColumn;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.orm.DAOUtil;
import com.zving.framework.thirdparty.commons.ArrayUtils;
import com.zving.framework.utility.Errorx;
import com.zving.framework.utility.ObjectUtil;
import com.zving.platform.code.DataType;
import com.zving.platform.code.YesOrNo;
import com.zving.platform.meta.MetaModel;
import com.zving.platform.meta.MetaUtil;
import com.zving.platform.util.NoUtil;
import com.zving.platform.util.OrderUtil;
import com.zving.platform.util.PlatformCache;
import com.zving.schema.ZDMetaColumn;
import com.zving.schema.ZDMetaColumnGroup;
import com.zving.schema.ZDMetaModel;

public class MetaModelColumnBL {

	// 可用目标字段<TableName, FieldNames>
	public static Mapx<String, List<String>> fieldMap = new ConcurrentMapx<String, List<String>>();

	public static boolean isColumnGroupCodeExists(String code, long id) {
		Q q = new Q().select("count(1)").from("ZDMetaColumnGroup").where("Code", code);
		if (id != 0) {
			q.and().ne("ID", id);
		}
		return q.executeInt() > 0;
	}

	public static boolean isColumnCodeExists(String code, long id, long modelID) {
		Q q = new Q().select("count(1)").from("ZDMetaColumn").where("Code", code).and().eq("ModelID", modelID);
		if (ObjectUtil.notEmpty(id)) {
			q.and().ne("ID", id);
		}
		return q.executeInt() > 0;
	}

	public static void saveColumnGroup(Mapx<String, Object> params, Transaction trans) {
		long id = params.getLong("ID");
		if (id == 0) {
			ZDMetaColumnGroup mcg = new ZDMetaColumnGroup();
			mcg.setID(NoUtil.getMaxID(MetaModelBL.MetaColumnGroupID));
			mcg.setValue(params);
			mcg.setAddTime(new Date());
			mcg.setAddUser(User.getUserName());
			trans.insert(mcg);
		} else {
			ZDMetaColumnGroup mcg = new ZDMetaColumnGroup();
			mcg.setID(id);
			mcg.fill();
			mcg.setValue(params);
			mcg.setModifyTime(new Date());
			mcg.setModifyUser(User.getUserName());
			trans.update(mcg);
		}
	}

	public static DAOSet<ZDMetaColumnGroup> deleteColumnGroup(String ids, Transaction trans) {
		DAOSet<ZDMetaColumnGroup> set = new ZDMetaColumnGroup().query(new Q().where().in("ID", ids));
		trans.deleteAndBackup(set);
		return set;
	}

	public static void saveColumn(Mapx<String, Object> params, Transaction trans) {
		String id = params.getString("ID");
		if (ObjectUtil.notEmpty(id)) {
			ZDMetaColumn mcg = new ZDMetaColumn();
			mcg.setID(id);
			mcg.fill();
			String dataType = mcg.getDataType();
			String targetField = mcg.getTargetField();
			mcg.setValue(params);
			mcg.setModifyTime(new Date());
			mcg.setModifyUser(User.getUserName());
			if (!dataType.equals(mcg.getDataType())) {
				try {
					String oldTargetField = targetField;
					targetField = arrangeTargetField(params.getLong("ModelID"), mcg.getDataType());
					// 尝试将原字段值复制到新字段
					if (YesOrNo.Yes.equals(params.getString("copy"))) {
						Q q = new Q().update("ZDMetaValue").set().eq2(targetField, oldTargetField)
								.where("ModelID", params.getLong("ModelID"));
						trans.add(q);
					}
					Q q = new Q().update("ZDMetaValue").set().eq2(oldTargetField, "null").where("ModelID", params.getLong("ModelID"));
					trans.add(q);
				} catch (Exception e) {
					Errorx.addError(Lang.get("Platform.NoMoreColumnCanBeUse"));
					return;
				}
			}
			mcg.setTargetField(targetField);
			trans.update(mcg);
		} else {
			ZDMetaColumn mcg = new ZDMetaColumn();
			mcg.setID(NoUtil.getMaxID(MetaModelBL.MetaColumnID));
			mcg.setValue(params);
			mcg.setOrderFlag(OrderUtil.getDefaultOrder());
			mcg.setAddTime(new Date());
			mcg.setAddUser(User.getUserName());
			String targetField = null;
			try {
				targetField = arrangeTargetField(params.getLong("ModelID"), mcg.getDataType());
			} catch (Exception e) {
				Errorx.addError(Lang.get("Platform.NoMoreColumnCanBeUse"));
				return;
			}
			mcg.setTargetField(targetField);
			trans.insert(mcg);
		}
	}

	public static DAOSet<ZDMetaColumn> deleteColumn(String ids, Transaction trans) {
		DAOSet<ZDMetaColumn> set = new ZDMetaColumn().query(new Q().where().in("ID", ids));
		trans.deleteAndBackup(set);
		return set;
	}

	/**
	 * 加载可用目标字段
	 */
	private static void loadFieldMap() {
		DAOSet<ZDMetaModel> mms = new ZDMetaModel().query();
		for (ZDMetaModel mm : mms) {
			if (fieldMap.containsKey(mm.getTargetTable())) {
				continue;
			}
			try {// 目标元数据表可以自定义，但是字段命名方式必须沿用ZDMetaValue表：DataType+数字
				DAOColumn[] cols = DAOUtil.getColumns(MetaUtil.newTargetTableInstance(mm.getTargetTable()));
				List<String> list = new ArrayList<String>();
				for (int i = 2; i < cols.length; i++) {
					list.add(cols[i].getColumnName());
				}
				fieldMap.put(mm.getTargetTable(), list);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 按数据类型为逻辑字段安排物理字段。
	 * 注意：只有在新建字段或者字段数据类型改变时才调用此方法。
	 * 
	 * @param modelID 模型ID
	 * @param dataType 数据类型
	 */
	public static String arrangeTargetField(long modelID, String dataType) throws Exception {
		if (fieldMap.size() == 0) {
			loadFieldMap();
		}
		String newTargetField = dataType;
		if (DataType.Datetime.equals(newTargetField)) {
			newTargetField = "Date";
		}
		// 可用字段集合
		MetaModel mm = PlatformCache.getMetaModel(modelID);
		List<String> fields = fieldMap.get(mm.getDAO().getTargetTable());

		// 已使用field集合
		Mapx<String, ZDMetaColumn> mapping = MetaUtil.getMapping(modelID);
		String[] usedField = new String[mapping.size()];
		int i = 0;
		for (ZDMetaColumn c : mapping.values()) {
			usedField[i++] = c.getTargetField();
		}
		// 确定目标字段名称
		i = 1;
		while (true) {
			if (!ArrayUtils.contains(usedField, newTargetField + i)) {
				newTargetField += i;
				break;
			}
			i++;
		}
		// 是否数据库存在该字段
		if (!fields.contains(newTargetField)) {
			throw new Exception("no such field '" + newTargetField + "' in table " + mm.getDAO().getTargetTable());
		}
		return newTargetField;
	}

}
