package com.zving.framework.json;

import java.util.ArrayList;

import com.zving.framework.core.castor.LongCastor;

/**
 * JSON数组,继承ArrayList<Object>
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-9-12
 */
public class JSONArray extends ArrayList<Object> implements JSONAware {
	private static final long serialVersionUID = 3957988303675231981L;

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}

	@Override
	public String toJSONString() {
		return JSON.toJSONString(this);
	}

	public JSONObject getJSONObject(int i) {
		return (JSONObject) get(i);
	}

	public long getLong(int i) {
		return (Long) LongCastor.getInstance().cast(get(i), null);
	}

	public String getString(int i) {
		Object obj = get(i);
		return obj == null ? null : obj.toString();
	}

	public int length() {
		return size();
	}

	public JSONArray getJSONArray(int i) {
		return (JSONArray) get(i);
	}

}
