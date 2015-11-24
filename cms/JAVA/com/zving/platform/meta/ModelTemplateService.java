package com.zving.platform.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.utility.StringUtil;
import com.zving.schema.ZDMetaColumn;
import com.zving.schema.ZDMetaColumnGroup;

public class ModelTemplateService {

	public static final Pattern FieldGroupCodePattern = Pattern.compile("<model:fieldgroup .*?code=\"(.*?)\">", Pattern.CASE_INSENSITIVE
			| Pattern.DOTALL);

	public static final Pattern FieldCodePattern = Pattern.compile("<model:field .*?code=\"(.*?)\">", Pattern.CASE_INSENSITIVE
			| Pattern.DOTALL);

	public static final String DefaultTemplate = "<model:fieldgroup>"
			+ "<fieldset class=\"extend\">"
			+ "<legend ><b>@{FieldGroup.Name}</b></legend>"
			+ "<table id=\"table@{FieldGroup.Code}\" width=\"650\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" bordercolor=\"#eeeeee\">"
			+ "<model:field>" + "<tr><td width=\"120\" align=\"right\">@{Field.Name}：</td><td>@{Field.ControlHtml}</td></tr>" + "</model:field>"
			+ "</table>" + "</fieldset>" + "</model:fieldgroup>";

	/**
	 * 解析扩展模型模板
	 * 
	 * @param modelID
	 * @param template
	 * @return
	 */
	public static String parseModelTemplate(long modelID, String template) {
		template = parseModelFieldGroupTag(template, modelID);
		template = parseModelFieldTag(template, modelID);
		return template;
	}

	/**
	 * 解析model:fieldgroup标签
	 */
	private static String parseModelFieldGroupTag(String tmpl, long modelID) {
		StringBuilder sb = new StringBuilder();
		while (tmpl.indexOf("<model:fieldgroup") > -1) {
			int beginIndex = tmpl.indexOf("<model:fieldgroup");
			int endIndex = tmpl.indexOf("</model:fieldgroup>", beginIndex);
			sb.append(tmpl.substring(0, beginIndex));

			String tagStr = tmpl.substring(beginIndex, endIndex + 19);
			// 处理code
			Matcher matcher = FieldGroupCodePattern.matcher(tagStr);
			String code = matcher.find() ? matcher.group(1) : "";
			Q qb = new Q("where ModelID=?", modelID);
			if (StringUtil.isNotEmpty(code)) {
				qb.append(" and Code=?", code);
			}
			DAOSet<ZDMetaColumnGroup> groups = new ZDMetaColumnGroup().query(qb);

			// 替换标签内占位符
			String body = tagStr.substring(tagStr.indexOf(">") + 1, tagStr.indexOf("</model:fieldgroup>"));
			for (ZDMetaColumnGroup group : groups) {
				String newBody = body;
				Mapx<String, Object> groupMap = group.toMapx();
				for (String key : groupMap.keySet()) {
					newBody = StringUtil.replaceEx(newBody, "@{FieldGroup." + key + "}", groupMap.getString(key));
				}
				newBody = parseModelFieldTag(newBody, modelID, group.getID());
				sb.append(newBody);
			}

			tmpl = tmpl.substring(endIndex + 19);
		}
		sb.append(tmpl);
		return sb.toString();
	}

	/**
	 * 解析model:fieldgroup标签内的model:field标签
	 */
	private static String parseModelFieldTag(String tmpl, long modelID, long groupID) {
		// ZCMS-2821扩展模型中字段的排序发生变化，配置该扩展模型的对应栏目新建文章时，发现编辑器中扩展模型的字段顺序不一致
		Q qb = new Q("where ModelID=? and GroupID=? order by OrderFlag", modelID, groupID);
		return parseModelFieldTag(tmpl, qb);
	}

	/**
	 * 解析不包含在model:fieldgroup标签内的model:field标签
	 */
	private static String parseModelFieldTag(String tmpl, long modelID) {
		Q qb = new Q("where ModelID=?", modelID);
		return parseModelFieldTag(tmpl, qb);
	}

	@SuppressWarnings("unchecked")
	private static String parseModelFieldTag(String tmpl, Q qb) {
		StringBuilder sb = new StringBuilder();
		while (tmpl.indexOf("<model:field") > -1) {
			int beginIndex = tmpl.indexOf("<model:field");
			int endIndex = tmpl.indexOf("</model:field>", beginIndex);
			sb.append(tmpl.substring(0, beginIndex));

			String tagStr = tmpl.substring(beginIndex, endIndex + 14);
			// 处理code
			Matcher matcher = FieldCodePattern.matcher(tagStr);
			String code = matcher.find() ? matcher.group(1) : "";
			Q qbTemp = new Q();
			qbTemp.setSQL(qb.getSQL());
			qbTemp.setParams((ArrayList<Object>) qb.getParams().clone());
			if (StringUtil.isNotEmpty(code)) {
				qbTemp.append(" and Code=?", code);
			}
			DAOSet<ZDMetaColumn> cols = new ZDMetaColumn().query(qbTemp);
			qbTemp.append(" orderby orderflag asc ");

			// 替换标签内占位符
			String body = tagStr.substring(tagStr.indexOf(">") + 1, tagStr.indexOf("</model:field>"));
			for (ZDMetaColumn col : cols) {
				String newBody = body;
				Mapx<String, Object> colMap = col.toMapx();
				colMap.put("ControlHtml", MetaUtil.getControlHTML(col));
				for (String key : colMap.keySet()) {
					newBody = StringUtil.replaceEx(newBody, "@{Field." + key + "}", colMap.getString(key));
				}
				sb.append(newBody);
			}

			tmpl = tmpl.substring(endIndex + 14);
		}
		sb.append(tmpl);
		return sb.toString();
	}

	public static DataTable listToDataTable(List<IMetaModelTemplateType> list) {
		DataTable dt = new DataTable();
		dt.insertColumns(new String[] { "MMTemplateTypeID", "Name" });
		for (IMetaModelTemplateType mmtt : list) {
			dt.insertRow(new Object[] { mmtt.getID(), mmtt.getName() });
		}
		return dt;
	}

}
