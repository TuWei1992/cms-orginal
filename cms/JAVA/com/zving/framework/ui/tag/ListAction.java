package com.zving.framework.ui.tag;

import com.zving.framework.collection.Mapx;
import com.zving.framework.config.MaxPageSize;
import com.zving.framework.data.DBUtil;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.QueryBuilder;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.ui.IPageEnableAction;

/**
 * 列表数据绑定行为类
 * 
 * @Author 王育春
 * @Date 2010-11-18
 * @Mail wyuch@zving.com
 */
public class ListAction implements IPageEnableAction {
	DataTable dataSource;

	Mapx<String, Object> params;

	int pageSize;

	int pageIndex;

	boolean page;

	String ID;

	String method;

	ListTag tag;

	int total;

	String queryString;// 用于构建分页链接

	public void bindData(QueryBuilder qb) {// NO_UCD
		if (total == 0) {
			total = DBUtil.getCount(qb);
		}
		dataSource = qb.executePagedDataTable(pageSize, pageIndex);
	}

	public void bindData(DataTable dt) {
		dataSource = dt;
	}

	@Override
	public void setTotal(int total) {
		this.total = total;
	}

	@Override
	public void setTotal(QueryBuilder qb) {
		total = DBUtil.getCount(qb);
	}

	@Override
	public int getTotal() {
		return total;
	}

	/**
	 * 得到上级循环的当前行
	 */
	public DataRow getParentCurrentDataRow() {
		AbstractTag p = tag.getParent();
		if (p instanceof ListTag) {
			return ((ListTag) p).getCurrentDataRow();
		}
		return null;
	}

	public DataTable getParentData() {
		AbstractTag p = tag.getParent();
		if (p instanceof ListTag) {
			return ((ListTag) p).getData();
		}
		return null;
	}

	public String getParam(String key) {
		return params.getString(key);
	}

	public DataTable getDataSource() {
		return dataSource;
	}

	public Mapx<String, Object> getParams() {
		return params;
	}

	public void setParams(Mapx<String, Object> params) {
		this.params = params;
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

	@Override
	public int getPageIndex() {
		return pageIndex;
	}

	@Override
	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public boolean isPage() {
		return page;
	}

	public void setPage(boolean pageEnable) {
		page = pageEnable;
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public void setTag(ListTag tag) {
		this.tag = tag;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

}
