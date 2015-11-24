package com.zving.framework.utility;

import java.util.Map;

import com.zving.framework.cache.CacheManager;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.expression.ExpressionException;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.orm.DAO;
import com.zving.framework.ui.html.HtmlTD;
import com.zving.framework.ui.html.HtmlTR;
import com.zving.framework.ui.html.HtmlTable;
import com.zving.framework.ui.util.PlaceHolderUtil;

/**
 * HTML工具类
 * 
 * @Author 王育春
 * @Date 2007-6-23
 * @Mail wyuch@zving.com
 */
public class HtmlUtil {
	/**
	 * 把ZDCode中的基础代码转化为Mapx
	 */
	public static Mapx<String, Object> codeToMapx(String CodeType) {
		Map<String, Object> map = CacheManager.getMapx("Code", CodeType);
		Mapx<String, Object> map2 = new Mapx<String, Object>();
		if (map == null) {
			return map2;
		}
		for (String key : map.keySet()) {
			if (map.get(key).toString().startsWith("{")) {
				DAO<?> dao = (DAO<?>) map.get(key);
				map2.put(key, LangUtil.get(dao.getV("CodeName") + ""));
			} else {
				map2.put(key, LangUtil.get(map.get(key).toString()));
			}
		}
		return map2;
	}

	/**
	 * 把ZDCode中的基础代码转化为多选框
	 */
	public static String codeToCheckboxes(String name, String CodeType, String[] checkedArray, boolean direction) {
		return mapxToCheckboxes(name, codeToMapx(CodeType), checkedArray, null, direction);
	}

	public static String mapxToOptions(Map<?, ?> map, String type, Object checkedValue, boolean addBlankOptionFlag) {
		FastStringBuilder sb = new FastStringBuilder();
		if (addBlankOptionFlag) {
			sb.append("<").append(type).append(" value=''></").append(type).append(">");
		}
		if (map == null) {
			return sb.toStringAndClose();
		}
		Object[] keys = map.keySet().toArray();
		for (Object k : keys) {
			sb.append("<").append(type).append(" value=\"");
			sb.append(k == null ? "" : StringUtil.quickHtmlEncode(k.toString()));
			if (k != null && k.equals(checkedValue)) {
				sb.append("\" selected='true' >");
			} else {
				sb.append("\">");
			}
			sb.append(map.get(k));
			sb.append("</").append(type).append(">");
		}
		return sb.toStringAndClose();
	}

	public static String mapxToRadios(String name, Map<?, ?> map, Object checkedValue) {
		return mapxToRadios(name, map, checkedValue, false);
	}

	public static String mapxToRadios(String name, Map<?, ?> map, Object checkedValue, boolean direction) {
		FastStringBuilder sb = new FastStringBuilder();
		Object[] keys = map.keySet().toArray();
		for (int i = 0; i < keys.length; i++) {
			Object k = keys[i];
			sb.append("<input type='radio' name='").append(name);
			sb.append("' id='").append(name).append("_").append(i).append("' value='");
			sb.append(k == null ? "" : StringUtil.quickHtmlEncode(k.toString()));
			if (k != null && k.equals(checkedValue)) {
				sb.append("' checked='true' >");
			} else {
				sb.append("' />");
			}
			sb.append("<label for='").append(name).append("_").append(i).append("'>");
			sb.append(map.get(k));
			sb.append("</label>&nbsp;");
			if (direction) {
				sb.append("<br>");
			}
		}
		return sb.toStringAndClose();
	}

	public static String mapxToCheckboxes(String name, Map<?, ?> map, Object[] checkedArray) {
		return mapxToCheckboxes(name, map, checkedArray, null);
	}

	public static String mapxToCheckboxes(String name, Map<?, ?> map, Object[] checkedArray, Object[] disabledValue) {
		return mapxToCheckboxes(name, map, checkedArray, disabledValue, false);
	}

