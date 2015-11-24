package com.zving.framework.annotation.util;

import com.zving.framework.collection.CaseIgnoreMapx;
import com.zving.framework.collection.Mapx;
import com.zving.framework.collection.ReadOnlyMapx;

/**
 * UI方法的别名映射，在三个地方可以使用别名映射:<br>
 * 1、JS中的Server.sendRequest()方法<br>
 * 2、各个标签的method属性<br>
 * 3、action URL路径映射<br>
 * <br>
 * 注意：别名不区分大小写<br>
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-30
 */
public class AliasMapping {
	private static CaseIgnoreMapx<String, String> map = new CaseIgnoreMapx<String, String>();

	/**
	 * @param alias 别名
	 * @return 别名对应的UI方法全名，如果没有找到对应的UI方法，则返回别名本身（有可能没有别名直接使用全路径）。
	 */
	public static String get(String alias) {
		AnnotationVisitor.load();
		alias = normalize(alias);
		String m = map.get(alias);
		if (m != null) {
			return m;
		}
		return alias;// 返回本身，有可能没有别名直接使用全路径
	}

	/**
	 * @param alias 别名
	 * @return 重整后的别名，将斜杠转为圆点，去掉前后的圆点（如果有的话）。
	 */
	private static String normalize(String alias) {
		if (alias == null) {
			return null;
		}
		alias = alias.replace('/', '.');
		if (alias.endsWith(".")) {
			alias = alias.substring(0, alias.length() - 1);
		}
		if (alias.startsWith(".")) {
			alias = alias.substring(1);
		}
		return alias;
	}

	/**
	 * 添加一个别名
	 * 
	 * @param alias 别名
	 * @param method 别名对应的UI方法全名
	 */
	public static void put(String alias, String method) {
		if (alias == null) {
			return;
		}
		alias = normalize(alias);
		method = normalize(method);
		map.put(alias, method);
	}

	/**
	 * 获得所有的别名
	 */
	public static Mapx<String, String> getAll() {
		AnnotationVisitor.load();
		return new ReadOnlyMapx<String, String>(map);
	}
}
