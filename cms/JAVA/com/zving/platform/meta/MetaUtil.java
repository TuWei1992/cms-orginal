package com.zving.platform.meta;

import java.util.ArrayList;
import java.util.Collection;

import com.zving.framework.Current;
import com.zving.framework.collection.CaseIgnoreMapx;
import com.zving.framework.collection.Mapx;
import com.zving.framework.core.method.IMethodLocator;
import com.zving.framework.core.method.MethodLocatorUtil;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.orm.DAO;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.security.PrivCheck;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.code.DataType;
import com.zving.platform.code.YesOrNo;
import com.zving.platform.meta.control.MetadataDateTimeColumn;
import com.zving.platform.meta.control.MetadataRichTextColumn;
import com.zving.platform.meta.control.MetadataTextAreaColumn;
import com.zving.platform.util.PlatformCache;
import com.zving.platform.util.PlatformUtil;
import com.zving.schema.ZDMetaColumn;

/*
 * @Author 王育春
 * @Date 2010-10-13
 * @Mail wyuch@zving.com
 */
public class MetaUtil {

	/**
	 * 获取元数据表表单HTML
	 * 
	 * @param modelID
	 * @param groupID
	 * @return
	 */
	public static DataTable getControlHTML(long modelID, long groupID) {
		DAOSet<ZDMetaColumn> set = new DAOSet<ZDMetaColumn>();
		DAOSet<ZDMetaColumn> all = PlatformCache.getMetaModel(modelID).getColumns();
		for (ZDMetaColumn column : all) {
			if (column.getGroupID() != groupID) {
				continue;
			}
			set.add(column);
		}
		DataTable dt = set.toDataTable();
		dt.insertColumn("ControlHTML");
		for (int i = 0; i < dt.getRowCount(); i++) {
			dt.set(i, "ControlHTML", MetaUtil.getControlHTML(set.get(i)));
		}
		return dt;
	}

	/**
	 * 获得指定类别的字段映射
	 */
	public static Mapx<String, ZDMetaColumn> getMapping(long modelID) {
		MetaModel mm = PlatformCache.getMetaModel(modelID);
		if (mm == null) {
			return null;
		}
		return mm.getMapping();
	}

	public static DAO<?> newTargetTableInstance(String targetTable) {
		Class<?> metaValueDAO = null;
		try {
			metaValueDAO = Class.forName("com.zving.schema." + targetTable);
		} catch (Exception e) {
			e.printStackTrace();
		}
		DAO<?> dao = null;
		try {
			if (metaValueDAO == null) {
				return null;
			}
			dao = (DAO<?>) metaValueDAO.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return dao;
	}

	/**
	 * 获得指定类别的逆向字段映射
	 */
	public static Mapx<String, String> getConverseMapping(long modelID) {// NO_UCD
		MetaModel mm = PlatformCache.getMetaModel(modelID);
		if (mm == null) {
			return null;
		}
		return mm.getConverseMapping();
	}

	public static Mapx<String, Object> getExtendData(String PKValue, MetaModel mm) {
		Mapx<String, Object> map = new Mapx<String, Object>();
		if (mm == null) {
			return map;
		}
		Q qb = new Q("select * from " + mm.getDAO().getTargetTable() + " where PKValue=? and ModelID=?", PKValue, mm.getDAO().getID());
		DataTable metaDT = qb.fetch();
		if (metaDT.getRowCount() == 0) {
			return map;
		}
		for (ZDMetaColumn mc : mm.getColumns()) {
			if (mc.getDataType().equals(DataType.Datetime)) {
				map.put(MetadataService.ControlPrefix + mc.getCode(),
						DateUtil.toDateTimeString(DateUtil.parseDateTime(metaDT.getString(0, mc.getTargetField()))));
			} else {
				map.put(MetadataService.ControlPrefix + mc.getCode(), metaDT.get(0, mc.getTargetField()));
			}
		}
		return map;
	}

	public static Mapx<String, Object> getExtendData(String PKValue, String modelCode) {
		MetaModel mm = MetaModel.load(modelCode);
		return getExtendData(PKValue, mm);
	}

	public static Mapx<String, Object> getExtendData(String PKValue, long modelID) {
		MetaModel mm = PlatformCache.getMetaModel(modelID);
		return getExtendData(PKValue, mm);
	}

	public static void deleteExtendData(Transaction tran, String PKValue, long modelID) {
		MetaModel mm = PlatformCache.getMetaModel(modelID);
		if (mm == null) {
			return;
		}
		Q qb = new Q("delete from " + mm.getDAO().getTargetTable() + " where ModelID=? and PKValue=?", modelID, PKValue);
		tran.add(qb);
	}

	public static boolean backupExtendData(String PKValue, long modelID) {
		Transaction tran = new Transaction();
		backupExtendData(tran, PKValue, modelID);
		return tran.commit();
	}

	public static void backupExtendData(Transaction tran, String PKValue, long modelID) {
		MetaModel mm = PlatformCache.getMetaModel(modelID);
		if (mm == null) {
			return;
		}

		DAO<?> dao = newTargetTableInstance(mm.getDAO().getTargetTable());
		dao.setV("ModelID", modelID);
		dao.setV("PKValue", PKValue);
		if (dao.fill()) {
			tran.deleteAndBackup(dao);
		}
	}

	private static String getUpdateSQL(String table, Collection<ZDMetaColumn> cols, long modelID, String pkValue) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append("update ").append(table).append(" set ");
		for (ZDMetaColumn c : cols) {
			if (!first) {
				sb.append(", ");
			} else {
				first = false;
			}
			sb.append(c.getTargetField());
			sb.append("=?");
		}
		sb.append(" where ModelID=").append(modelID).append(" and PKValue='").append(pkValue).append("'");
		return sb.toString();
	}

