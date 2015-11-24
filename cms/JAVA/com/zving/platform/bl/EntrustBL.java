package com.zving.platform.bl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.zving.framework.Config;
import com.zving.framework.User;
import com.zving.framework.i18n.Lang;
import com.zving.framework.json.JSONObject;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.Errorx;
import com.zving.platform.code.Enable;
import com.zving.platform.code.YesOrNo;
import com.zving.platform.config.AdminUserName;
import com.zving.schema.ZDUser;

public class EntrustBL {
	public static final String ID = "com.zving.platform.Entrust";

	public static void entrust(String fromUser, String toUser, Date start, Date end) {
		// 为委托人增加可选项
		JSONObject jo = new JSONObject();
		jo.put("User", toUser);
		jo.put("StartTime", DateUtil.toDateTimeString(start));
		jo.put("EndTime", end == null ? null : DateUtil.toDateTimeString(end));
		PreferenceBL.save(fromUser, EntrustBL.ID, "To", jo);

		// 为被委托人增加委托记录
		JSONObject toMap = (JSONObject) PreferenceBL.get(toUser, ID, "From");
		if (toMap == null) {
			toMap = new JSONObject();
		}
		jo.put("StartTime", DateUtil.toDateTimeString(start));
		jo.put("EndTime", end == null ? null : DateUtil.toDateTimeString(end));
		toMap.put(fromUser, jo);
		PreferenceBL.save(toUser, EntrustBL.ID, "From", toMap);

	}

	public static boolean login(String userName, String agentUser) {
		JSONObject jo = (JSONObject) PreferenceBL.get(userName, ID, "To");
		if (jo == null || !agentUser.equals(jo.getString("User"))) {
			return false;
		}
		String endTime = jo.getString("EndTime");
		if (endTime != null && DateUtil.compare(DateUtil.getCurrentDateTime(), endTime) >= 0) {
			return false;
		}
		userName = userName.toLowerCase();
		if (!Config.isAllowLogin() && !userName.equalsIgnoreCase(AdminUserName.getValue())) {
			Errorx.addError(Lang.get("User.DenyLoginTemp"));
			return false;
		}
		ZDUser user = new ZDUser();
		user.setUserName(userName);
		if (user.fill()) {
			if (!AdminUserName.getValue().equalsIgnoreCase(user.getUserName()) && Enable.isDisable(user.getStatus())) {
				Errorx.addError(Lang.get("User.UserStopped"));
				return false;
			}
			User.destory();
			UserBL.login(user);
			User.setValue("_ZVING_ENTRUSTING", YesOrNo.Yes);
			return true;
		}
		return false;
	}

	public static boolean isEntrust(String userName) {
		Object obj = PreferenceBL.get(userName, ID, "To");
		if (obj == null) {
			return false;
		}
		JSONObject jo = (JSONObject) obj;
		return jo.size() != 0;
	}

	public static boolean isAgent(String userName) {
		Object obj = PreferenceBL.get(userName, ID, "From");
		if (obj == null) {
			return false;
		}
		JSONObject jo = (JSONObject) obj;
		return jo.size() != 0;
	}

	public static List<String> getEntrustUsers(String userName) {
		Object obj = PreferenceBL.get(userName, ID, "From");
		if (obj == null) {
			return null;
		}
		JSONObject jo = (JSONObject) obj;
		List<String> list = new ArrayList<String>();
		for (String user : jo.keySet()) {
			JSONObject times = (JSONObject) jo.get(user);
			String endTime = times.getString("EndTime");
			if (endTime != null && DateUtil.compare(DateUtil.getCurrentDateTime(), endTime) >= 0) {
				continue;
			}
			list.add(user);
		}
		return list;
	}

	public static String getAgentuser(String userName) {
		Object obj = PreferenceBL.get(userName, ID, "To");
		if (obj == null) {
			return null;
		}
		JSONObject jo = (JSONObject) obj;
		return jo.getString("User");
	}

	public static boolean cancel(String userName) {
		Object obj = PreferenceBL.get(userName, ID, "To");
		if (obj == null) {
			return true;
		}
		JSONObject jo = (JSONObject) obj;
		PreferenceBL.remove(userName, ID, "To");

		String toUser = jo.getString("User");
		obj = PreferenceBL.get(toUser, ID, "From");
		if (obj == null) {
			return true;
		}
		jo = (JSONObject) obj;
		jo.remove(userName);
		if (jo.size() == 0) {
			PreferenceBL.remove(toUser, ID, "From");
		} else {
			PreferenceBL.save(toUser, ID, "From", jo);
		}
		return true;
	}
}
