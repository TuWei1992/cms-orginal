package com.zving.framework.i18n;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.zving.framework.Config;
import com.zving.framework.Constant;
import com.zving.framework.Current;
import com.zving.framework.RequestData;
import com.zving.framework.User;
import com.zving.framework.collection.Mapx;
import com.zving.framework.core.bean.BeanUtil;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.orm.DAO;
import com.zving.framework.orm.DAOColumn;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.Primitives;
import com.zving.framework.utility.StringUtil;

/**
 * 国际化工具类
 * 
 * @Author 王育春
 * @Date 2011-4-13
 * @Mail wyuch@zving.com
 */
public class LangUtil {
	public static final String LangFieldPrefix = "@Lang\n";

	/**
	 * 获得浏览器的默认语言
	 */
	public static String getLanguage(HttpServletRequest request) {
		// 获得默认语言
		String path = Config.getContextPath();
		if (path != null) {
			if (path.startsWith("/")) {
				path = path.substring(1);
			}
			if (path.endsWith("/")) {
				path = path.substring(0, path.length() - 1);
			}
		}
		if (request.getCookies() != null) {
			for (Cookie c : request.getCookies()) {
				if (!c.getName().equals(Constant.LanguageCookieName)) {
					continue;
				}
				String path2 = c.getPath();
				if (path2 != null) {
					if (path2.startsWith("/")) {
						path2 = path2.substring(1);
					}
					if (path2.endsWith("/")) {
						path2 = path2.substring(0, path2.length() - 1);
					}
					if (path2.equals(path)) {
						return c.getValue();
					}
				} else {
					if (ObjectUtil.empty(path2)) {
						return c.getValue();
					}
				}
			}
		}
		String lang = request.getHeader("Accept-Language");
		lang = getLanguage(lang);
		return lang;
	}

	private static String[] LangIDArr = new String[] { "ar-", "be-", "bg-", "ca-", "cs-", "da-", "de-", "el-", "en-", "es-", "et-", "fi-",
			"fr-", "hr-", "hu-", "is-", "it-", "iw-", "ja-", "ko-", "lt-", "lv-", "mk-", "nl-", "no-", "pl-", "pt-", "ro-", "ru-", "sh-",
			"sk-", "sl-", "sq-", "sr-", "sv-", "th-", "tr-", "uk-" };

	public static String getLanguage(String lang) {
		if (lang == null) {
			return LangMapping.getInstance().defaultLanguage;
		}
		lang = lang.replace('_', '-').toLowerCase();
		if (lang.indexOf("zh-hans") >= 0) {
			return "zh-cn";// 简体中文
		}
		if (lang != null && lang.indexOf(',') > 0) {
			lang = lang.substring(0, lang.indexOf(",")).trim();
		}
		if (!lang.endsWith("-")) {
			lang += "-";
		}
		if (lang.startsWith("zh-")) {
			if (lang.equals("zh-tw") || lang.equals("zh-sg")) {
				return "zh-tw";// 繁体中文
			} else {
				return "zh-cn";// 简体中文
			}
		} else {
			for (String id : LangIDArr) {
				if (lang.startsWith(id)) {
					return id.substring(0, id.length() - 1);
				}
			}
		}
		return LangMapping.getInstance().defaultLanguage;
	}

	static String getCurrentLanguage() {
		String lang = null;
		if (Current.getExecuteContext() != null) {
			lang = Current.getExecuteContext().getLanguage();
		} else {
			lang = User.getLanguage();
		}
		if (lang == null) {
			lang = LangMapping.getInstance().defaultLanguage;
		}
		return lang;
	}

	/**
	 * 检查DataTable中的所有行中所有字段的值，如果是国际化字符串，则转换之
	 */
	public static void decodeDataTable(DataTable dt, String lang) {
		if (dt == null) {
			return;
		}
		for (DataRow dr : dt) {
			for (int i = 0; i < dt.getColumnCount(); i++) {
				Object obj = dr.get(i);
				if (obj instanceof String) {
					dr.set(i, decode((String) obj, lang));
				}
			}
		}
	}

	/**
	 * 对于一个字段存储多语种字符串的情况，可以使用本方法解出默认语言的字符串
	 */
	public static void decode(DataTable dt, String column) {
		decode(dt, column, getCurrentLanguage());
	}

	/**
	 * 对于一个字段存储多语种字符串的情况，可以使用本方法解出对应语言的字符串
	 */
	public static void decode(DataTable dt, String column, String lang) {
		if (dt == null) {
			return;
		}
		for (DataRow dr : dt) {
			Object obj = dr.get(column);
			if (obj instanceof String) {
				dr.set(column, decode((String) obj, lang));
			}
		}
	}

	/**
	 * 对map中value值进行国际化处理，取出当前语言的值
	 */
	public static <K, V> void decode(Map<K, V> map) {
		decode(map, getCurrentLanguage());
	}