	public static String mapxToCheckboxes(String name, Map<?, ?> map, Object[] checkedArray, Object[] disabledValue, boolean direction) {
		FastStringBuilder sb = new FastStringBuilder();
		int i = 0;
		for (Object k : map.keySet()) {
			sb.append("<input type='checkbox'");
			sb.append(" name='").append(name);
			sb.append("' id='").append(name).append("_").append(i).append("' value='");
			sb.append(k == null ? "" : StringUtil.quickHtmlEncode(k.toString()));
			sb.append("'");
			if (disabledValue != null) {
				for (Object element : disabledValue) {
					if (k.equals(element)) {
						sb.append(" disabled='true'");
						break;
					}
				}
			}

			if (checkedArray != null) {
				for (Object element : checkedArray) {
					if (k.equals(element)) {
						sb.append(" checked='true'");
						break;
					}
				}
			}

			sb.append(" >");
			sb.append("<label for='").append(name).append("_").append(i).append("'>");
			sb.append(map.get(k));
			sb.append("</label>&nbsp;");
			if (direction) {
				sb.append("<br>");
			}
			i++;
		}
		return sb.toStringAndClose();
	}

	/**
	 * 将一段html中形如${Field}的占位符用map中对应的键值替换;<br>
	 * blankFlag为true时将未在map中有对应键值的占位符替换为空字符串,为false时不替换<br>
	 */
	public static String replacePlaceHolder(String html, Map<?, ?> map, boolean blankFlag) {
		return replacePlaceHolder(html, map, blankFlag, false);
	}

	/**
	 * 将一段html中形如${Field}的占位符用map中对应的键值替换;<br>
	 * blankFlag为true时将未在map中有对应键值的占位符替换为空字符串,为false时不替换<br>
	 * spaceFlag为true时将map中键值为null或者空字符串的替换为&nbsp;
	 */
	public static String replacePlaceHolder(String html, Map<?, ?> map, boolean blankFlag, boolean spaceFlag) {
		try {
			return PlaceHolderUtil.replacePlaceHolder(html, map, blankFlag, spaceFlag);
		} catch (ExpressionException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 用DataTable中的行将一段HTML多次替换
	 */
	public static String replaceWithDataTable(DataTable dt, String html) {
		return replaceWithDataTable(dt, html, false, true);
	}

	public static String replaceWithDataTable(DataTable dt, String html, boolean spaceFlag) {
		return replaceWithDataTable(dt, html, false, spaceFlag);
	}

	/**
	 * 用DataTable中的行将一段HTML多次替换<br>
	 * blankFlag为true表示当DataTable中未有对应列时保留占位符
	 * spaceFlag为true表示当DataTable中未有对应列时替换成空格
	 */
	public static String replaceWithDataTable(DataTable dt, String html, boolean blankFlag, boolean spaceFlag) {
		try {
			return PlaceHolderUtil.replaceWithDataTable(html, dt, blankFlag, spaceFlag);
		} catch (ExpressionException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 用DataRow将一段HTML替换,匹配形如${FieldName}的字符串
	 */
	public static String replaceWithDataRow(DataRow dr, String html) {
		return replaceWithDataRow(dr, html, true);
	}

	public static String replaceWithDataRow(DataRow dr, String html, boolean spaceFlag) {
		return replaceWithDataRow(dr, html, false, spaceFlag);
	}

	/**
	 * 用DataRow将一段HTML替换,spaceFlag为true表示当DataTable中未有对应列时替换成空格,匹配形如${FieldName}
	 * 的字符串
	 */
	public static String replaceWithDataRow(DataRow dr, String html, boolean blankFlag, boolean spaceFlag) {
		return replacePlaceHolder(html, dr.toMapx(), blankFlag, spaceFlag);
	}

	public static HtmlTable dataTableToHtmlTable(DataTable dt, String cellHtml, int columnCount) {
		HtmlTable table = new HtmlTable();
		HtmlTR tr = null;
		for (int i = 0; i < dt.getRowCount(); i++) {
			if (i % columnCount == 0) {
				tr = new HtmlTR();
				table.addTR(tr);
			}
			HtmlTD td = new HtmlTD();
			td.setInnerHTML(HtmlUtil.replaceWithDataRow(dt.getDataRow(i), cellHtml));
			tr.addChild(td);
		}
		for (int i = 0; i < dt.getRowCount() % columnCount; i++) {
			HtmlTD td = new HtmlTD();
			td.setInnerHTML("&nbsp;");
			tr.addChild(td);
		}
		return table;
	}
}
