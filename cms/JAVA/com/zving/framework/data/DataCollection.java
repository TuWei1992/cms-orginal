package com.zving.framework.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.zving.framework.Constant;
import com.zving.framework.collection.Filter;
import com.zving.framework.collection.Mapx;
import com.zving.framework.json.JSON;
import com.zving.framework.json.JSONObject;
import com.zving.framework.orm.DAO;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.orm.DAOUtil;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.FastStringBuilder;
import com.zving.framework.utility.StringUtil;
import com.zving.framework.xml.XMLDocument;
import com.zving.framework.xml.XMLElement;
import com.zving.framework.xml.XMLParser;

/**
 * Http请求参数的封装，对应于前端JavaScript中的DataCollection对象
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2006-7-6
 */
public class DataCollection extends JSONObject {
	private static final long serialVersionUID = 1L;

	/**
	 * 尝试获取键对应的DataTable
	 * 
	 * @param key 键
	 * @return 键对应的DataTable，如果键不存在或者对应的数据不是DataTable则返回null
	 */
	public DataTable getDataTable(String key) {
		Object o = super.get(key);
		if (DataTable.class.isInstance(o)) {
			return (DataTable) o;
		}
		return null;
	}

	/**
	 * 转换为XML字符串
	 * 
	 * @return XML字符串
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String toXML() {
		XMLDocument doc = new XMLDocument();
		doc.setEncoding("UTF-8");
		XMLElement root = doc.createRoot("collection");
		for (String id : keySet()) {
			Object value = get(id);
			XMLElement ele = root.addElement("element");
			ele.addAttribute("id", id);
			if (value == null || value.equals("")) {
				ele.addAttribute("type", "String");
				ele.addCDATA(Constant.Null);
				continue;
			}
			if (value instanceof String) {
				ele.addAttribute("type", "String");
				ele.addCDATA((String) value);
			} else if (value instanceof Integer) {
				ele.addAttribute("type", "Int");
				ele.addAttribute("value", String.valueOf(value));
			} else if (value instanceof Date) {
				ele.addAttribute("type", "String");
				ele.addAttribute("value", DateUtil.toDateTimeString((Date) value));
			} else if (value instanceof Long) {
				ele.addAttribute("type", "Long");
				ele.addAttribute("value", String.valueOf(value));
			} else if (value instanceof Float) {
				ele.addAttribute("type", "Float");
				ele.addAttribute("value", String.valueOf(value));
			} else if (value instanceof Double) {
				ele.addAttribute("type", "Double");
				ele.addAttribute("value", String.valueOf(value));
			} else if (value instanceof int[]) {
				int[] t = (int[]) value;
				StringBuilder sb = new StringBuilder();
				sb.append(t[0]);
				for (int j = 1; j < t.length; j++) {
					sb.append(",");
					sb.append(t[j]);
				}
				ele.addAttribute("type", "IntArray");
				ele.addAttribute("value", sb.toString());
			} else if (value instanceof long[]) {
				long[] t = (long[]) value;
				StringBuilder sb = new StringBuilder();
				sb.append(t[0]);
				for (int j = 1; j < t.length; j++) {
					sb.append(",");
					sb.append(t[j]);
				}
				ele.addAttribute("type", "LongArray");
				ele.addAttribute("value", sb.toString());
			} else if (value instanceof float[]) {
				float[] t = (float[]) value;
				StringBuilder sb = new StringBuilder();
				sb.append(t[0]);
				for (int j = 1; j < t.length; j++) {
					sb.append(",");
					sb.append(t[j]);
				}
				ele.addAttribute("type", "FloatArray");
				ele.addAttribute("value", sb.toString());
			} else if (value instanceof double[]) {
				double[] t = (double[]) value;
				StringBuilder sb = new StringBuilder();
				sb.append(t[0]);
				for (int j = 1; j < t.length; j++) {
					sb.append(",");
					sb.append(t[j]);
				}
				ele.addAttribute("type", "DoubleArray");
				ele.addAttribute("value", sb.toString());
			} else if (value instanceof String[]) {
				String[] t = (String[]) value;
				StringBuilder sb = new StringBuilder();
				for (int j = 0; j < t.length; j++) {
					if (j != 0) {
						sb.append(",");
					}
					sb.append("\"");
					if (t[j] == null) {
						t[j] = Constant.Null;
					}
					sb.append(StringUtil.javaEncode(t[j]));
					sb.append("\"");
				}
				ele.addAttribute("type", "StringArray");
				ele.addCDATA(sb.toString());
			} else if (value instanceof DataTable) {
				dataTableToXML((DataTable) value, ele, "DataTable");
			} else if (value instanceof DAO) {
				daoToXML((DAO) value, ele);
			} else if (value instanceof DAOSet) {
				daoSetToXML((DAOSet<?>) value, ele);
			} else if (value instanceof Mapx) {
				mapToXML((Mapx<Object, ?>) value, ele);
			}
		}
		return doc.asXML();
	}

	public String toJSON() {
		return JSON.toJSONString(this);
	}

	private void dataTableToXML(DataTable dt, XMLElement ele, String type) {
		if (dt == null) {
			throw new RuntimeException("DataTable can't be null!");
		}
		ele.addAttribute("type", type);
		DataColumn[] dcs = dt.getDataColumns();
		XMLElement cols = ele.addElement("columns");
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < dcs.length; i++) {
			if (i != 0) {
				sb.append(",");
			}
			sb.append("[\"");
			sb.append(dcs[i].getColumnName());
			sb.append("\"");
			sb.append(",");
			sb.append(dcs[i].getColumnType());
			sb.append("]");
		}
		sb.append("]");
		cols.addCDATA(sb.toString());
		sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < dt.getRowCount(); i++) {
			sb.append("[");
			for (int j = 0; j < dcs.length; j++) {
				String v = dt.getString(i, j);
				if (j == 0) {
					if (v == null) {
						continue;
					}
					sb.append("\"");
					sb.append(StringUtil.javaEncode(v));
					sb.append("\"");
				} else {
					if (v == null) {
						sb.append(",");
						continue;
					}
					sb.append(",\"");
					sb.append(StringUtil.javaEncode(v));
					sb.append("\"");
				}
			}
			if (i == dt.getRowCount() - 1) {
				sb.append("]");
			} else {
				sb.append("],\n");
			}
		}
		sb.append("]");
		XMLElement value = ele.addElement("values");
		value.addCDATA(sb.toString());
	}

	/**
	 * 将DataTable转为JS中的数组
	 * 
	 * @param dt DataTable
	 * @return JS字符串
	 */
	public static String dataTableToJS(DataTable dt) {
		FastStringBuilder sb = new FastStringBuilder();
		dataTableToJS(dt, null, sb);
		return sb.toStringAndClose();
	}