	private static String getInsertSQL(String table, Collection<ZDMetaColumn> cols, long modelID, String pkValue) {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ");
		sb.append(table);
		sb.append(" (ModelID,PKValue");
		for (ZDMetaColumn col : cols) {
			sb.append(",").append(col.getTargetField());
		}
		sb.append(") values (").append(modelID).append(",'").append(pkValue).append("'");
		for (int i = 0; i < cols.size(); i++) {
			sb.append(",").append("?");
		}
		sb.append(")");
		return sb.toString();
	}

	public static void saveExtendData(Transaction tran, Mapx<String, ?> map, String PKValue, MetaModel mm) {
		if (mm == null) {
			return;
		}
		Mapx<String, ZDMetaColumn> mMap = MetaUtil.getMapping(mm.getDAO().getID());
		if (mMap == null) {
			return;
		}
		String targetTable = mm.getDAO().getTargetTable();
		Q qSelect = new Q("select count(1) from " + targetTable + " where ModelID=? and PKValue=?", mm.getDAO().getID(), PKValue);
		Q q = new Q();
		if (qSelect.executeInt() > 0) {
			q.setSQL(getUpdateSQL(targetTable, mMap.values(), mm.getDAO().getID(), PKValue));
		} else {
			q.setSQL(getInsertSQL(targetTable, mMap.values(), mm.getDAO().getID(), PKValue));
		}

		for (String key : mMap.keySet()) {
			Object v = null;
			if (map.containsKey(MetadataService.ControlPrefix + key)) {
				ZDMetaColumn mc = mMap.get(key);
				v = map.getString(MetadataService.ControlPrefix + key);
				if (mc.getDataType().equals(DataType.Long)) {
					if (ObjectUtil.notEmpty(v)) {
						v = new Long(map.getLong(MetadataService.ControlPrefix + key));
					} else {
						v = null;
					}
				}
				if (mc.getDataType().equals(DataType.Double)) {
					if (ObjectUtil.notEmpty(v)) {
						v = new Double(map.getString(MetadataService.ControlPrefix + key));
					} else {
						v = null;
					}
				}
				if (mc.getDataType().equals(DataType.Datetime)) {
					if (ObjectUtil.notEmpty(v)) {
						v = DateUtil.parseDateTime(map.getString(MetadataService.ControlPrefix + key));
					} else {
						v = null;
					}
				}
				if ((mc.getControlType().equals(MetadataRichTextColumn.ID) || mc.getControlType().equals(MetadataTextAreaColumn.ID))
						&& ObjectUtil.notEmpty(v)) {
					v = StringUtil.htmlDecode(v.toString());
				}
				q.add(v);
			} else {
				q.add(v);
			}
		}
		tran.add(q);
	}

	public static void saveExtendData(Transaction tran, Mapx<String, ?> map, String PKValue, long modelID) {
		MetaModel mm = MetaModel.load(modelID);
		if (mm == null) {
			return;
		}
		saveExtendData(tran, map, PKValue, mm);
	}

	public static void saveExtendData(Transaction tran, Mapx<String, ?> map, String PKValue, String modelCode) {
		MetaModel mm = MetaModel.load(modelCode);
		if (mm == null) {
			return;
		}
		saveExtendData(tran, map, PKValue, mm);
	}

