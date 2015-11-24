package com.zving.platform.meta.bl;

import com.zving.framework.collection.Mapx;
import com.zving.framework.data.Transaction;
import com.zving.framework.orm.DAO;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.utility.ObjectUtil;
import com.zving.platform.meta.MetaModel;
import com.zving.platform.meta.MetaUtil;
import com.zving.platform.meta.MetadataService;
import com.zving.platform.util.PlatformCache;
import com.zving.schema.ZDMetaColumn;
import com.zving.schema.ZDMetaValue;

public class MetadataBL {

	/**
	 * 添加元数据
	 * 
	 * @param params
	 * @param trans
	 */
	public static String addMetadata(Mapx<String, Object> params, Transaction trans) {
		long id = params.getLong("ModelID");
		String pk = params.getString("PKValue");
		String oldPk = params.getString("OldPKValue");
		MetaModel mm = PlatformCache.getMetaModel(id);
		if (mm == null) {
			return null;
		}

		Mapx<String, Object> map = new Mapx<String, Object>();
		map.put("ModelID", id);
		map.put("PKValue", pk);
		Mapx<String, ZDMetaColumn> mapping = mm.getMapping();
		for (String k : params.keySet()) {
			if (k.startsWith(MetadataService.ControlPrefix)) {
				String k2 = k.substring(10);
				if (mapping.get(k2) == null) {
					continue;// 下拉框等控件可能会生成一些辅助用的表单元素，要排除过这些元素
				}
				k2 = mapping.get(k2).getTargetField();
				map.put(k2, params.get(k));
			}
		}

		DAO<?> dao = MetaUtil.newTargetTableInstance(mm.getDAO().getTargetTable());
		if (ObjectUtil.empty(oldPk)) {
			dao.setValue(map);
			trans.insert(dao);
		} else {
			if (pk.equals(oldPk)) {
				dao.setValue(map);
				trans.update(dao);
			} else {
				dao.setV("ModelID", id);
				dao.setV("PKValue", oldPk);
				dao.fill();// 这样才能保证更新原记录
				dao.setValue(map);
				trans.update(dao);
			}
		}
		return dao.getV("PKValue").toString();
	}

	/**
	 * 删除元数据
	 * 
	 * @param params
	 * @param trans
	 */
	public static void deleteMetadata(DAOSet<ZDMetaValue> set, Transaction trans) {
		trans.deleteAndBackup(set);
	}
}
