package com.zving.framework.data.sql;

import java.util.ArrayList;

import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 将一个Select SQL语句分解成各组成部分，但该SQL语句不能包含Union
 * 
 * @Author 王育春
 * @Date 2008-9-18
 * @Mail wyuch@zving.com
 */
public class SelectSQLParser {
	private String SQL = null;

	private String lowerSQL = null;

	private String ColumnPart;

	private String FromPart;

	private String WherePart;

	private String OrderbyPart;

	private String GroupByPart;

	private String HavingPart;

	private String FormattedSQL;

	private String[] Columns;

	private String[] Tables;

	private String[] Conditions;

	private String[] ConditionLogics;

	private String[] OrderByFields;

	private String[] GroupByFields;

	private String[] HavingConditions;

	private String[] HavingConditionLogics;

	/**
	 * @param sql 待解析的Select SQL
	 */
	public SelectSQLParser(String sql) {
		if (ObjectUtil.empty(sql)) {
			throw new NullPointerException();
		}
		SQL = sql.trim();
		lowerSQL = SQL.toLowerCase();
	}

	/**
	 * 解析Select SQL
	 * 
	 * @throws Exception
	 */
	public void parse() throws Exception {
		if (!lowerSQL.startsWith("select ")) {
			throw new Exception("String：\"" + SQL + "\" is not a Select SQL statement.");
		}
		// 初始化各变量为空，以便于多次setSQL后执行parse
		ColumnPart = null;
		FromPart = null;
		WherePart = null;
		GroupByPart = null;
		HavingPart = null;
		OrderbyPart = null;
		FormattedSQL = null;

		ArrayList<String> cols = new ArrayList<String>();
		ArrayList<String> tables = new ArrayList<String>();

		int leftBracketCount = 0;
		int rightBracketCount = 0;
		int singleQuoteCount = 0;
		int startIndex = 6;
		// 以下解析各列
		for (int i = startIndex; i < SQL.length(); i++) {
			if (SQL.charAt(i) == '(') {
				leftBracketCount++;
			}
			if (SQL.charAt(i) == ')') {
				rightBracketCount++;
			}
			if (SQL.charAt(i) == '\'') {
				singleQuoteCount++;
			}
			if (leftBracketCount == rightBracketCount && singleQuoteCount % 2 == 0) {
				if (SQL.charAt(i) == ',') {
					cols.add(SQL.substring(startIndex, i).trim());
					startIndex = i + 1;
				}
				if (SQL.charAt(i) == ' ' && lowerSQL.substring(i).trim().startsWith("from ")) {
					cols.add(SQL.substring(startIndex, i).trim());
					startIndex = lowerSQL.indexOf("from ", i) + 5;
					break;
				}
			}
		}
		Columns = new String[cols.size()];
		for (int i = 0; i < cols.size(); i++) {
			Columns[i] = cols.get(i);
		}
		// 以下解析FromTable
		for (int i = startIndex; i < SQL.length(); i++) {
			if (SQL.charAt(i) == '(') {
				leftBracketCount++;
			}
			if (SQL.charAt(i) == ')') {
				rightBracketCount++;
			}
			if (SQL.charAt(i) == '\'') {
				singleQuoteCount++;
			}
			if (leftBracketCount == rightBracketCount && singleQuoteCount % 2 == 0) {
				if (SQL.charAt(i) == ',') {
					tables.add(SQL.substring(startIndex, i).trim());
					startIndex = i + 1;
				}
				if (SQL.charAt(i) == ' ') {
					if (lowerSQL.substring(i).trim().startsWith("where ")) {
						tables.add(SQL.substring(startIndex, i).trim());
						parseWhere(SQL.substring(i + 1).trim());
						break;
					}
					if (lowerSQL.substring(i).trim().startsWith("group ")) {
						tables.add(SQL.substring(startIndex, i).trim());
						parseGroupBy(SQL.substring(i + 1).trim());
						break;
					}
					if (lowerSQL.substring(i).trim().startsWith("order ")) {
						tables.add(SQL.substring(startIndex, i).trim());
						parseOrderBy(SQL.substring(i + 1).trim());
						break;
					}
				}
				if (i == SQL.length() - 1) {
					tables.add(SQL.substring(startIndex).trim());
				}
			}
		}
		Tables = new String[tables.size()];
		for (int i = 0; i < tables.size(); i++) {
			Tables[i] = tables.get(i);
		}
	}

