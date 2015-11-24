package com.zving.platform.meta;

import com.zving.framework.collection.Mapx;
import com.zving.framework.data.Q;
import com.zving.framework.orm.DAOSet;
import com.zving.schema.ZDMetaColumn;
import com.zving.schema.ZDMetaColumnGroup;
import com.zving.schema.ZDMetaModel;
import com.zving.schema.ZDModelTemplate;

/*
 * @Author 王育春
 * @Date 2010-10-13
 * @Mail wyuch@zving.com
 */
public class MetaModel {
	private Mapx<String, ZDMetaColumn> mapping;
	private Mapx<String, String> converseMapping;
	private DAOSet<ZDMetaColumn> columns;
	private DAOSet<ZDMetaColumnGroup> groups;
	private ZDMetaModel schema;
	private DAOSet<ZDModelTemplate> templates;

	public static MetaModel load(long id) {
		ZDMetaModel mm = new ZDMetaModel();
		mm.setID(id);
		if (!mm.fill()) {
			return null;
		}
		return load(mm);
	}

	public static MetaModel load(String modelCode) {
		DAOSet<ZDMetaModel> set = new ZDMetaModel().query(new Q("where Code=?", modelCode));
		if (set == null || set.size() == 0) {
			return null;
		}
		return load(set.get(0));
	}

	private static MetaModel load(ZDMetaModel mm) {
		MetaModel model = new MetaModel();
		model.schema = mm;
		model.columns = new ZDMetaColumn().query(new Q("where ModelID=?", mm.getID()));
		model.groups = new ZDMetaColumnGroup().query(new Q("where ModelID=?", mm.getID()));
		model.templates = new ZDModelTemplate().query(new Q("where ModelID=?", mm.getID()));

		model.mapping = new Mapx<String, ZDMetaColumn>();
		model.converseMapping = new Mapx<String, String>();
		for (ZDMetaColumn mc : model.columns) {
			model.mapping.put(mc.getCode(), mc);
			model.converseMapping.put(mc.getTargetField(), mc.getCode());
		}
		return model;
	}

	public ZDModelTemplate getTemplateByType(String templateType) {
		for (ZDModelTemplate mt : templates) {
			if (mt.getTemplateType().equals(templateType)) {
				return mt;
			}
		}
		return null;
	}

	public Mapx<String, ZDMetaColumn> getMapping() {
		return mapping;
	}

	public Mapx<String, String> getConverseMapping() {
		return converseMapping;
	}

	public DAOSet<ZDMetaColumn> getColumns() {
		return columns;
	}

	public DAOSet<ZDMetaColumnGroup> getGroups() {
		return groups;
	}

	public ZDMetaModel getDAO() {
		return schema;
	}

	public void setTemplates(DAOSet<ZDModelTemplate> templates) {
		this.templates = templates;
	}

	public DAOSet<ZDModelTemplate> getTemplates() {
		return templates;
	}

}
