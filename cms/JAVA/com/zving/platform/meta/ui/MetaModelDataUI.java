package com.zving.platform.meta.ui;

import java.util.ArrayList;

import com.zving.framework.Current;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.i18n.Lang;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.ui.tag.ListAction;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.code.ControlType;
import com.zving.platform.code.DataType;
import com.zving.platform.meta.MetaUtil;
import com.zving.platform.meta.MetadataService;
import com.zving.platform.meta.bl.MetadataBL;
import com.zving.platform.meta.control.MetadataDateColumn;
import com.zving.platform.meta.control.MetadataDateTimeColumn;
import com.zving.platform.meta.control.MetadataRichTextColumn;
import com.zving.platform.privilege.MetadataPriv;
import com.zving.platform.util.PlatformCache;
import com.zving.schema.ZDMetaColumn;
import com.zving.schema.ZDMetaValue;

/**
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-2-16
 */
@Alias("MetaModelData")
public class MetaModelDataUI extends UIFacade {
	@Priv
	public void bindGrid(DataGridAction dga) {
		long id = $L("ID");
		if (id == 0) {
			return;
		}
		DataTable dt = new DataTable();
		dt.insertColumn("Name");
		dt.insertColumn("Code");
		dt.insertColumn("Format");
		dt.insertRow(new Object[] { "PKValue", "PKValue", null });

		DAOSet<ZDMetaColumn> set = PlatformCache.getMetaModel(id).getColumns();
		for (ZDMetaColumn column : set) {
			if (column.getControlType().equals("DateTime")) {
				dt.insertRow(new Object[] { column.getName(), column.getCode(), "yyyy-MM-dd HH:mm:ss" });
			} else if (column.getControlType().equals("Date")) {
				dt.insertRow(new Object[] { column.getName(), column.getCode(), "yyyy-MM-dd" });
			} else {
				dt.insertRow(new Object[] { column.getName(), column.getCode(), null });

			}
		}
		$S("ColumnData", dt);
		dga.bindData(getSelectQ(id));
	}

	@Priv
	public void init() {
		long id = $L("ID");
		if (id == 0) {
			return;
		}
		String pk = $V("PKValue");
		if (ObjectUtil.empty(pk)) {
			return;
		}
		Q q = getSelectQ(id);
		q.and().eq("PKValue", pk);
		DataTable dt = q.fetch();
		if (dt.getRowCount() > 0) {
			Mapx<String, Object> map = dt.getDataRow(0).toCaseIgnoreMapx();
			for (String k : map.keySet()) {
				$S(MetadataService.ControlPrefix + k, map.get(k));
			}
		}
	}

	@Priv
	public void bindGroupList(ListAction la) {
		long id = $L("ID");
		if (id == 0) {
			return;
		}
		DataTable dt = PlatformCache.getMetaModel(id).getGroups().toDataTable();
		la.bindData(dt);
	}

	@Priv
	public void bindFieldList(ListAction la) {
		long id = $L("ID");
		if (id == 0) {
			return;
		}
		long groupID = la.getParentCurrentDataRow().getLong("ID");
		DataTable dt = MetaUtil.getControlHTML(id, groupID);
		la.bindData(dt);
	}

	@Priv(MetadataPriv.AddData + "||" + MetadataPriv.EditData)
	public void save() {
		long id = $L("ModelID");
		String pk = $V("PKValue");
		String oldPk = $V("OldPKValue");
		if (id == 0 || ObjectUtil.empty(pk)) {
			fail(Lang.get("Platform.MetaModel.PKValueAndModelIDCanNotBeNull"));
			return;
		}
		ZDMetaValue mv = new ZDMetaValue();
		mv.setPKValue(pk);
		mv.setModelID(id);
		if (!pk.equals(oldPk) && mv.fill()) {
			fail(Lang.get("Platform.MetaModel.DuplicatePKValue"));
			return;
		}
		MetadataBL.addMetadata(Request, Current.getTransaction());
		if (Current.getTransaction().commit()) {
			success(Lang.get("Common.SaveSuccess"));
		} else {
			success(Lang.get("Common.SaveFailed") + ":" + Current.getTransaction().getExceptionMessage());
		}
	}

