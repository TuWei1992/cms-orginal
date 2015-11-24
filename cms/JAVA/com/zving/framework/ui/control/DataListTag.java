package com.zving.framework.ui.control;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.Config;
import com.zving.framework.Constant;
import com.zving.framework.Current;
import com.zving.framework.collection.Mapx;
import com.zving.framework.core.method.IMethodLocator;
import com.zving.framework.core.method.MethodLocatorUtil;
import com.zving.framework.data.DataTypes;
import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.security.PrivCheck;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.TemplateExecutor;
import com.zving.framework.template.command.TagCommand;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.ui.UIException;
import com.zving.framework.ui.control.datalist.DataListBody;
import com.zving.framework.ui.control.datalist.DataListBodyManager;
import com.zving.framework.utility.StringUtil;

/**
 * DataList标签
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2006-9-26
 */
public class DataListTag extends AbstractTag {
	private String method;

	private String id;

	private int size;

	private boolean page = true;

	private boolean autoFill = true;

	private boolean autoPageSize = false;

	private String dragClass;

	private String listNodes;

	private String sortEnd;

	private String bodyUID;

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "datalist";
	}

	DataListAction prepareAction() {
		DataListAction dla = new DataListAction();
		dla.setPageEnabled(page);
		dla.setAutoFill(autoFill);
		dla.setAutoPageSize(autoPageSize);
		dla.setMethod(method);
		dla.setID(id);
		dla.setDragClass(dragClass);
		dla.setListNodes(listNodes);
		dla.setSortEnd(sortEnd);
		dla.setPageSize(size);

		if (page) {
			dla.setPageIndex(0);
			if (dla.getParams() != null && StringUtil.isNotEmpty(dla.getParam(Constant.DataGridPageIndex))) {
				dla.setPageIndex(Integer.parseInt(dla.getParam(Constant.DataGridPageIndex)));
			}
			if (dla.getPageIndex() < 0) {
				dla.setPageIndex(0);
			}
			dla.setPageSize(size);
		}
		dla.setTagBody(DataListBodyManager.get(dla, bodyUID, getTagSource()));
		return dla;
	}

	@Override
	public int doStartTag() throws TemplateRuntimeException {
		try {
			if (StringUtil.isEmpty(method)) {
				throw new UIException("DataList's method cann't be empty");
			}

			DataListAction dla = prepareAction();
			IMethodLocator m = MethodLocatorUtil.find(method);
			PrivCheck.check(m);

			if (Current.getRequest() != null) {
				dla.setParams(Current.getRequest());
			}
			m.execute(dla);

			pageContext.setAttribute(id + Constant.ActionInPageContext, dla);
			dla.addVariables(context);
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

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public boolean isPage() {
		return page;
	}

	public void setPage(boolean page) {
		this.page = page;
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

	public String getDragClass() {
		return dragClass;
	}

	public void setDragClass(String dragClass) {
		this.dragClass = dragClass;
	}

	public String getListNodes() {
		return listNodes;
	}

	public void setListNodes(String listNodes) {
		this.listNodes = listNodes;
	}

	public String getSortEnd() {
		return sortEnd;
	}

	public void setSortEnd(String sortEnd) {
		this.sortEnd = sortEnd;
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("id", true));
		list.add(new TagAttr("autoFill", TagAttr.BOOL_OPTIONS));
		list.add(new TagAttr("autoPageSize", TagAttr.BOOL_OPTIONS));
		list.add(new TagAttr("page", TagAttr.BOOL_OPTIONS));
		list.add(new TagAttr("size", DataTypes.INTEGER));
		list.add(new TagAttr("sortEnd"));
		list.add(new TagAttr("method"));
		list.add(new TagAttr("listNodes"));
		list.add(new TagAttr("dragClass"));
		return list;
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.UIControl.DataListTagName}";
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
		DataListAction dla = prepareAction();
		dla.setParams(new Mapx<String, Object>());
		DataListBody body = DataListBodyManager.get(dla, bodyUID, getTagSource());
		if (!tc.isHasBody()) {
			tc.setHasBody(true);
		}
		tc.setCommands(body.getExecutor().getCommands());
	}

}
