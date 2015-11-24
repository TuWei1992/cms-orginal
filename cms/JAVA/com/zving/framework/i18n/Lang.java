package com.zving.framework.i18n;


/**
 * LangMapping.get()的缩写
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-1-30
 */
public class Lang {
	public static String get(String key, Object... args) {
		return LangMapping.get(key, args);
	}

	public static String get(String lang, String key, Object... args) {
		return LangMapping.get(lang, key, args);
	}
}
