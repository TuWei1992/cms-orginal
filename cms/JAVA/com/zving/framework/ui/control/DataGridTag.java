package com.zving.framework.ui.control;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.Config;
import com.zving.framework.Constant;
import com.zving.framework.Current;
import com.zving.framework.collection.Mapx;
import com.zving.framework.core.method.IMethodLocator;
import com.zving.framework.core.method.MethodLocatorUtil;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.DataTypes;
import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.security.PrivCheck;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.TemplateExecutor;
import com.zving.framework.template.command.TagCommand;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.ui.UIException;
import com.zving.framework.ui.control.grid.DataGridBody;
import com.zving.framework.ui.control.grid.DataGridBodyManager;
import com.zving.framework.utility.StringUtil;

/**
 * DataGrid标签
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2005-9-26
 */
public class DataGridTag extends AbstractTag {
	private String method;

	private String id;

	private boolean page = true;

	private int size;

	private boolean multiSelect = true;

	private boolean autoFill = true;

	private boolean autoPageSize = false;

	private boolean scroll = true;

	private boolean lazy = false;// 默认是否加载数据

	private int cacheSize;

	private String bodyUID;

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "datagrid";
	}

	DataGridAction prepareAction() {
		DataGridAction dga = new DataGridAction();
		dga.setMethod(method);
		dga.setID(id);
		dga.setAjaxRequest(false);
		dga.setPageEnabled(page);
		dga.setMultiSelect(multiSelect);
		dga.setAutoFill(autoFill);
		dga.setAutoPageSize(autoPageSize);
		dga.setScroll(scroll);
		dga.setCacheSize(cacheSize);
		dga.setLazy(lazy);

		if (page) {
			dga.setPageIndex(0);
			if (dga.getParams() != null && StringUtil.isNotEmpty(dga.getParam(Constant.DataGridPageIndex))) {
				dga.setPageIndex(Integer.parseInt(dga.getParam(Constant.DataGridPageIndex)));
			}
			if (dga.getPageIndex() < 0) {
				dga.setPageIndex(0);
			}
			if (autoPageSize == true) {
				size = 30;
			}
			dga.setPageSize(size);
		}

		dga.setTagBody(DataGridBodyManager.get(dga, bodyUID, getTagSource()));
		return dga;
	}

	@Override
	public int doStartTag() throws TemplateRuntimeException {
		try {
			if (StringUtil.isEmpty(method)) {
				throw new UIException("DataGrid's method is not set!");
			}
			DataGridAction dga = prepareAction();

			if (lazy) {
				dga.bindData(new DataTable());// 默认不加载
			} else {
				IMethodLocator m = MethodLocatorUtil.find(method);
				PrivCheck.check(m);

				dga.setParams(Current.getRequest());

				m.execute(dga);
			}
			pageContext.setAttribute(id + Constant.ActionInPageContext, dga);
			dga.addVariables(context);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return EVAL_BODY_INCLUDE;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isPage() {
		return page;
	}

	public void setPage(boolean page) {
		this.page = page;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public boolean isMultiSelect() {
		return multiSelect;
	}

	public void setMultiSelect(boolean multiSelect) {
		this.multiSelect = multiSelect;
	}

	public boolean isAutoFill() {
		return autoFill;
	}

	public void setAutoFill(boolean autoFill) {
		this.autoFill = autoFill;
	}

	public boolean isAutoPageSize() {
		return autoPageSize;
	}

	public void setAutoPageSize(boolean autoPageSize) {
		this.autoPageSize = autoPageSize;
	}

	public boolean isScroll() {
		return scroll;
	}

	public void setScroll(boolean scroll) {
		this.scroll = scroll;
	}

	public int getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}

	public boolean isLazy() {
		return lazy;
	}

	public void setLazy(boolean lazy) {
		this.lazy = lazy;
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("id", true));
		list.add(new TagAttr("autoFill", TagAttr.BOOL_OPTIONS));
		list.add(new TagAttr("autoPageSize", TagAttr.BOOL_OPTIONS));
		list.add(new TagAttr("cacheSize", DataTypes.INTEGER));
		list.add(new TagAttr("lazy", TagAttr.BOOL_OPTIONS));
		list.add(new TagAttr("method"));
		list.add(new TagAttr("multiSelect", TagAttr.BOOL_OPTIONS));
		list.add(new TagAttr("page", TagAttr.BOOL_OPTIONS));
		list.add(new TagAttr("scroll", TagAttr.BOOL_OPTIONS));
		list.add(new TagAttr("size", DataTypes.INTEGER));
		return list;
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.UIControl.DataGridTagName}";
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
		DataGridAction dga = prepareAction();
		dga.setParams(new Mapx<String, Object>());
		DataGridBody body = DataGridBodyManager.get(dga, bodyUID, getTagSource());
		if (!tc.isHasBody()) {
			tc.setHasBody(true);
		}
		tc.setCommands(body.getExecutor().getCommands());
	}
}