	/**
	 * 对map中value值进行国际化处理，取出对应语言的值
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> void decode(Map<K, V> map, String lang) {
		if (map == null) {
			return;
		}
		for (Entry<K, V> e : map.entrySet()) {
			V v = e.getValue();
			if (v == null) {
				continue;
			}
			if (v instanceof String) {
				e.setValue((V) decode((String) v, lang));
			} else {
				decode(v, lang);
			}
		}
	}

	/**
	 * 对DAO中字段值进行国际化处理，取出当前语言的值
	 */
	public static void decode(DAO<?> dao) {// NO_UCD
		decode(dao, getCurrentLanguage());
	}

	/**
	 * 对DAO中字段值进行国际化处理，取出对应语言的值
	 */
	public static void decode(DAO<?> dao, String lang) {
		if (dao == null) {
			return;
		}
		for (DAOColumn c : dao.columns()) {
			Object v = dao.getV(c.getColumnName());
			if (v == null) {
				continue;
			}
			if (v instanceof String) {
				dao.setV(c.getColumnName(), decode((String) v, lang));
			}
		}
	}

	/**
	 * 对DAOSet中各个DAO的字段值进行国际化处理，取出当前语言的值
	 */
	public static void decode(DAOSet<?> set) {// NO_UCD
		decode(set, getCurrentLanguage());
	}

	/**
	 * 对DAOSet中各个DAO的字段值进行国际化处理，取出当前语言的值
	 */
	public static void decode(DAOSet<?> set, String lang) {
		if (set == null || set.size() == 0) {
			return;
		}
		for (DAOColumn c : set.get(0).columns()) {
			for (int i = 0; i < set.size(); i++) {
				Object v = set.get(i).getV(c.getColumnName());
				if (v == null) {
					continue;
				}
				if (v instanceof String) {
					set.get(i).setV(c.getColumnName(), decode((String) v, lang));
				}
			}
		}
	}

	/**
	 * 对List中value值进行国际化处理，取出当前语言的值
	 */
	public static <T> void decode(List<T> list) {// NO_UCD
		decode(list, getCurrentLanguage());
	}

	/**
	 * 对List中value值进行国际化处理，取出对应语言的值
	 */
	@SuppressWarnings("unchecked")
	public static <T> void decode(List<T> list, String lang) {
		if (list == null) {
			return;
		}
		for (int i = 0; i < list.size(); i++) {
			T v = list.get(i);
			if (v == null) {
				continue;
			}
			if (v instanceof String) {
				list.set(i, (T) decode((String) v, lang));
			} else {
				decode(v, lang);
			}
		}
	}

	/**
	 * 对数组中的值进行国际化处理，取出当前语言的值
	 */
	public static <T> void decode(T[] arr) {// NO_UCD
		decode(arr, getCurrentLanguage());
	}

	/**
	 * 对数组中的值进行国际化处理，取出对应语言的值
	 */
	@SuppressWarnings("unchecked")
	public static <T> void decode(T[] arr, String lang) {
		if (arr == null) {
			return;
		}
		for (int i = 0; i < arr.length; i++) {
			T v = arr[i];
			if (v == null) {
				continue;
			}
			if (v instanceof String) {
				arr[i] = (T) decode((String) v, lang);
			} else {
				decode(v, lang);
			}
		}
	}

	/**
	 * 获取多语种字符串中当前语言的值
	 */
	public static String decode(String src) {
		return decode(src, getCurrentLanguage());
	}

	/**
	 * 按如下步骤处理可能的国际化字符串，以便于转化为当前语言中的字符串:<br>
	 * 1、若第一个参数是形如@{ID}的字符串，则从I18n名值对中查找，如果查找到则返回当前语言对应的值<br>
	 * 2、如不是@{ID}字符串，则看是否是以LangUtil.LangFieldPrefix开头的字符串，如果是则返回当前语言对应的值<br>
	 * 3、返回原字符串
	 */
	public static String get(String str) {
		return get(str, getCurrentLanguage());
	}

	/**
	 * 按如下步骤处理可能的国际化字符串，以便于转化为lang参数对应的语言中的字符串:<br>
	 * 1、若第一个参数是形如@{ID}的字符串，则从I18n名值对中查找，如果查找到则返回lang参数对应的语言字符串<br>
	 * 2、如不是@{ID}字符串，则看是否是以LangUtil.LangFieldPrefix开头的字符串，如果是则返回lang参数对应的语言字符串<br>
	 * 3、返回原字符串
	 */
	public static String get(String str, String lang) {
		if (ObjectUtil.empty(str)) {
			return str;
		}
		if (ObjectUtil.empty(lang)) {
			lang = LangMapping.getInstance().defaultLanguage;
		}
		return decode(str, lang);
	}