	/**
	 * 解析where子句
	 * 
	 * @param wherePart SQL语句中的where部分
	 */
	private void parseWhere(String wherePart) {
		int leftBracketCount = 0;
		int rightBracketCount = 0;
		int singleQuoteCount = 0;
		int startIndex = 6;
		String lowerWherePart = wherePart.toLowerCase();
		ArrayList<String> conditions = new ArrayList<String>();
		ArrayList<String> conditionLogics = new ArrayList<String>();
		// 以下解析wherePart
		for (int i = startIndex; i < wherePart.length(); i++) {
			if (wherePart.charAt(i) == '(') {
				leftBracketCount++;
			}
			if (wherePart.charAt(i) == ')') {
				rightBracketCount++;
			}
			if (wherePart.charAt(i) == '\'') {
				singleQuoteCount++;
			}
			if (leftBracketCount == rightBracketCount && singleQuoteCount % 2 == 0) {
				if (wherePart.charAt(i) == ' ') {
					if (lowerWherePart.substring(i).trim().startsWith("and ")) {
						conditions.add(wherePart.substring(startIndex, i).trim());
						conditionLogics.add("and");
						startIndex = i = lowerWherePart.indexOf("and ", i) + 3;
					}
					if (lowerWherePart.substring(i).trim().startsWith("or ")) {
						conditions.add(wherePart.substring(startIndex, i).trim());
						conditionLogics.add("or");
						startIndex = i = lowerWherePart.indexOf("or ", i) + 2;
					}
					if (lowerWherePart.substring(i).trim().startsWith("group ")) {
						conditions.add(wherePart.substring(startIndex, i).trim());
						parseGroupBy(wherePart.substring(i + 1).trim());
						break;
					}
					if (lowerWherePart.substring(i).trim().startsWith("order ")) {
						conditions.add(wherePart.substring(startIndex, i).trim());
						parseOrderBy(wherePart.substring(i + 1).trim());
						break;
					}
				}
				if (i == wherePart.length() - 1) {
					conditions.add(wherePart.substring(startIndex).trim());
				}

			}
		}
		Conditions = new String[conditions.size()];
		if (conditionLogics.size() > 0) {
			ConditionLogics = new String[conditions.size()];
		}
		for (int i = 0; i < conditions.size(); i++) {
			Conditions[i] = conditions.get(i);
			if (i < conditionLogics.size()) {
				ConditionLogics[i] = conditionLogics.get(i);
			}
		}
	}

	/**
	 * 解析group by子句
	 * 
	 * @param groupPart SQL中的group by部分
	 */
	private void parseGroupBy(String groupPart) {
		int leftBracketCount = 0;
		int rightBracketCount = 0;
		int singleQuoteCount = 0;
		String lowerGroupPart = groupPart.toLowerCase();
		int startIndex = lowerGroupPart.indexOf(" by ") + 3;
		ArrayList<String> groupFields = new ArrayList<String>();
		// 以下解析wherePart
		for (int i = startIndex; i < groupPart.length(); i++) {
			if (groupPart.charAt(i) == '(') {
				leftBracketCount++;
			}
			if (groupPart.charAt(i) == ')') {
				rightBracketCount++;
			}
			if (groupPart.charAt(i) == '\'') {
				singleQuoteCount++;
			}
			if (leftBracketCount == rightBracketCount && singleQuoteCount % 2 == 0) {
				if (groupPart.charAt(i) == ',') {
					groupFields.add(groupPart.substring(startIndex, i).trim());
					startIndex = i + 1;
				}
				if (groupPart.charAt(i) == ' ') {
					if (lowerGroupPart.substring(i).trim().startsWith("having ")) {
						groupFields.add(groupPart.substring(startIndex, i).trim());
						parseHaving(groupPart.substring(i + 1).trim());
						break;
					}
					if (lowerGroupPart.substring(i).trim().startsWith("order ")) {
						groupFields.add(groupPart.substring(startIndex, i).trim());
						parseOrderBy(groupPart.substring(i + 1).trim());
						break;
					}
				}
				if (i == groupPart.length() - 1) {
					groupFields.add(groupPart.substring(startIndex).trim());
				}

			}
		}
		GroupByFields = new String[groupFields.size()];
		for (int i = 0; i < groupFields.size(); i++) {
			GroupByFields[i] = groupFields.get(i);
		}
	}

