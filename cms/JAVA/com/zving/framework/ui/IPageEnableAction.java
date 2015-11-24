package com.zving.framework.ui;

import com.zving.framework.data.QueryBuilder;

/**
 * 可分页的Action
 * 
 * @author 王育春
 * @date 2009-10-8
 * @email wangyc@zving.com
 */
public interface IPageEnableAction {
	public int getPageSize();

	public int getPageIndex();

	public int getTotal();

	public void setPageSize(int pageSize);

	public void setPageIndex(int pageIndex);

	public void setTotal(QueryBuilder qb);

	public void setTotal(int total);
}
