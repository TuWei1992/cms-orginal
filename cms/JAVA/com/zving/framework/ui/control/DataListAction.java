package com.zving.framework.ui.control;

import com.zving.framework.collection.Mapx;
import com.zving.framework.config.MaxPageSize;
import com.zving.framework.data.DBUtil;
import com.zving.framework.data.DataColumn;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.DataTypes;
import com.zving.framework.data.QueryBuilder;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.ui.IPageEnableAction;
import com.zving.framework.ui.control.datalist.DataListBody;
import com.zving.framework.ui.tag.ListTag;
import com.zving.framework.ui.zhtml.ZhtmlExecuteContext;
import com.zving.framework.ui.zhtml.ZhtmlManagerContext;

/**
 * DataList绑定行为类
 * 
 * @Author 王育春
 * @Date 2008-1-23
 * @Mail wyuch@zving.com
 */
public class DataListAction implements IPageEnableAction {

	DataTable dataSource;

	String ID;

	DataListBody tagBody;

	boolean pageEnabled;

	Mapx<String, Object> params = new Mapx<String, Object>();

	String method;

	int total;

	int pageIndex;

	int pageSize;

	boolean autoFill;

	boolean autoPageSize;

	String dragClass;

	String listNodes;

	String sortEnd;

	boolean totalFlag = false;// 是否需要重新计算记录总数

	boolean isAjaxRequest;

	String result = "";

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Mapx<String, Object> getParams() {
		return params;
	}

	public void setParams(Mapx<String, Object> params) {
		this.params = params;
	}

	public String getParam(String key) {
		return params.getString(key);
	}

	public void bindData(DataTable dt) {
		dataSource = dt;
		try {
			this.bindData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void addVariables(AbstractExecuteContext context) {
		context.addDataVariable(ListTag.ZListDataNameKey, dataSource);
		context.addDataVariable(ListTag.ZListItemNameKey, "DataRow");
		context.addDataVariable("_DataListAction", this);
		LangUtil.decodeDataTable(dataSource, context.getLanguage()); // 检查国际化字符串
	}

	private void bindData() throws Exception {
		if (dataSource.getDataColumn("_RowNo") == null) {
			dataSource.insertColumn(new DataColumn("_RowNo", DataTypes.INTEGER));
		}
		for (int j = 0; j < dataSource.getRowCount(); j++) {
			int rowNo = pageIndex * pageSize + j + 1;
			dataSource.set(j, "_RowNo", new Integer(rowNo));
		}
		if (isAjaxRequest) {
			ZhtmlExecuteContext context = new ZhtmlExecuteContext(ZhtmlManagerContext.getInstance(), null, null);
			addVariables(context);
			tagBody.getExecutor().execute(context);
			result = context.getOut().getResult();
		}
	}

	public void bindData(QueryBuilder qb) {// NO_UCD
		bindData(qb, pageEnabled);
	}

	public void bindData(QueryBuilder qb, boolean pageFlag) {
		if (pageFlag) {
			if (!totalFlag) {// 需要重新计算总数
				setTotal(DBUtil.getCount(qb));
			}
			bindData(qb.executePagedDataTable(pageSize, pageIndex));
		} else {
			bindData(qb.executeDataTable());
		}
	}

	public String getResult() {
		return result;
	}

	public DataTable getDataSource() {
		return dataSource;
	}

	public String getID() {
		return ID;
	}

	public void setID(String id) {
		ID = id;
	}

	public boolean isPageEnabled() {
		return pageEnabled;
	}

	public void setPageEnabled(boolean page) {
		pageEnabled = page;
	}

	@Override
	public int getTotal() {
		return total;
	}

	@Override
	public void setTotal(int total) {
		if (total < 0) {
			return;
		}
		this.total = total;
		if (pageIndex > Math.ceil(total * 1.0 / pageSize)) {
			pageIndex = new Double(Math.floor(total * 1.0 / pageSize)).intValue();
		}
		totalFlag = true;
	}

	@Override
	public void setTotal(QueryBuilder qb) {
		if (pageIndex == 0 || !totalFlag) {
			setTotal(DBUtil.getCount(qb));
		}
	}

	@Override
	public int getPageIndex() {
		return pageIndex;
	}

	@Override
	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
		/* ${_ZVING_LICENSE_CODE_} */
	}

	@Override
	public int getPageSize() {
		return pageSize;
	}

	@Override
	public void setPageSize(int pageSize) {
		if (pageSize > MaxPageSize.getValue()) {
			pageSize = MaxPageSize.getValue();
		}
		this.pageSize = pageSize;
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

	public DataListBody getTagBody() {
		return tagBody;
	}

	public void setTagBody(DataListBody tagBody) {
		this.tagBody = tagBody;
	}

	public boolean isAjaxRequest() {
		return isAjaxRequest;
	}

	public void setAjaxRequest(boolean isAjaxRequest) {
		this.isAjaxRequest = isAjaxRequest;
	}
}