	@Priv(MetadataPriv.DeleteData)
	public void delete() {
		long id = $L("ID");
		if (id == 0) {
			return;
		}
		String pks = $V("PKValues");
		if (ObjectUtil.empty(pks)) {
			return;
		}
		Q q = new Q().where("ModelID", id).and().in("PKValue", pks);
		DAOSet<ZDMetaValue> set = new ZDMetaValue().query(q);
		MetadataBL.deleteMetadata(set, Current.getTransaction());
		if (Current.getTransaction().commit()) {
			success(Lang.get("Common.DeleteSuccess"));
		} else {
			success(Lang.get("Common.DeleteFailed"));
		}
	}

	public static Q getSelectQ(long id) {
		DAOSet<ZDMetaColumn> set = PlatformCache.getMetaModel(id).getColumns();
		if (set.size() == 0) {
			return new Q().select("*").from("ZDMetaValue").where("1", "2");
		}
		ArrayList<String> columns = new ArrayList<String>();
		columns.add("ModelID");
		columns.add("PKValue");
		for (ZDMetaColumn column : set) {
			columns.add(column.getTargetField() + " as \"" + column.getCode() + "\"");
		}
		String table = PlatformCache.getMetaModel(id).getDAO().getTargetTable();
		Q q = new Q().select(columns).from(table).where("ModelID", id);
		return q;
	}
	
	/*
	 * 根据传入字段的字段名和值获取满足条件的扩展数据  
	 * @param id 扩展模型ID
	 * @param code 扩展字段代码
	 * @param value 查询的值
	 * @param type 扩展字段类型
	*/
	public static Q getSelectQ(long id, String code, String value, String type, String dataType, String searchStart, String searchend) {
		DAOSet<ZDMetaColumn> set = PlatformCache.getMetaModel(id).getColumns();
		if (set.size() == 0) {
			return new Q().select("*").from("ZDMetaValue").where("1", "2");
		}
		ArrayList<String> columns = new ArrayList<String>();
		columns.add("ModelID");
		columns.add("PKValue");
		String searchColumn = "";
		for (ZDMetaColumn column : set) {
			columns.add(column.getTargetField() + " as \"" + column.getCode() + "\"");
			if(StringUtil.isNotEmpty(code) && code.equalsIgnoreCase(column.getCode())){
				searchColumn = column.getTargetField();
			}
		}
		String table = PlatformCache.getMetaModel(id).getDAO().getTargetTable();
		Q q = new Q().select(columns).from(table).where("ModelID", id);
		if(StringUtil.isNotEmpty(searchColumn) ){
			if(type.equalsIgnoreCase(ControlType.Text) || type.equalsIgnoreCase(ControlType.TextArea)){
				if(DataType.Long.equalsIgnoreCase(dataType) || DataType.Double.equalsIgnoreCase(dataType)){
					if(StringUtil.isNotEmpty(searchStart) && !"null".equalsIgnoreCase(searchStart)){
						q.and().ge(searchColumn, searchStart);
					}
					if(StringUtil.isNotEmpty(searchend)  && !"null".equalsIgnoreCase(searchend)){
						q.and().le(searchColumn, searchend);
					}
				} else {
					if(StringUtil.isEmpty(value)) return q;
					q.and().like(searchColumn, value);
				}
			} else if(type.equalsIgnoreCase(MetadataDateColumn.ID)){
				if(StringUtil.isNotEmpty(searchStart) && !"null".equalsIgnoreCase(searchStart)){
					q.and().ge(searchColumn, searchStart);
				}
				if(StringUtil.isNotEmpty(searchend)  && !"null".equalsIgnoreCase(searchend)){
					searchend += " 23:59:59";
					q.and().le(searchColumn, searchend);
				}
			}  else if(type.equalsIgnoreCase(MetadataDateTimeColumn.ID)){
				if(StringUtil.isNotEmpty(searchStart) && !"null".equalsIgnoreCase(searchStart)){
					q.and().ge(searchColumn, searchStart);
				}
				if(StringUtil.isNotEmpty(searchend)  && !"null".equalsIgnoreCase(searchend)){
					q.and().le(searchColumn, searchend);
				}
			} else if(StringUtil.isNotEmpty(value)) {
				//非文本类型字段，如果数据类型是数值，直接返回空
				if(DataType.Long.equalsIgnoreCase(dataType) || DataType.Double.equalsIgnoreCase(dataType)){
					return q.and().append(" 1=2");
				}
				if(type.equalsIgnoreCase(MetadataRichTextColumn.ID)){
					q.and().like(searchColumn, value);
				} else if(type.equalsIgnoreCase(ControlType.Radio) || type.equalsIgnoreCase(ControlType.Selector)){
					q.and().eq(searchColumn, value);
				} 
			}
		}
		return q;
	}
}
