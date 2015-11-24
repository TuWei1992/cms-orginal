package com.zving.framework.ui.control;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.Config;
import com.zving.framework.Current;
import com.zving.framework.collection.Mapx;
import com.zving.framework.core.method.IMethodLocator;
import com.zving.framework.core.method.MethodLocatorUtil;
import com.zving.framework.data.DataTypes;
import com.zving.framework.expression.ExpressionException;
import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.security.PrivCheck;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.TemplateExecutor;
import com.zving.framework.template.command.TagCommand;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.ui.UIException;
import com.zving.framework.ui.control.tree.TreeBody;
import com.zving.framework.ui.control.tree.TreeBodyManager;
import com.zving.framework.utility.StringUtil;

/**
 * 树标签　
 * 
 * @Author 王育春
 * @Date 2008-1-23
 * @Mail wyuch@zving.com
 */
public class TreeTag extends AbstractTag {
	private String id;

	private String method;

	private String style;

	private boolean lazy;

	private boolean customscrollbar;

	private String checkbox;// 可能的值 all/branch/leaf

	private boolean cascade = true;// 是否级联

	private String radio;

	private boolean expand; // 延迟加载时全部展开

	private int level;

	private String bodyUID;

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "tree";
	}

	@Override
	public void init() throws ExpressionException {
		customscrollbar = true;
		super.init();
	}

	private TreeAction prepareAction() {
		TreeAction ta = new TreeAction();
		ta.setMethod(method);

		ta.setID(id);
		ta.setLazy(lazy);
		ta.setCustomscrollbar(customscrollbar);
		ta.setCheckbox(checkbox);
		ta.setCascade(cascade);
		ta.setRadio(radio);
		ta.setExpand(expand);
		if (level <= 0) {
			level = 999;
		}
		ta.setLevel(level);
		ta.setStyle(style);

		String content = getTagSource();
		ta.setTagBody(TreeBodyManager.get(ta, bodyUID, content));
		return ta;
	}

	@Override
	public int doStartTag() throws TemplateRuntimeException {
		try {
			if (method == null || method.equals("")) {
				throw new UIException("Tree's method can't be empty");
			}

			IMethodLocator m = MethodLocatorUtil.find(method);
			PrivCheck.check(m);

			TreeAction ta = prepareAction();
			ta.setParams(Current.getRequest());
			m.execute(ta);
			ta.bindData();

			ta.addVariables(context);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return EVAL_BODY_INCLUDE;
	}

	public boolean isLazy() {
		return lazy;
	}

	public void setLazy(boolean lazy) {
		this.lazy = lazy;
	}

	public boolean isCustomscrollbar() {
		return customscrollbar;
	}

	public void setCustomscrollbar(boolean customscrollbar) {
		this.customscrollbar = customscrollbar;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public boolean isExpand() {
		return expand;
	}

	public void setExpand(boolean expand) {
		this.expand = expand;
	}

	public boolean isCascade() {
		return cascade;
	}

	public void setCascade(boolean cascade) {
		this.cascade = cascade;
	}

	public String getCheckbox() {
		return checkbox;
	}

	public void setCheckbox(String checkbox) {
		this.checkbox = checkbox;
	}

	public String getRadio() {
		return radio;
	}

	public void setRadio(String radio) {
		this.radio = radio;
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("id", true));
		list.add(new TagAttr("customscrollbar", TagAttr.BOOL_OPTIONS));
		list.add(new TagAttr("checkbox"));
		list.add(new TagAttr("cascade", TagAttr.BOOL_OPTIONS));
		list.add(new TagAttr("radio"));
		list.add(new TagAttr("expand", TagAttr.BOOL_OPTIONS));
		list.add(new TagAttr("lazy", TagAttr.BOOL_OPTIONS));
		list.add(new TagAttr("level", DataTypes.INTEGER));
		list.add(new TagAttr("size", DataTypes.INTEGER));
		list.add(new TagAttr("style"));
		list.add(new TagAttr("method"));
		return list;
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.UIControl.TreeTagName}";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public String getPluginID() {
		return FrameworkPlugin.ID;
	}

	@Override
	public boolean isKeepTagSource() {
		return true;// 本标签要求编译后依然持有源代码
	}

	@Override
	public void afterCompile(TagCommand tc, TemplateExecutor te) {
		String fileName = te.getFileName();
		if (fileName != null && fileName.startsWith(Config.getContextRealPath())) {
			fileName = fileName.substring(Config.getContextRealPath().length());
		}
		bodyUID = fileName + "#" + StringUtil.md5Hex(getTagSource());
		TreeAction ta = prepareAction();
		ta.setParams(new Mapx<String, Object>());

		TreeBody body = TreeBodyManager.get(ta, bodyUID, getTagSource());
		if (!tc.isHasBody()) {
			tc.setHasBody(true);
		}
		tc.setCommands(body.getExecutor().getCommands());
	}

}
