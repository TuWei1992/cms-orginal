package com.zving.platform.config;

import java.util.List;

import com.zving.framework.ConfigLoader;
import com.zving.framework.collection.Mapx;
import com.zving.framework.xml.XMLElement;

public class APIMethodCache {

	private static boolean enabled;
	private static int expires;

	static {
		List<XMLElement> datas = ConfigLoader.getElements("clustering.apiCache.config");
		Mapx<String, String> configMap = new Mapx<String, String>();
		for (XMLElement data : datas) {
			configMap.put("apiCache." + data.getAttributes().get("name"), data.getText());
		}
		enabled = configMap.getBoolean("apiCache.Enabled");
		expires = configMap.getInt("apiCache.Expires");
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static int getExpires() {
		return expires;
	}
}