	/**
	 * 解析order by子句
	 * 
	 * @param orderPart SQL中的order by 部分
	 */
	private void parseOrderBy(String orderPart) {
		int leftBracketCount = 0;
		int rightBracketCount = 0;
		int singleQuoteCount = 0;
		ArrayList<String> orderFields = new ArrayList<String>();
		int startIndex = orderPart.toLowerCase().indexOf(" by ") + 3;
		// 以下解析wherePart
		for (int i = startIndex; i < orderPart.length(); i++) {
			if (orderPart.charAt(i) == '(') {
				leftBracketCount++;
			}
			if (orderPart.charAt(i) == ')') {
				rightBracketCount++;
			}
			if (orderPart.charAt(i) == '\'') {
				singleQuoteCount++;
			}
			if (leftBracketCount == rightBracketCount && singleQuoteCount % 2 == 0) {
				if (orderPart.charAt(i) == ',') {
					orderFields.add(orderPart.substring(startIndex, i).trim());
					startIndex = i + 1;
				}
				if (i == orderPart.length() - 1) {
					orderFields.add(orderPart.substring(startIndex).trim());
				}

			}
		}
		OrderByFields = new String[orderFields.size()];
		for (int i = 0; i < orderFields.size(); i++) {
			OrderByFields[i] = orderFields.get(i);
		}
	}

	/**
	 * 解析having子句
	 * 
	 * @param havingPart SQL语句中的having部分
	 */
	private void parseHaving(String havingPart) {
		int leftBracketCount = 0;
		int rightBracketCount = 0;
		int singleQuoteCount = 0;
		int startIndex = 7;
		String lowerHavingPart = havingPart.toLowerCase();
		ArrayList<String> havingConditions = new ArrayList<String>();
		ArrayList<String> havingConditionLogics = new ArrayList<String>();
		// 以下解析wherePart
		for (int i = startIndex; i < havingPart.length(); i++) {
			if (havingPart.charAt(i) == '(') {
				leftBracketCount++;
			}
			if (havingPart.charAt(i) == ')') {
				rightBracketCount++;
			}
			if (havingPart.charAt(i) == '\'') {
				singleQuoteCount++;
			}
			if (leftBracketCount == rightBracketCount && singleQuoteCount % 2 == 0) {
				if (havingPart.charAt(i) == ' ') {
					if (lowerHavingPart.substring(i).trim().startsWith("and ")) {
						havingConditions.add(havingPart.substring(startIndex, i).trim());
						havingConditionLogics.add("and");
						startIndex = i = lowerHavingPart.indexOf("and ", i) + 3;
					}
					if (lowerHavingPart.substring(i).trim().startsWith("or ")) {
						havingConditions.add(havingPart.substring(startIndex, i).trim());
						havingConditionLogics.add("or");
						startIndex = i = lowerHavingPart.indexOf("or ", i) + 2;
					}
					if (lowerHavingPart.substring(i).trim().startsWith("order ")) {
						havingConditions.add(havingPart.substring(startIndex, i).trim());
						parseOrderBy(havingPart.substring(i + 1).trim());
						break;
					}
				}
				if (i == havingPart.length() - 1) {
					havingConditions.add(havingPart.substring(startIndex).trim());
				}

			}
		}
		HavingConditions = new String[havingConditions.size()];
		if (havingConditionLogics.size() > 0) {
			HavingConditionLogics = new String[havingConditionLogics.size()];
		}
		for (int i = 0; i < havingConditions.size(); i++) {
			HavingConditions[i] = havingConditions.get(i);
			if (i < havingConditions.size() - 1) {
				HavingConditionLogics[i] = havingConditionLogics.get(i);
			}
		}

	}

