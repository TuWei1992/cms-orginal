package com.zving.framework.ui.control;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.ui.util.TagUtil;
import com.zving.framework.utility.NumberUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 滑块标签　
 * 
 * @Author 李伟仪
 * @Date 2013-7-29
 * @Mail lwy@zving.com
 */
public class SliderTag extends AbstractTag {
	private String id;

	private String name;

	private String onChange;

	private String value;

	private int defaultValue;

	private int min = 0; // default 0

	private int max = 100; // default 100

	private boolean disabled; // default false

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "slider";
	}

	@Override
	public int doEndTag() throws TemplateRuntimeException {
		try {
			if (ObjectUtil.empty(id)) {
				id = TagUtil.getTagID(pageContext, "Slider");
			}
			String html = getHtml();
			pageContext.getOut().write(html);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return EVAL_PAGE;
	}

	public String getHtml() {
		name = StringUtil.isNull(name) ? "" : name;
		int value = defaultValue;
		if (NumberUtil.isInt(this.value)) {
			value = Integer.valueOf(this.value);
		}
		String disabledStr = disabled ? "disabled" : "";

		StringBuilder sb = new StringBuilder();
		sb.append("<table>").append("<tr>").append("<td height=36>").append("<div style=\"margin-top:2px;\">")
				.append("<input id=\"")
				.append(id)
				.append("\" ")
				.append("name=\"")
				.append(name)
				.append("\"")
				// onblur方法校验值是否在min和max之间
				.append(" onchange=\"").append(onChange).append("\" ").append(disabledStr).append(" value=\"").append(value)
				.append("\" ztype=\"Number\" min=\"" + min + "\" max=\"" + max + "\" type=\"text\" style=\"width:40px;\">")
				.append("</div>").append("</td>").append("<td>&nbsp;</td>").append("<td width=310>").append("	<div id=\"").append(id)
				.append("_slider\" style=\"width:300px;\"></div>")
				.append("	<div style=\"overflow:hidden; margin-top:-3px;font-size: 10px;\">").append("		<div style=\"float:left;\">")
				.append(min).append("</div>").append("		<div style=\"float:right;\">").append(max).append("</div>").append("	</div>")
				.append("</td></tr></table>");

		sb.append("<script>");
		sb.append("Page.onReady(function(){var slider = new Slider({").append("target:'" + id + "',").append("min:" + min + ",")
				.append("max:" + max + ",").append("value:" + value).append("});").append("slider.render(getDom(\"").append(id)
				.append("_slider\"));").append("});");
		sb.append("</script>");
		return sb.toString();
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("id", true));
		list.add(new TagAttr("name"));
		list.add(new TagAttr("value"));
		list.add(new TagAttr("onChange"));
		list.add(new TagAttr("defaultValue"));
		list.add(new TagAttr("min"));
		list.add(new TagAttr("max"));
		list.add(new TagAttr("disabled"));
		return list;
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.UIControl.SliderTagName}";
	}

	@Override
	public String getPluginID() {
		return FrameworkPlugin.ID;
	}

	public String getOnChange() {
		return onChange;
	}

	public void setOnChange(String onChange) {
		this.onChange = onChange;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(int defaultValue) {
		this.defaultValue = defaultValue;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public void setName(String name) {
		this.name = name;
	}
}