	/**
	 * 将DataTable转为JS中的数组
	 * 
	 * @param dt DataTable
	 * @param columnFilter 字段过滤器
	 * @param sb 快速字符串构造器
	 */
	public static void dataTableToJS(DataTable dt, Filter<DataColumn> columnFilter, FastStringBuilder sb) {
		if (dt == null) {
			throw new RuntimeException("DataTable cann't be null!");
		}
		boolean webModeFlag = dt.isWebMode();
		dt.setWebMode(false);
		DataColumn[] dcs = dt.getDataColumns();
		ArrayList<Integer> list = new ArrayList<Integer>();
		if (columnFilter != null) {
			for (int i = 0; i < dcs.length; i++) {
				if (columnFilter.filter(dcs[i])) {
					list.add(i);
				}
			}
		}
		int[] arr = new int[list.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = list.get(i);
		}
		sb.append("var _Zving_Cols = [");
		boolean first = true;
		for (int j : arr) {
			if (first) {
				first = false;
			} else {
				sb.append(',');
			}
			sb.append("[\"");
			sb.append(dcs[j].getColumnName());
			sb.append('\"');
			sb.append(',');
			sb.append(dcs[j].getColumnType());
			sb.append(']');
		}
		sb.append("];\n");
		sb.append("var _Zving_Values = [");
		for (int i = 0; i < dt.getRowCount(); i++) {
			sb.append('[');
			first = true;
			for (int k : arr) {
				Object v = dt.get(i, k);
				if (first) {
					first = false;
				} else {
					sb.append(',');
				}
				if (v == null) {
					sb.append("null");
					continue;
				} else if (v instanceof String) {
					sb.append('\"');
					encodeJSString((String) v, sb);
					sb.append('\"');
				} else if (v instanceof Date) {
					sb.append('\"');
					if (StringUtil.isNotEmpty(dcs[k].getDateFormat())) {
						v = DateUtil.toString((Date) v, dcs[k].getDateFormat());
					} else {
						v = DateUtil.toDateTimeString((Date) v);
					}
					sb.append((String) v);
					sb.append('\"');
				} else {
					sb.append(v);
				}
			}
			if (i == dt.getRowCount() - 1) {
				sb.append(']');
			} else {
				sb.append("],\n");
			}
		}
		dt.setWebMode(webModeFlag);
		sb.append("];\n");
	}