	/**
	 * 从解析结果中再次生成各部分（子句）
	 */
	private void generatePartString() {
		StringBuilder sb = new StringBuilder();
		// Columns
		for (int i = 0; i < Columns.length; i++) {
			if (i != 0) {
				sb.append(",");
			}
			sb.append(Columns[i]);

		}
		ColumnPart = sb.toString();

		// Tables
		sb = new StringBuilder();
		for (int i = 0; i < Tables.length; i++) {
			if (i != 0) {
				sb.append(",");
			}
			sb.append(Tables[i]);
		}
		FromPart = sb.toString();

		// Where
		sb = new StringBuilder();
		if (Conditions == null) {
			WherePart = "";
		} else {
			for (int i = 0; i < Conditions.length; i++) {
				if (i != 0) {
					sb.append(" ");
				}
				sb.append(Conditions[i]);
				if (i != Conditions.length - 1) {
					sb.append(" ");
					sb.append(ConditionLogics[i]);
				}
			}
			WherePart = sb.toString();
		}

		// Group By
		sb = new StringBuilder();
		if (GroupByFields == null) {
			GroupByPart = "";
		} else {
			for (int i = 0; i < GroupByFields.length; i++) {
				if (i != 0) {
					sb.append(",");
				}
				sb.append(GroupByFields[i]);
			}
			GroupByPart = sb.toString();
		}
		// Having
		sb = new StringBuilder();
		if (HavingConditions == null) {
			HavingPart = "";
		} else {
			for (int i = 0; i < HavingConditions.length; i++) {
				if (i != 0) {
					sb.append(" ");
				}
				sb.append(HavingConditions[i]);
				if (i != HavingConditions.length - 1) {
					sb.append(" ");
					sb.append(HavingConditionLogics[i]);
				}
			}
			HavingPart = sb.toString();
		}
		// Order By
		sb = new StringBuilder();
		if (OrderByFields == null) {
			OrderbyPart = "";
		} else {
			for (int i = 0; i < OrderByFields.length; i++) {
				if (i != 0) {
					sb.append(",");
				}
				sb.append(OrderByFields[i]);
			}
			OrderbyPart = sb.toString();
		}
	}

	/**
	 * @return 格式化后的select语句
	 */
	public String getFormattedSQL() {
		if (FormattedSQL != null) {
			return FormattedSQL;
		}
		StringBuilder sb = new StringBuilder();
		// Columns
		sb.append("SELECT\n\t");
		for (int i = 0; i < Columns.length; i++) {
			if (i != 0) {
				sb.append(",\n\t");
			}
			sb.append(Columns[i]);
		}

		// Tables
		sb.append("\nFROM\n\t");
		for (int i = 0; i < Tables.length; i++) {
			if (i != 0) {
				sb.append(",\n\t");
			}
			sb.append(Tables[i]);
		}

		// Where
		if (Conditions != null) {
			sb.append("\nWHERE\n");
			for (int i = 0; i < Conditions.length; i++) {
				sb.append("\t");
				sb.append(Conditions[i]);
				if (i != Conditions.length - 1) {
					sb.append(" ");
					sb.append(ConditionLogics[i]);
				}
				sb.append(" \n");
			}
		}

		// Group By
		if (GroupByFields != null) {
			sb.append("GROUP BY\n\t");
			for (int i = 0; i < GroupByFields.length; i++) {
				if (i != 0) {
					sb.append(",\n\t");
				}
				sb.append(GroupByFields[i]);
			}
		}
		// Having
		if (HavingConditions != null) {
			sb.append("\nHAVING\n");
			for (int i = 0; i < HavingConditions.length; i++) {
				sb.append("\t");
				sb.append(HavingConditions[i]);
				if (i != HavingConditions.length - 1) {
					sb.append(" ");
					sb.append(HavingConditionLogics[i]);
				}
				sb.append(" \n");
			}
		}
		// Order By
		if (OrderByFields != null) {
			sb.append("ORDER BY\n\t");
			for (int i = 0; i < OrderByFields.length; i++) {
				if (i != 0) {
					sb.append(",\n\t");
				}
				sb.append(OrderByFields[i]);
			}
		}
		FormattedSQL = sb.toString();
		return FormattedSQL;
	}

