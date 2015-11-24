package com.zving.platform.meta.bl;

import java.util.Date;
import java.util.List;

import com.zving.framework.Config;
import com.zving.framework.User;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.meta.IMetaModelTemplateType;
import com.zving.platform.meta.IMetaModelType;
import com.zving.platform.meta.MetaModel;
import com.zving.platform.meta.MetadataService;
import com.zving.platform.meta.ModelTemplateService;
import com.zving.platform.util.PlatformCache;
import com.zving.schema.ZDModelTemplate;

public class MetaModelTemplateBL {

	public static String getPreviewHtml(long modelID, String templateTypeID) {
		ZDModelTemplate mt = new ZDModelTemplate();
		mt.setModelID(modelID);
		mt.setTemplateType(templateTypeID);
		mt.fill();
		// 如果未设置模板内容则使用默认模板
		MetaModel mm = MetaModel.load(modelID);
		if (mm == null) {
			return null;
		}
		String templateHtml = mt.getTemplateContent();
		if (StringUtil.isEmpty(templateHtml)) {
			templateHtml = MetadataService.getInstance().get(mm.getDAO().getOwnerType()).getDefautlTemplateHtml();
		}
		String html = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n"
				+ "<html><head>\n" + "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=8, IE=9, IE=10, chrome=1\" />\n"
				+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" + "<link href=\"" + Config.getContextPath()
				+ "style/default.css\" rel=\"stylesheet\" type=\"text/css\" />\n" + "<link href=\"" + Config.getContextPath()
				+ "style/zvingclassic/default.css\" rel=\"stylesheet\" type=\"text/css\" />\n" + "<script src=\"" + Config.getContextPath()
				+ "framework/main.js\"></script>\n" + "<script src=\"" + Config.getContextPath() + "editor/ueditor.config.js\"></script>\n"
				+ "<script src=\"" + Config.getContextPath() + "editor/ueditor.all.js\"></script>\n"
				+ "</head>\n<body class=\"dialogBody\">\n" + "<div style=\"padding:20px;\">\n"
				+ ModelTemplateService.parseModelTemplate(modelID, templateHtml) + "</div>\n</body>\n</html>";
		return html;
	}

	public static void save(Mapx<String, Object> params, Transaction trans) {
		long modelID = params.getLong("ID");
		MetaModel mm = PlatformCache.getMetaModel(modelID);
		IMetaModelType modelType = MetadataService.getInstance().get(mm.getDAO().getOwnerType());
		List<IMetaModelTemplateType> tmplTypes = modelType.getTemplateTypes();
		for (IMetaModelTemplateType mmtt : tmplTypes) {
			ZDModelTemplate mt = new ZDModelTemplate();
			mt.setModelID(modelID);
			mt.setTemplateType(mmtt.getID());
			mt.setTemplateContent(params.getString("ModelTmpl_" + mmtt.getID()));
			mt.setAddTime(new Date());
			mt.setAddUser(User.getUserName());
			if (StringUtil.isEmpty(mt.getTemplateContent())) {
				trans.delete(mt);
			} else {
				trans.deleteAndInsert(mt);
			}
		}
	}

	public static DataTable getTemplateTypes(long modelID) {
		MetaModel mm = PlatformCache.getMetaModel(modelID);
		IMetaModelType modelType = MetadataService.getInstance().get(mm.getDAO().getOwnerType());
		List<IMetaModelTemplateType> tmplTypes = modelType.getTemplateTypes();

		DAOSet<ZDModelTemplate> set = new ZDModelTemplate().query(new Q().where("ModelID", modelID));
		Mapx<String, Object> map = new Mapx<String, Object>();
		if (set != null && set.size() > 0) {
			map = set.toDataTable().toMapx("TemplateType", "TemplateContent");
		}

		DataTable dt = ModelTemplateService.listToDataTable(tmplTypes);
		dt.insertColumn("Template");
		for (DataRow dr : dt) {
			dr.set("Template", map.getString(dr.getString("MMTemplateTypeID")));
		}
		return dt;
	}
}
