package com.zving.platform.util;

import java.util.Map;
import java.util.Map.Entry;

import com.zving.framework.Config;
import com.zving.framework.cache.CacheDataProvider;
import com.zving.framework.cache.CacheManager;
import com.zving.framework.collection.CacheMapx;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.Q;
import com.zving.framework.orm.DAOSet;
import com.zving.schema.ZDCode;

/**
 * 代码缓存
 * 
 * @Author 王育春
 * @Date 2008-10-30
 * @Mail wyuch@zving.com
 */
public class CodeCache extends CacheDataProvider {
	public static final String ProviderID = "Code";

	@Override
	public String getExtendItemID() {
		return ProviderID;
	}

	@Override
	public void onKeyNotFound(String type, String key) {
		if (!Config.isInstalled()) {
			return;
		}
		ZDCode code = new ZDCode();
		code.setCodeType(type);
		code.setCodeValue(key);
		DAOSet<ZDCode> set = code.query();
		if (set.size() == 0) {
			return;
		}
		CacheManager.set(ProviderID, type, key, set.get(0));
	}

	@Override
	public void onTypeNotFound(String type) {
		if (!Config.isInstalled()) {
			return;
		}
		CacheManager.setMapx(ProviderID, type, new CacheMapx<String, Object>());
		DAOSet<ZDCode> set = new ZDCode().query(new Q("where CodeType=? order by CodeOrder", type));
		for (ZDCode code2 : set) {
			String parentcode = code2.getParentCode();
			if (parentcode.equals("System")) {
				continue;
			}
			CacheManager.set(ProviderID, code2.getCodeType(), code2.getCodeValue(), code2);
		}
	}

	public static void setCode(ZDCode code) {
		if (code == null || "System".equals(code.getParentCode())) {
			return;
		}
		CacheManager.set(ProviderID, code.getCodeType(), code.getCodeValue(), code);
	}

	public static void removeCode(ZDCode code) {
		if (code == null) {
			return;
		}
		if ("System".equals(code.getParentCode())) {
			CacheManager.setMapx(ProviderID, code.getCodeType(), null);
		} else {
			CacheManager.remove(ProviderID, code.getCodeType(), code.getCodeValue());
		}
	}

	@Override
	public String getExtendItemName() {
		return "Code Cache";
	}

	/**
	 * 获取指定代码类别下的指定代码项
	 */
	public static ZDCode get(String type, String codeValue) {
		return (ZDCode) CacheManager.get(ProviderID, type, codeValue);
	}

	/**
	 * 获得指定代码类型的全部代码项
	 */
	public static Mapx<String, ZDCode> getMapx(String type) {
		CodeCache cc = (CodeCache) CacheManager.getCache(ProviderID);
		Map<String, Object> map = cc.TypeMap.get(type);
		if (map == null) {
			cc.onTypeNotFound(type);
			map = cc.TypeMap.get(type);
		}
		Mapx<String, ZDCode> r = new Mapx<String, ZDCode>();
		for (Entry<String, Object> entry : map.entrySet()) {
			r.put(entry.getKey(), (ZDCode) entry.getValue());
		}
		return r;
	}
}
