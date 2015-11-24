package com.zving.cxdata.service;

import java.util.List;

import com.zving.cxdata.ICXDataCondition;
import com.zving.framework.data.DataTable;
import com.zving.framework.extend.AbstractExtendService;
/**
 * 
 * @author v_zhouquan
 * 数据获取条件扩展服务
 */
public class CXDataConditionService extends
		AbstractExtendService<ICXDataCondition> {
	public static CXDataConditionService getInstance() {
	    return (CXDataConditionService)findInstance(CXDataConditionService.class);
	  }
	public static DataTable getAllCXDataConditions() {
		 DataTable dt = new DataTable();
		 dt.insertColumn("ID");
		 dt.insertColumn("Name");
		 List<ICXDataCondition> all = getInstance().getAll();
		 if(all != null && all.size() > 0) {
			 for (ICXDataCondition condition: all) {
				 dt.insertRow(new Object[]{condition.getExtendItemID(), condition.getExtendItemName()});
			 }
		 }
		 return dt;
	 }
}
