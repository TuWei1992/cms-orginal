package com.zving.framework.ui.tag;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.zving.framework.core.bean.BeanUtil;
import com.zving.framework.data.DataTypes;
import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.json.JSON;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.utility.NumberUtil;

/**
 * 变量定义/置值标签，用于在模板中定义和修改变量。<br>
 * 如果本标签没有父标签，则定义的变量在整个模板范围内有效。<br>
 * 如果本标签外面有父标签，则定义的变量只在父标签之内、本标签之后的范围内有效。
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2012-2-5
 */
public class SetTag extends AbstractTag {
	String var;
	Object value;
	String json;

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "set";
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public int doStartTag() throws TemplateRuntimeException {
		try {
			Object v = null;
			if (value != null) {
				v = value;
			} else if (json != null) {
				v = JSON.parse(json);
			}
			if (var.endsWith("]")) {
				String name = var.substring(0, var.lastIndexOf('[')).trim();
				Object obj = context.evalExpression("${" + name + "}");
				if (obj != null) {
					String ref = var.substring(var.lastIndexOf('[') + 1, var.length() - 1).trim();
					if (NumberUtil.isInt(ref)) {// 下标访问
						int i = Integer.parseInt(ref);
						if (obj.getClass().isArray() && Array.getLength(obj) > i) {
							Array.set(obj, Integer.parseInt(ref), v);
						} else if (obj instanceof List) {
							List list = (List) obj;
							while (list.size() <= i) {
								list.add(null);
							}
							list.set(i, v);
						}
					} else {// 字段名访问
						if (obj instanceof Map) {
							((Map) obj).put(ref, v);
						} else {
							BeanUtil.set(obj, ref, v);
						}
					}
				}
			} else if (var.indexOf(".") > 0) {
				Object obj = context.evalExpression("${" + var.substring(0, var.lastIndexOf('.')) + "}");
				if (obj != null) {
					String ref = var.substring(var.lastIndexOf(".") + 1);
					if (obj instanceof Map) {
						((Map) obj).put(ref, v);
					} else {
						BeanUtil.set(obj, ref, v);
					}
				}
			} else {// 新定义的变量
				pageContext.addRootVariable(var, v);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return SKIP_BODY;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("var", DataTypes.STRING, "@{Framework.VarTag.Name}"));
		list.add(new TagAttr("value", DataTypes.STRING, "@{Framework.VarTag.Value}"));
		list.add(new TagAttr("json", DataTypes.STRING, "@{Framework.VarTag.JsonValue}"));
		return list;
	}

	@Override
	public String getPluginID() {
		return FrameworkPlugin.ID;
	}

	@Override
	public String getDescription() {
		return "@{Framework.ZVarTagDescription}";
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.ZVarTagName}";
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}

}
