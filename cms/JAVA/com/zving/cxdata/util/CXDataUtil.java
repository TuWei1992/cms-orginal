package com.zving.cxdata.util;

import java.util.List;


import com.zving.framework.data.DataTable;
import com.zving.framework.utility.StringUtil;

public class CXDataUtil {
	public static DataTable listToOneColumnDT(List list, String column){
		DataTable dt = new DataTable();
		if(StringUtil.isEmpty(column)) {
			column = "data";
		}
		if (list != null && list.size() > 0) {
			for (Object data : list) {
				dt.insertRow(new Object[]{data});
			}
		}
		return dt;
	}
	
	public static DataTable listToOneColumnDT(List list) {
		return listToOneColumnDT(list, "data");
	}
}
