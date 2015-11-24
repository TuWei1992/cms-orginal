package com.zving.platform;

import com.zving.framework.collection.Mapx;
import com.zving.framework.data.Transaction;
import com.zving.framework.extend.IExtendItem;

public interface IRecyclableItem extends IExtendItem {
	/**
	 * 执行删除动作
	 */
	public void executeDeleteAction(String[] ids, Transaction trans);

	/**
	 * 执行恢复动作
	 */
	public void executeRecoveryAction(String[] ids, Transaction trans);

	/**
	 * 回收站右侧列表 返回类型为 DataTable,int 对象 dt表示数据 count表示数据总数
	 */
	public RecycbinListData getRecycleBinList(Mapx<String, Object> params, int PageSize, int PageIndex, String SortString);
}
