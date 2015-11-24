package com.zving.platform.bl;

import java.util.Date;

import com.zving.framework.User;
import com.zving.framework.json.JSON;
import com.zving.framework.json.JSONObject;
import com.zving.schema.ZDUserPreferences;

/**
 * 可选项的通用业务逻辑
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-1-30
 */
public class PreferenceBL {
	public static final String KEY = "_ZVING_USERPREFERCES";

	public static JSONObject getAll(String userName) {
		ZDUserPreferences up = new ZDUserPreferences();
		up.setUserName(userName);
		if (User.getUserName() != null && User.getUserName().equals(userName)) {
			Object obj = User.getValue(KEY);
			if (obj != null) {
				return (JSONObject) obj;
			} else {
				up.fill();
				JSONObject jo = (JSONObject) JSON.parse(up.getConfigProps());
				User.setValue(KEY, jo);
				return jo;
			}
		} else {
			up.fill();
			JSONObject jo = (JSONObject) JSON.parse(up.getConfigProps());
			return jo;
		}
	}

	public static JSONObject get(String userName, String typeID) {
		JSONObject ps = getAll(userName);
		if (ps == null) {
			return null;
		}
		return (JSONObject) ps.get(typeID);
	}

	public static Object remove(String userName, String typeID, String keyID) {
		JSONObject obj = get(userName, typeID);
		if (obj == null) {
			return null;
		}
		Object r = obj.remove(keyID);
		save(userName, typeID, obj);
		return r;
	}

	public static Object get(String userName, String typeID, String keyID) {
		JSONObject obj = get(userName, typeID);
		if (obj == null) {
			return null;
		}
		return obj.get(keyID);
	}

	public static boolean save(String userName, String typeID, String keyID, Object value) {
		JSONObject all = getAll(userName);
		if (all == null) {
			all = new JSONObject();
		}
		JSONObject jo = (JSONObject) all.get(typeID);
		if (jo == null) {
			jo = new JSONObject();
			all.put(typeID, jo);
		}
		jo.put(keyID, value);
		ZDUserPreferences up = new ZDUserPreferences();
		up.setUserName(userName);
		if (up.fill()) {
			up.setConfigProps(JSON.toJSONString(all));
			up.setModifyUser(userName);
			up.setModifyTime(new Date());
			return up.update();
		} else {
			up.setConfigProps(JSON.toJSONString(all));
			up.setAddUser(userName);
			up.setAddTime(new Date());
			return up.insert();
		}
	}

	public static boolean save(String userName, String typeID, JSONObject jo) {
		JSONObject all = getAll(userName);
		if (all == null) {
			all = new JSONObject();
		}
		all.put(typeID, jo);
		ZDUserPreferences up = new ZDUserPreferences();
		up.setUserName(userName);
		if (up.fill()) {
			up.setConfigProps(JSON.toJSONString(all));
			return up.update();
		} else {
			up.setConfigProps(JSON.toJSONString(all));
			return up.insert();
		}
	}
}