	/**
	 * 快速将一段JS转换可以输出到html页面中的形式
	 * 
	 * @param script 待转换的js
	 * @param sb 快速字符串构造器
	 */
	public static void encodeJSString(String script, FastStringBuilder sb) {
		int last = 0;
		for (int i = 0; i < script.length(); i++) {
			char c = script.charAt(i);
			if (c == '\"' || c == '\\' || c == '\'') {
				if (last != i) {
					sb.append(script, last, i - last);
				}
				last = i + 1;
				sb.append('\\').append(c);
			} else if (c == '<' && i < script.length() - 8) {// </script>必须拆开
				if (script.charAt(i + 1) == '/') {
					if (expectScript(script, i + 2)) {
						sb.append(script.substring(last, i + 4));
						sb.append('\"');
						sb.append('+');
						sb.append('\"');
						last = i + 4;
						i += 7;
					}
				}
			} else if (c == '\n') {
				if (last != i) {
					sb.append(script, last, i - last);
				}
				last = i + 1;
				sb.append('\\').append('n');
			} else if (c == '\r') {
				if (last != i) {
					sb.append(script, last, i - last);
				}
				last = i + 1;
				sb.append('\\').append('r');
			} else if (c == '\t') {
				if (last != i) {
					sb.append(script, last, i - last);
				}
				last = i + 1;
				sb.append('\\').append('t');
			} else if (c == '\f') {
				if (last != i) {
					sb.append(script, last, i - last);
				}
				last = i + 1;
				sb.append('\\').append('f');
			} else if (c == '\b') {
				if (last != i) {
					sb.append(script, last, i - last);
				}
				last = i + 1;
				sb.append('\\').append('b');
			}
		}
		if (last == 0) {
			sb.append(script);
		} else {
			sb.append(script, last, script.length() - last);
		}
	}

	/**
	 * 在字符串的指定位置上是否是“script”
	 * 
	 * @return 如果是则返回true，否则返回false
	 */
	private static boolean expectScript(String str, int pos) {
		char c = str.charAt(pos);
		if (c != 's' && c != 'S') {
			return false;
		}
		c = str.charAt(pos + 1);
		if (c != 'c' && c != 'C') {
			return false;
		}
		c = str.charAt(pos + 2);
		if (c != 'r' && c != 'R') {
			return false;
		}
		c = str.charAt(pos + 3);
		if (c != 'i' && c != 'I') {
			return false;
		}
		c = str.charAt(pos + 4);
		if (c != 'p' && c != 'P') {
			return false;
		}
		c = str.charAt(pos + 5);
		if (c != 't' && c != 'T') {
			return false;
		}
		return true;
	}

