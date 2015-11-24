package com.zving.platform.service;

import java.util.List;

import com.zving.framework.collection.Mapx;
import com.zving.framework.extend.AbstractExtendService;
import com.zving.framework.json.JSON;
import com.zving.platform.AbstractUserPreferences;
import com.zving.schema.ZDUserPreferences;

public class UserPreferencesService extends AbstractExtendService<AbstractUserPreferences> {

	public static UserPreferencesService getInstance() {
		return findInstance(UserPreferencesService.class);
	}

	/**
	 * 校验用户可选项属性值是否正确
	 */
	public static boolean validate(Mapx<String, Object> map) {
		for (String key : map.keySet()) {
			AbstractUserPreferences up = getInstance().get(key);
			if (up == null) {
				continue;
			}
			if (!up.validate(map.getString(key))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 处理用户可选项
	 * 
	 * @param request
	 * @return
	 */
	public static Mapx<String, String> process(Mapx<String, Object> request) {
		Mapx<String, String> map = new Mapx<String, String>();
		List<AbstractUserPreferences> list = getInstance().getAll();
		for (AbstractUserPreferences up : list) {
			map.put(up.getExtendItemID(), up.process(request));
		}
		return map;
	}

	public static Mapx<String, Object> getUerPreferences(String username) {
		Mapx<String, Object> map = new Mapx<String, Object>();
		ZDUserPreferences dao = new ZDUserPreferences();
		dao.setUserName(username);
		if (dao.fill()) {
			map.putAll(JSON.parseJSONObject(dao.getConfigProps()));
		}
		List<AbstractUserPreferences> list = getInstance().getAll();
		for (AbstractUserPreferences up : list) {
			if (!map.containsKey(up.getExtendItemID())) {
				map.put(up.getExtendItemID(), up.defaultValue());
			}
		}
		return map;
	}
}