	/**
	 * @return SQLServer(2005以上)下的分页语句
	 */
	public String getMSSQLPagedSQL() {
		StringBuilder sb = new StringBuilder();
		// Columns
		sb.append("select * from (select ");
		sb.append(StringUtil.join(Columns));
		sb.append(",ROW_NUMBER() over (");
		// Order By
		if (OrderByFields != null) {
			sb.append("order by ");
			sb.append(StringUtil.join(OrderByFields));
		} else {
			String tmp = ("," + StringUtil.join(Columns, ",") + ",").toLowerCase();
			if (tmp.indexOf(",id,") >= 0) {
				sb.append("order by id");
			} else if (tmp.indexOf(",addtime,") >= 0) {
				sb.append("order by addtime");
			} else if (tmp.equals(",*,")) {
				sb.append("order by current_timestamp");// 通用分页功能，不支持2000，支持sqlserver2005,2008+
			} else {
				String column = Columns[0];
				if (column.trim().indexOf(' ') > 0) {// 说明有可能是子查询
					sb.append("order by id");
				} else if (column.indexOf("*") > 0) {// 说明可能是类似ZDUser.*这样的格式
					sb.append("order by current_timestamp");
				} else {
					sb.append("order by " + Columns[0]);
				}
			}
		}
		sb.append(") as _RowNumber");

		// Tables
		sb.append(" from ");
		sb.append(StringUtil.join(Tables));

		// Where
		if (Conditions != null) {
			sb.append(" where ");
			for (int i = 0; i < Conditions.length; i++) {
				sb.append(" ");
				sb.append(Conditions[i]);
				if (i != Conditions.length - 1) {
					sb.append(" ");
					sb.append(ConditionLogics[i]);
				}
				sb.append(" ");
			}
		}

		// Group By
		if (GroupByFields != null) {
			sb.append(" group by ");
			sb.append(StringUtil.join(GroupByFields));
		}
		// Having
		if (HavingConditions != null) {
			sb.append(" having ");
			for (int i = 0; i < HavingConditions.length; i++) {
				sb.append(" ");
				sb.append(HavingConditions[i]);
				if (i != HavingConditions.length - 1) {
					sb.append(" ");
					sb.append(HavingConditionLogics[i]);
				}
				sb.append(" ");
			}
		}
		sb.append(") _Results where  _RowNumber between ? and ?");
		return sb.toString();
	}