	/**
	 * 将DAO转换成XML元素
	 * 
	 * @param dao 待转换的DAO
	 * @param ele 当前XML元素
	 */
	private <T extends DAO<T>> void daoToXML(T dao, XMLElement ele) {
		try {
			DAOSet<T> set = dao.newSet();
			set.add(dao);
			DataTable dt = set.toDataTable();
			ele.addAttribute("tablecode", DAOUtil.getTableCode(dao));
			dataTableToXML(dt, ele, "DAO");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将DAOSet转换成XML元素
	 * 
	 * @param set 待转换的DAOSet
	 * @param ele 当前XML元素
	 */
	private void daoSetToXML(DAOSet<?> set, XMLElement ele) {
		if (set != null && set.size() != 0) {
			DataTable dt = set.toDataTable();
			ele.addAttribute("tablecode", DAOUtil.getTableCode(set.get(0)));
			dataTableToXML(dt, ele, "DAOSet");
		}
	}

	/**
	 * 解析 Javascript中的DataCollection对象生成的XML
	 * 
	 * @param xml 待解析的XML
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void parseXML(String xml) {
		if (xml == null || xml.length() == 0) {
			throw new RuntimeException("XML string can't be null!");
		}
		xml = StringUtil.replaceEx(xml, StringUtil.urlDecode("%C2%A0", "UTF-8"), " ");// 此处尚待严谨处理
		xml = StringUtil.clearForXML(xml);
		XMLParser parser = new XMLParser(xml);
		try {
			parser.parse();
			XMLElement root = parser.getDocument().getRoot();
			List<?> elements = root.elements();
			for (int i = 0; i < elements.size(); i++) {
				XMLElement ele = (XMLElement) elements.get(i);
				String id = ele.attributeValue("id");
				String type = ele.attributeValue("type");
				if (type.equals("String")) {
					String str = ele.getText();
					if (Constant.Null.equals(str)) {
						str = null;
					}
					put(id, str);
				} else if (type.equals("Int")) {
					String str = ele.attributeValue("value");
					put(id, Integer.parseInt(str));
				} else if (type.equals("Long")) {
					String str = ele.attributeValue("value");
					put(id, Long.parseLong(str));
				} else if (type.equals("Float")) {
					String str = ele.attributeValue("value");
					put(id, Float.parseFloat(str));
				} else if (type.equals("Double")) {
					String str = ele.attributeValue("value");
					put(id, Double.parseDouble(str));
				} else if (type.equals("StringArray")) {
					String str = ele.getText();
					String[] t = StringUtil.splitEx(str, "\",\"");
					for (int j = 0; j < t.length; j++) {
						t[j] = StringUtil.javaDecode(t[j]);
					}
					put(id, t);
				} else if (type.equals("IntArray")) {
					String str = ele.attributeValue("value");
					String[] t = str.split(",");
					int[] a = new int[t.length];
					for (int j = 0; j < t.length; j++) {
						a[j] = Integer.parseInt(t[j]);
					}
					put(id, a);
				} else if (type.equals("LongArray")) {
					String str = ele.attributeValue("value");
					String[] t = str.split(",");
					long[] a = new long[t.length];
					for (int j = 0; j < t.length; j++) {
						a[j] = Long.parseLong(t[j]);
					}
					put(id, a);
				} else if (type.equals("FloatArray")) {
					String str = ele.attributeValue("value");
					String[] t = str.split(",");
					float[] a = new float[t.length];
					for (int j = 0; j < t.length; j++) {
						a[j] = Float.parseFloat(t[j]);
					}
					put(id, a);
				} else if (type.equals("DoubleArray")) {
					String str = ele.attributeValue("value");
					String[] t = str.split(",");
					double[] a = new double[t.length];
					for (int j = 0; j < t.length; j++) {
						a[j] = Double.parseDouble(t[j]);
					}
					put(id, a);
				} else if (type.equals("DataTable")) {
					DataTable dt = parseDataTable(ele);
					put(id, dt);
				} else if (type.equals("Map")) {
					Mapx<String, String> map = parseMap(ele);
					put(id, map);
				} else if (type.equals("DAO")) {
					Class<?> c = Class.forName(ele.attributeValue("ClassName"));
					DAO DAO = (DAO) c.newInstance();
					DataTable dt = parseDataTable(ele);
					for (int j = 0; j < DAOUtil.getColumns(DAO).length; j++) {
						DAO.setV(i, dt.get(0, j));
					}
					put(id, DAO);
				} else if (type.equals("DAOSet")) {
					Class<?> c = Class.forName(ele.attributeValue("ClassName"));
					DataTable dt = parseDataTable(ele);
					DAOSet set = ((DAO) c.newInstance()).newSet();
					for (int j = 0; j < dt.getRowCount(); j++) {
						DAO dao = (DAO) c.newInstance();
						for (int k = 0; k < DAOUtil.getColumns(dao).length; k++) {
							dao.setV(i, dt.get(j, k));
						}
						set.add(dao);
					}
					put(id, set);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析JSON格式的字符串
	 * 
	 * @param json 待解析的JSON
	 */
	public void parseJSON(String json) {
		if (json == null || json.length() == 0) {
			throw new RuntimeException("JSON string can't be null!");
		}
		json = StringUtil.replaceEx(json, StringUtil.urlDecode("%C2%A0", "UTF-8"), " ");// 此处尚待严谨处理
		json = StringUtil.clearForXML(json);
		try {
			Object obj = JSON.parse(json);
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) obj;
			this.putAll(map);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param ele XML元素
	 * @return 将XML元素解析成Map
	 */
	private Mapx<String, String> parseMap(XMLElement ele) {
		Mapx<String, String> map = new Mapx<String, String>();
		String str = ele.getText();
		str = str.trim().substring(1, str.length() - 1);
		String[] arr = StringUtil.splitEx(str, ",\"");
		for (String element : arr) {
			String[] arr2 = StringUtil.splitEx(element, "\":");
			if (arr2.length != 2) {
				continue;
			}
			String k = arr2[0];
			String v = arr2[1];
			if (k.startsWith("\"")) {
				k = k.substring(1);
			}
			if (k.endsWith("\"")) {
				k = k.substring(0, k.length() - 1);
			}
			if (v.startsWith("\"")) {
				v = v.substring(1);
			}
			if (v.endsWith("\"")) {
				v = v.substring(0, v.length() - 1);
			}
			map.put(k, v);
		}
		return map;
	}

	/**
	 * 将map转成XML元素
	 * 
	 * @param map Map
	 * @param ele XML元素
	 */
	private void mapToXML(Mapx<?, ?> map, XMLElement ele) {
		ele.addAttribute("type", "Map");
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		int i = 0;
		for (Object key : map.keySet()) {
			if (i != 0) {
				sb.append(",");
			}
			sb.append(key);
			sb.append(":\"");
			sb.append(StringUtil.javaEncode(String.valueOf(map.get(key))));
			sb.append("\"");
			i++;
		}
		sb.append("}");
		ele.addCDATA(sb.toString());
	}

	/**
	 * @param ele XML元素
	 * @return 将XML元素解析成DataTable
	 */
	private DataTable parseDataTable(XMLElement ele) {
		String str = ele.element("columns").getText();
		String[] t = StringUtil.splitEx(str, "],[");
		DataColumn[] dcs = new DataColumn[t.length];
		t[0] = t[0].substring(2);
		int length = t.length;
		t[length - 1] = t[length - 1].substring(0, t[length - 1].length() - 2);
		for (int i = 0; i < t.length; i++) {
			dcs[i] = new DataColumn();
			int index = t[i].lastIndexOf(",");
			dcs[i].setColumnName(t[i].substring(1, index - 1));
			dcs[i].setColumnType(Integer.parseInt(t[i].substring(index + 1)));
		}
		String value = ele.element("values").getText();
		t = value.split("\\\"\\]\\,\\s*?\\[\\\"");
		if (t[0].equals("[]")) {
			return new DataTable(dcs, null);
		}
		t[0] = t[0].substring(3);
		length = t.length;
		t[length - 1] = t[length - 1].substring(0, t[length - 1].length() - 3);

		Object[][] values = new Object[length][dcs.length];
		for (int i = 0; i < t.length; i++) {
			String[] r = t[i].split("\",\"");
			for (int j = 0; j < r.length; j++) {
				if (dcs[j].getColumnType() == DataTypes.STRING || dcs[j].getColumnType() == DataTypes.CLOB) {
					values[i][j] = StringUtil.javaDecode(r[j]);
					if (r[j].equals("_ZVING_NULL") || StringUtil.isEmpty(r[j])) {
						values[i][j] = null;
					}
				} else {
					if (r[j].equals("_ZVING_NULL") || StringUtil.isEmpty(r[j])) {
						values[i][j] = null;
					} else if (dcs[j].getColumnType() == DataTypes.BIGDECIMAL) {
						values[i][j] = new Double(Double.parseDouble(r[j]));
					} else if (dcs[j].getColumnType() == DataTypes.DATETIME) {
						values[i][j] = DateUtil.parse(r[j]);
					} else if (dcs[j].getColumnType() == DataTypes.DECIMAL) {
						values[i][j] = new Double(Double.parseDouble(r[j]));
					} else if (dcs[j].getColumnType() == DataTypes.DOUBLE) {
						values[i][j] = new Double(Double.parseDouble(r[j]));
					} else if (dcs[j].getColumnType() == DataTypes.FLOAT) {
						values[i][j] = new Float(Float.parseFloat(r[j]));
					} else if (dcs[j].getColumnType() == DataTypes.INTEGER) {
						values[i][j] = new Integer(Integer.parseInt(r[j]));
					} else if (dcs[j].getColumnType() == DataTypes.LONG) {
						values[i][j] = new Long(Long.parseLong(r[j]));
					} else if (dcs[j].getColumnType() == DataTypes.SMALLINT) {
						values[i][j] = new Integer(Integer.parseInt(r[j]));
					}
				}
			}
		}
		return new DataTable(dcs, values);
	}
}