	public static boolean saveExtendData(Mapx<String, ?> map, String PKValue, String modelCode) {
		Transaction tran = new Transaction();
		saveExtendData(tran, map, PKValue, modelCode);
		return tran.commit();
	}

	public static String getControlHTML(ZDMetaColumn mc) {
		StringBuilder sb = new StringBuilder();
		IMetadataColumnControlType controlType = MetadataColumnControlTypeService.getColumnControlTypoe(mc.getControlType());
		sb.append(controlType.getHtml(mc, null));
		return sb.toString();
	}

	public static String getControlHTML(ZDMetaColumn mc, String value) {
		StringBuilder sb = new StringBuilder();
		IMetadataColumnControlType controlType = MetadataColumnControlTypeService.getColumnControlTypoe(mc.getControlType());
		sb.append(controlType.getHtml(mc, value));
		return sb.toString();
	}

	/**
	 * 获取样式
	 * 
	 * @param mc
	 * @return
	 */
	public static String getClass(ZDMetaColumn mc) {
		// 获取样式
		String styleText = "";
		if (StringUtil.isNotEmpty(mc.getStyleClass())) {
			styleText += " class=\"" + mc.getStyleClass() + "\"";
		}
		if (StringUtil.isNotEmpty(mc.getStyleText())) {
			styleText += " style=\"" + mc.getStyleText() + "\"";
		} else {
			styleText += " style=\"width:220px\"";
		}
		return styleText;
	}

	/**
	 * 获取 值
	 * 
	 * @param mc
	 * @return
	 */
	public static String getValue(ZDMetaColumn mc) {
		String value = null;
		String code = MetadataService.ControlPrefix + mc.getCode();
		if (Current.getRequest() != null) {
			Mapx<String, Object> values = new CaseIgnoreMapx<String, Object>(Current.getRequest());
			values.putAll(Current.getResponse());
			value = values.getString(code);

		}
		if (value == null) {
			value = mc.getDefaultValue() == null ? "" : mc.getDefaultValue();
		}
		if (MetadataDateTimeColumn.ID.equals(mc.getControlType()) && value.indexOf(".") > -1) {
			value = value.substring(0, value.indexOf("."));
		}
		return value;
	}

	/**
	 * 获取校验规则
	 * 
	 * @param mc
	 * @return
	 */
	public static String getVerify(ZDMetaColumn mc) {
		// 获取校验规则
		ArrayList<String> verifyList = new ArrayList<String>();
		if (YesOrNo.isYes(mc.getMandatoryFlag())) {
			verifyList.add("NotNull");
		}
		if (mc.getDataType().equals(DataType.Double)) {
			verifyList.add("Number");
		}
		if (mc.getDataType().equals(DataType.Long)) {
			verifyList.add("Int");
		}
		if (StringUtil.isNotEmpty(mc.getVerifyRule())) {
			for (String vr : StringUtil.splitEx(mc.getVerifyRule(), "&&")) {
				if (!verifyList.contains(vr)) {
					verifyList.add(vr);
				}
			}
		}
		String verify = "";
		if (verifyList.size() > 0) {
			verify += " verify=\"" + StringUtil.join(verifyList, "&&") + "\"";
		}
		return verify;
	}

	/**
	 * 解析字段配置信息中的options，并返回对应的Mapx
	 */
	@SuppressWarnings("unchecked")
	public static Mapx<String, Object> options2Mapx(String options) {
		if (StringUtil.isEmpty(options)) {
			return null;
		}
		Mapx<String, Object> map = null;
		String type = options.substring(0, options.indexOf(":"));
		options = options.substring(options.indexOf(":") + 1);
		if (type.equals("Code")) {
			map = PlatformUtil.getCodeMap(options);
			LangUtil.decode(map);
		}
		if (type.equals("Input")) {
			String[] arr = options.split("\\n");
			map = new Mapx<String, Object>();
			for (String element : arr) {
				String str = element.trim();
				if (StringUtil.isNotEmpty(str)) {
					map.put(str, str);
				}
			}
		}
		if (type.equals("Method")) {
			try {
				IMethodLocator m = MethodLocatorUtil.find(options);
				PrivCheck.check(m);
				map = (Mapx<String, Object>) m.execute();
			} catch (Exception e) {
				e.printStackTrace();// 有可能会有方法填写不对的情况
			}
		}
		return map;
	}
}
