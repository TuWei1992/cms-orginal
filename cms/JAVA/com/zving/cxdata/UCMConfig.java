package com.zving.cxdata;


import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.zving.adapter.config.UCMConfigLoad;
import com.zving.framework.Config;
import com.zving.framework.collection.Mapx;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.utility.PropertiesUtil;
import com.zving.framework.utility.StringUtil;

public class UCMConfig {
	
	protected static Mapx<String, String> configMap  = new Mapx<String, String>();
	public static final String CONFIGKEY = "@ucmKey";
	static {
		loadUCMConfig();
	}
	
	public static void loadUCMConfig() {
		//Config.setPluginContext(true);
				//ExtendManager.getInstance().start();
		/*
		Properties ucmp= new Properties();
		ucmp.setProperty("ebiz.ms.web.mainBase", "http://www.dds.com");
		ucmp.setProperty("ebiz.ms.web.accountBase", "https://account.dds.com-UCMConfig");
		ucmp.setProperty("ebiz.ms.web.joinBase", "http://join.dds.com");
		ucmp.setProperty("database.ip", "10.32.140.161");
		*/
		configMap.clear();
		Properties ucmp= UCMConfigLoad.ucmProperties();
		Map<String, String> keyMap = PropertiesUtil.read( new File(Config.getPluginPath() + "classes/ucmKeys.properties"));
		
		for (Object key : ucmp.keySet()) {
			setValue((String)key, (String)ucmp.get(key));
		}
		
		for (Entry<String, String> km : keyMap.entrySet()) {
			String value = (String)ucmp.get(km.getValue());
			setValue(km.getKey(), value);
		}
	}
	
	
	 public static String getValue(String configName) {
		 return (String)configMap.get(configName);
	 }
	  
	 public static void setValue(String configName, String configValue) {
		 configMap.put(configName, configValue);
	 }
	  
	 public static Mapx<String, String> getMapx() { 
		 return configMap;
	 }
	 
	 public static void addToConfig() {
		 for (Entry<String, String> km : configMap.entrySet()) {
			// if (StringUtil.isNull(Config.getValue(km.getKey()))) {
			Config.setValue(km.getKey(), km.getValue());
			// }
		 }
	 }

	  
}
