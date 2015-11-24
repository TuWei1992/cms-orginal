package com.zving.framework.ui.tag;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.Constant;
import com.zving.framework.Current;
import com.zving.framework.RequestData;
import com.zving.framework.core.method.IMethodLocator;
import com.zving.framework.core.method.MethodLocatorUtil;
import com.zving.framework.data.DataColumn;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.DataTypes;
import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.i18n.Lang;
import com.zving.framework.security.PrivCheck;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.TemplateCompiler;
import com.zving.framework.template.TemplateExecutor;
import com.zving.framework.template.command.TagCommand;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.ui.UIException;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 列表循环标签，用于循环输出DataTable数据，有两种用法：<br>
 * 1、指定item和data属性，其中item的值作为变量名的前缀，data属性为一个DataTable变量的表达式。
 * 2、作为其他标签的内部循环器，这就要求外部循环器在上下文中设置两个变量：ZListItemNameKey（用于指定变量名前缀）和ZListDataNameKey（用于指定DataTable变量）
 * 注意：在循环体内可以获取到${i}用于表明循环了几次，${first}用来表明是不是第一次循环，${last}用来表明是否是最后一次循环
 * 
 * @Author 王育春
 * @Date 2010-11-18
 * @Mail wyuch@zving.com
 */
public class ListTag extends AbstractTag implements IListTag {
	public static final String ZListDataNameKey = "_Zving_ZList_Data";
	public static final String ZListItemNameKey = "_Zving_ZList_Item";
	String method;
	boolean page;
	int size;
	int rowIndex;
	String id;
	Object data;
	String item;
	int count;
	int begin;
	DataRow currentRow; // 当前数据行
	DataTable dataTable;

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "list";
	}

	@Override
	public boolean isKeepTagSource() {
		return true;
	}

	@Override
	public void afterCompile(TagCommand tc, TemplateExecutor te) {
		String content = getTagBodySource();
		content = StringUtil.rightTrim(content);
		TemplateCompiler c = new TemplateCompiler(te.getManagerContext());
		c.compileSource(content);
		if (!tc.isHasBody()) {
			tc.setHasBody(true);
		}
		tc.setCommands(c.getExecutor().getCommands());
	}

	@Override
	public int doStartTag() throws TemplateRuntimeException {
		try {
			if (ObjectUtil.notEmpty(method)) {
				IMethodLocator m = MethodLocatorUtil.find(method);
				PrivCheck.check(m);
				RequestData request = Current.getRequest();
				ListAction la = new ListAction();
				la.setParams(request);
				la.setPage(page);
				la.setMethod(method);
				la.setID(id);
				la.setPageSize(size);
				la.setTag(this);
				if (request != null) {
					la.setQueryString(request.getQueryString());
				}
				if (page) {
					la.setPageIndex(0);
					if (StringUtil.isNotEmpty(la.getParam("PageIndex"))) {
						la.setPageIndex(Integer.parseInt(la.getParam("PageIndex")) - 1);
					}
					if (la.getPageIndex() < 0) {
						la.setPageIndex(0);
					}
				}
				m.execute(la);
				dataTable = la.getDataSource();
				pageContext.setAttribute(id + Constant.ActionInPageContext, la);// 供PageBar标签使用
			} else {
				if (StringUtil.isEmpty(item)) {
					item = pageContext.eval(ZListItemNameKey);
				}
				if (data != null) {
					if (data instanceof DataTable) {
						dataTable = (DataTable) data;
					} else {
						throw new UIException(Lang.get("Framework.ZListDataAttributeMustBeDataTable"));
					}
				} else {
					dataTable = (DataTable) pageContext.evalExpression("${" + ZListDataNameKey + "}");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		begin = begin <= 0 ? 1 : begin;
		count = count <= 0 ? Integer.MAX_VALUE : count;
		if (begin > 0) {
			rowIndex = begin - 1;
		}
		if (dataTable != null && dataTable.getRowCount() > 0 && rowIndex < dataTable.getRowCount()) {
			if (dataTable.getDataColumn("_RowNo") == null) {
				dataTable.insertColumn(new DataColumn("_RowNo", DataTypes.INTEGER));
				for (int i = 0; i < dataTable.getRowCount(); i++) {
					dataTable.set(i, "_RowNo", i + 1);
				}
			}
			currentRow = dataTable.getDataRow(rowIndex++);
			if (ObjectUtil.notEmpty(item)) {
				context.addDataVariable(item, currentRow);
			}
			context.addDataVariable("i", rowIndex - begin + 1);
			context.addDataVariable("first", true);
			if (rowIndex == dataTable.getRowCount() - 1) {
				context.addDataVariable("last", true);// 存在只有一条记录的情况
			} else {
				context.addDataVariable("last", false);
			}
			return EVAL_BODY_INCLUDE;
		} else {
			return SKIP_BODY;
		}
	}

	@Override
	public int doAfterBody() throws TemplateRuntimeException {
		if (dataTable.getRowCount() > rowIndex && rowIndex - begin + 1 < count) {
			currentRow = dataTable.getDataRow(rowIndex++);
			if (ObjectUtil.notEmpty(item)) {
				context.addDataVariable(item, currentRow);
			}
			context.addDataVariable("i", rowIndex - begin + 1);
			context.addDataVariable("first", false);// 后续都置为false
			if (rowIndex == dataTable.getRowCount()) {
				context.addDataVariable("last", true);
			}
			return EVAL_BODY_AGAIN;
		} else {
			return SKIP_BODY;
		}
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
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

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public DataRow getCurrentDataRow() {
		return currentRow;
	}

	public DataTable getData() {
		return dataTable;
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("id", true));
		list.add(new TagAttr("method", DataTypes.STRING, "@{Framework.ListTag.Method}"));
		list.add(new TagAttr("data", DataTypes.STRING, "@{Framework.ListTag.Data}"));
		list.add(new TagAttr("item", DataTypes.STRING, "@{Framework.ListTag.Item}"));
		list.add(new TagAttr("size", DataTypes.INTEGER, "@{Framework.ListTag.Size}"));
		list.add(new TagAttr("page", TagAttr.BOOL_OPTIONS, "@{Framework.ListTag.Page}"));
		list.add(new TagAttr("count", DataTypes.INTEGER, "@{Framework.ListTag.Count}"));
		list.add(new TagAttr("begin", DataTypes.INTEGER, "@{Framework.ListTag.Begin}"));
		return list;
	}

	@Override
	public String getDescription() {
		return "@{Framework.ZListTagDescription}";
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.ZListTagName}";
	}

	@Override
	public String getPluginID() {
		return FrameworkPlugin.ID;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setBegin(int begin) {
		this.begin = begin;
	}
}