	/**
	 * 获取多语种字符串中指定语言的值
	 */
	public static String decode(String str, String lang) {
		if (str == null) {
			return str;
		}
		if (ObjectUtil.empty(lang)) {
			return str;
		}
		if (str.startsWith("@{") && str.endsWith("}")) {
			return LangMapping.get(lang, str);
		}
		if (!str.startsWith(LangFieldPrefix.substring(0, LangFieldPrefix.length() - 1))) {
			return str;
		}
		int i = str.indexOf("\n" + lang);
		if (i < 0) {
			lang = LangMapping.getInstance().defaultLanguage;// 没有则找默认的
			i = str.indexOf("\n" + lang);
			if (i < 0) {
				i = str.indexOf('\n');// 找第一家
				int i2 = str.indexOf('=');
				if (i2 < 0) {
					return str;
				}
				int i3 = str.indexOf('\n', i2);
				if (i3 < 0) {
					i3 = str.length();
				}
				return str.substring(i2 + 1, i3);
			}
		}
		int i2 = str.indexOf('\n', i + lang.length() + 1);
		if (i2 < 0) {
			i2 = str.length();
		}
		return str.substring(i + lang.length() + 2, str.charAt(i2 - 1) == '\r' ? i2 - 1 : i2);
	}

	/**
	 * 对一个对象进行i18N转码
	 */
	public static void decode(Object data) {// NO_UCD
		decode(data, getCurrentLanguage());
	}

	/**
	 * 对一个对象进行i18N转码
	 */
	public static void decode(Object data, String language) {
		if (data == null) {
			return;
		}
		if (Primitives.isPrimitives(data) || Primitives.isPrimitiveArray(data)) {
			return;
		}
		if (data instanceof DAO) {
			decode((DAO<?>) data, language);
		} else if (data.getClass().isArray()) {
			for (int i = 0; i < Array.getLength(data); i++) {
				decode(Array.get(data, i), language);
			}
		} else if (data instanceof DataTable) {
			decodeDataTable((DataTable) data, language);
		} else if (data instanceof Map) {
			decode((Map<?, ?>) data, language);
		} else if (data instanceof List) {
			decode((List<?>) data, language);
		} else if (data instanceof DAOSet) {
			decode((DAOSet<?>) data, language);
		} else {// 作为Bean处理
			Map<String, Object> map = BeanUtil.toMap(data, true);
			decode(map);
			BeanUtil.fill(data, map);
		}
	}

	/**
	 * 修改多语种字符串中当前语言的值
	 */
	public static String modify(String src, String value) {
		return modify(src, getCurrentLanguage(), value);
	}

	/**
	 * 修改多语种字符串中一种语言的值
	 */
	public static String modify(String src, String lang, String value) {
		Mapx<String, String> map = null;
		if (StringUtil.isEmpty(src)) {
			return src;
		}
		if (ObjectUtil.empty(lang)) {
			return src;
		}
		src = StringUtil.replaceEx(src, "\r\n", "\n");
		if (src.startsWith(LangFieldPrefix)) {
			src = src.substring(LangFieldPrefix.length());
			map = StringUtil.splitToMapx(src, "\n", "=", '\\');
		} else {
			map = new Mapx<String, String>();
		}
		map.put(lang, value);
		StringBuilder sb = new StringBuilder();
		sb.append(LangFieldPrefix);
		for (String k : map.keySet()) {
			String v = map.get(k);
			sb.append(k);
			sb.append("=");
			sb.append(StringUtil.javaEncode(v));
			sb.append("\n");
		}
		return sb.toString();
	}

	public static String getDefaultLanguage() {
		return LangMapping.getInstance().defaultLanguage;
	}

	/**
	 * 获得应用支持的语言，通过classes/lang/lang.i18n配置
	 */
	public static Mapx<String, String> getSupportedLanguages() {
		return LangMapping.getInstance().getLanguageMap();
	}

	/**
	 * 获取前台传入的国际化字段的值
	 */
	public static String getI18nFieldValue(String name) {
		RequestData dc = Current.getRequest();
		if (dc == null) {
			throw new RuntimeException(" Current.getRequest() failed!");
		}
		String currenStr = dc.getString(name);
		String langStr = dc.getString(name + "_I18N");
		if (ObjectUtil.empty(langStr)) {
			return currenStr;
		}
		langStr = StringUtil.unescape(langStr);
		langStr = modify(langStr, currenStr);
		return langStr;
	}

	/**
	 * 替换字符串中的形如@{key}国际化占位符
	 */
	public static String replace(String str, String lang) {
		StringBuilder sb = new StringBuilder();
		int last = 0;
		while (true) {
			int i = str.indexOf("@{", last);
			if (i < 0) {
				break;
			}
			int i2 = str.indexOf('}', i + 2);
			if (i2 < 0) {
				break;
			}
			String key = str.substring(i + 2, i2);
			String v = LangMapping.get(lang, key);
			if (v == null) {
				sb.append(str.substring(last, i2 + 1));
				last = i2 + 1;
				continue;
			}
			sb.append(str.substring(last, i));
			sb.append(v);
			last = i2 + 1;
		}
		sb.append(str.substring(last));
		return sb.toString();
	}
}
