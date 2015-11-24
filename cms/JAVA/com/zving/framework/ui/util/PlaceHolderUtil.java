package com.zving.framework.ui.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.expression.DefaultFunctionMapper;
import com.zving.framework.expression.ExpressionException;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.ui.zhtml.ZhtmlManagerContext;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.FastStringBuilder;

/**
 * 占位符工具
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-3-26
 */
public class PlaceHolderUtil {
	public static List<PlaceHolder> parse(String html) {
		List<PlaceHolder> list = new ArrayList<PlaceHolder>();
		int lastIndex = 0, pos = 0;
		while ((pos = html.indexOf("${", lastIndex)) >= 0) {
			if (pos != 0 && lastIndex != pos) {
				PlaceHolder p = new PlaceHolder();
				p.isString = true;
				p.Value = html.substring(lastIndex, pos);
				list.add(p);
			}
			char lastStringChar = 0;
			boolean escapeFlag = false;
			boolean continueFlag = false;
			for (int i = pos + 2; i < html.length(); i++) {
				char c = html.charAt(i);
				if (c == '\\') {
					escapeFlag = true;
					continue;
				}
				if (!escapeFlag) {
					if (c == '\"' || c == '\'') {
						if (lastStringChar == c) {
							lastStringChar = 0;
							continue;
						} else {
							lastStringChar = c;
						}
					}
					if (c == '}' && lastStringChar == 0) {
						String v = html.substring(pos, i + 1);
						PlaceHolder p = new PlaceHolder();
						p.isExpression = true;
						p.Value = v;
						list.add(p);
						lastIndex = i + 1;
						continueFlag = true;
						break;
					}
					if (c == '\n') {
						lastIndex = i + 1;
						continueFlag = true;
						break;
					}
				} else {
					escapeFlag = false;
					continue;
				}
			}
			if (!continueFlag) {
				break;
			}
		}
		if (lastIndex != html.length()) {
			PlaceHolder p = new PlaceHolder();
			p.isString = true;
			p.Value = html.substring(lastIndex);
			list.add(p);
		}
		return list;
	}

	public static String replacePlaceHolder(String html, Map<?, ?> map, boolean blankFlag, boolean spaceFlag) throws ExpressionException {
		if (html == null || map == null) {
			return "";
		}
		String blank = "";
		if (spaceFlag) {
			blank = "&nbsp;";
		}
		List<PlaceHolder> list = parse(html);
		FastStringBuilder sb = new FastStringBuilder();
		LoopVariableResolver vr = new LoopVariableResolver();
		vr.setMap(map);
		for (PlaceHolder p : list) {
			if (p.isString) {
				sb.append(p.Value);
			}
			if (p.isExpression) {
				Object v = ZhtmlManagerContext.getInstance().getEvaluator()
						.evaluate(p.Value, Object.class, vr, DefaultFunctionMapper.getInstance());
				if (v != null) {
					if (v == null || v.equals("")) {
						sb.append(blank);
					} else if (v instanceof String) {
						String str = (String) v;
						str = LangUtil.get(str);
						sb.append(str);
					} else if (v instanceof Date) {
						String str = DateUtil.toDateTimeString((Date) v);
						sb.append(str);
					} else {
						sb.append(v);
					}
				} else if (blankFlag) {
					sb.append("");
				} else {
					sb.append(p.Value);
				}
			}
		}
		return sb.toStringAndClose();
	}

	public static String replaceWithDataTable(String html, DataTable dt, boolean blankFlag, boolean spaceFlag) throws ExpressionException {
		if (html == null || dt == null) {
			return "";
		}
		String blank = "";
		if (spaceFlag) {
			blank = "&nbsp;";
		}
		List<PlaceHolder> list = parse(html);
		FastStringBuilder sb = new FastStringBuilder();
		LoopVariableResolver vr = new LoopVariableResolver();
		for (DataRow dr : dt) {
			vr.setDataRow(dr);
			for (PlaceHolder p : list) {
				if (p.isString) {
					sb.append(p.Value);
				}
				if (p.isExpression) {
					Object v = ZhtmlManagerContext.getInstance().getEvaluator()
							.evaluate(p.Value, Object.class, vr, DefaultFunctionMapper.getInstance());
					if (v != null) {
						if (v == null || v.equals("")) {
							sb.append(blank);
						} else if (v instanceof String) {
							String str = (String) v;
							str = LangUtil.get(str);
							sb.append(str);
						} else if (v instanceof Date) {
							String str = DateUtil.toDateTimeString((Date) v);
							sb.append(str);
						} else {
							sb.append(v);
						}
					} else if (blankFlag) {
						sb.append("");
					} else {
						sb.append(p.Value);
					}
				}
			}
		}
		return sb.toStringAndClose();
	}

}