	/**
	 * @return Mysql下的count语句
	 */
	public String getMysqlCountSQL() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from ");
		// Group By
		if (GroupByFields != null) {
			sb.append("(");
			sb.append("select ");
			sb.append(StringUtil.join(GroupByFields));
			sb.append(" from ");
			sb.append(StringUtil.join(Tables));
		} else {
			sb.append(StringUtil.join(Tables));
		}
		// Where
		if (Conditions != null) {
			sb.append(" where ");
			for (int i = 0; i < Conditions.length; i++) {
				sb.append(" ");
				sb.append(Conditions[i]);
				if (i != Conditions.length - 1) {
					sb.append(" ");
					sb.append(ConditionLogics[i]);
				}
				sb.append(" ");
			}
		}
		// Group By
		if (GroupByFields != null) {
			sb.append(" group by ");
			sb.append(StringUtil.join(GroupByFields));
			// Having
			if (HavingConditions != null) {
				sb.append(" having ");
				for (int i = 0; i < HavingConditions.length; i++) {
					sb.append(" ");
					sb.append(HavingConditions[i]);
					if (i != HavingConditions.length - 1) {
						sb.append(" ");
						sb.append(HavingConditionLogics[i]);
					}
					sb.append(" ");
				}
			}
			sb.append(") as t");
		}
		return sb.toString();
	}

	/**
	 * @param pageSize 分页大小
	 * @param pageIndex 第几页
	 * @param connID 连接ID
	 * @return Sybase ASE下的分页语句
	 */
	public String getSybasePagedSQL(int pageSize, int pageIndex, int connID) {
		StringBuilder sb = new StringBuilder();
		sb.append("set rowcount ");
		sb.append((pageIndex + 1) * pageSize);
		sb.append(" select ");
		sb.append(StringUtil.join(Columns));
		sb.append(",_RowNumber=identity(12) into #tmp_z_");
		sb.append(connID);
		sb.append(" from ");
		sb.append(StringUtil.join(Tables));
		if (Conditions != null) {
			sb.append(" where ");
			for (int i = 0; i < Conditions.length; i++) {
				sb.append(" ");
				sb.append(Conditions[i]);
				if (i != Conditions.length - 1) {
					sb.append(" ");
					sb.append(ConditionLogics[i]);
				}
				sb.append(" ");
			}
		}
		if (OrderByFields != null) {
			sb.append(" order by ");
			sb.append(StringUtil.join(OrderByFields));
		}
		if (GroupByFields != null) {
			sb.append(" group by ");
			sb.append(StringUtil.join(GroupByFields));
		}
		if (HavingConditions != null) {
			sb.append(" having ");
			for (int i = 0; i < HavingConditions.length; i++) {
				sb.append(" ");
				sb.append(HavingConditions[i]);
				if (i != HavingConditions.length - 1) {
					sb.append(" ");
					sb.append(HavingConditionLogics[i]);
				}
				sb.append(" ");
			}
		}
		sb.append(" select * from #tmp_z_");
		sb.append(connID);
		sb.append(" where _RowNumber>");
		sb.append(pageIndex * pageSize);
		sb.append(" drop table #tmp_z_");
		sb.append(connID);
		sb.append(" set rowcount 0");
		return sb.toString();
	}

	/**
	 * @return 字段列表部分
	 */
	public String getColumnPart() {
		if (ColumnPart == null) {
			generatePartString();
		}
		return ColumnPart;
	}

	/**
	 * @return from部分
	 */
	public String getFromPart() {
		if (FromPart == null) {
			generatePartString();
		}
		return FromPart;
	}

	/**
	 * @return group by子句
	 */
	public String getGroupByPart() {
		if (GroupByPart == null) {
			generatePartString();
		}
		return GroupByPart;
	}

	/**
	 * @return having子句
	 */
	public String getHavingPart() {
		if (HavingPart == null) {
			generatePartString();
		}
		return HavingPart;
	}

	/**
	 * @return order by子句
	 */
	public String getOrderbyPart() {
		if (OrderbyPart == null) {
			generatePartString();
		}
		return OrderbyPart;
	}

	/**
	 * @return where子句
	 */
	public String getWherePart() {
		if (WherePart == null) {
			generatePartString();
		}
		return WherePart;
	}

	/**
	 * @return 字段数组
	 */
	public String[] getColumns() {
		return Columns;
	}

	/**
	 * @return 条件数组
	 */
	public String[] getConditions() {
		return Conditions;
	}

	/**
	 * @return group by字段数组
	 */
	public String[] getGroupByFields() {
		return GroupByFields;
	}

	/**
	 * @return order by字段数组
	 */
	public String[] getOrderByFields() {
		return OrderByFields;
	}

	/**
	 * @return 表数组
	 */
	public String[] getTables() {
		return Tables;
	}

	/**
	 * @return having条件数组
	 */
	public String[] getHavingConditions() {
		return HavingConditions;
	}

	/**
	 * @return where条件逻辑符号
	 */
	public String[] getConditionLogics() {
		return ConditionLogics;
	}

	/**
	 * @return having条件逻辑符号
	 */
	public String[] getHavingConditionLogics() {
		return HavingConditionLogics;
	}

}
