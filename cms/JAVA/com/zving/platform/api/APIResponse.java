package com.zving.platform.api;

import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataTable;

/**
 * 用于表示一次接口调用的响应
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-4-3
 */
public class APIResponse extends Mapx<String, Object> {
	private static final long serialVersionUID = 1L;

	public int getStatus() {
		return getInt("_Status");
	}

	public void setStatus(int status) {
		put("_Status", status);
	}

	public String getMessage() {
		return getString("_Message");
	}

	public void setMessage(String message) {
		put("_Message", message);
	}

	public void setDataTable(String columns, DataTable dt) {
		APIUtil.filterColumns(columns, dt);
		put("DataTable", dt);
	}

	public void setMapx(Mapx<String, ?> map) {
		put("Mapx", map);
	}

}
