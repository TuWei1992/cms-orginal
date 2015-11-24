package com.zving.framework.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.zving.framework.utility.NumberUtil;
import com.zving.framework.utility.ObjectUtil;

/**
 * 使用QueryBuilder的快捷方式，并封装有一系列的参数化方法，以便更好地防止SQL注入。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-1-6
 */
public class Q extends QueryBuilder {
	public Q() {
		super();
	}

	/**
	 * 根据传入的SQL字符串构造一个SQL查询，参数个数可变.
	 */
	public Q(String sql, Object... args) {
		super(sql, args);
	}

	/**
	 * 在SQL最后面加一个where关键字，此关键字之前会加一个空格.
	 */
	public Q where() {
		sql.append(" where");
		return this;
	}

	/**
	 * 在SQL最后面加一个where关键字，此关键字之前会加一个空格.<br>
	 * 参数field,value用来指定一个等于条件，本方法等同于调用where().eq(field,value).<br>
	 */
	public Q where(String field, Object value) {
		sql.append(" where");
		eq(field, value);
		return this;
	}

	/**
	 * 在SQL最后面加一个and关键字，此关键字之前会加一个空格.
	 */
	public Q and() {
		sql.append(" and");
		return this;
	}

	/**
	 * 在SQL最后面加一个not关键字，此关键字之前会加一个空格.
	 */
	public Q not() {
		sql.append(" not");
		return this;
	}

	/**
	 * 在SQL最后面加一个or关键字，此关键字之前会加一个空格.
	 */
	public Q or() {
		sql.append(" or");
		return this;
	}

	/**
	 * 在SQL最后面加一个in子句，in子句之前会加一个空格.<br>
	 * values为已经拼接好的值列表，值之间用逗号隔开,<br>
	 */
	/**
	 * @param field 字段名称
	 * @param values in条件中的值列表，形如"1,2,3,4,5"
	 */
	public Q in(String field, String values) {
		return in(field, values, true);
	}

	public Q in(String field, Q sub) {
		if (sub == null) {
			return ignoreNull();
		}
		verifyName(field);
		sql.append(" ");
		sql.append(field);
		sql.append(" in ");
		sub(sub);
		return this;
	}

	/**
	 * 在SQL最后面加一个in子句，in子句之前会加一个空格.<br>
	 * values为已经拼接好的值列表，值之间用逗号隔开,<br>
	 */
	/**
	 * @param field 字段名称
	 * @param values in条件中的值列表，形如"1,2,3,4,5"
	 */
	public Q in(String field, String values, boolean ignoreNull) {
		if (ignoreNull && ObjectUtil.empty(values)) {
			return ignoreNull();
		}
		List<Object> list = null;
		if (field.toLowerCase().endsWith("id") && values.matches("^\\d+(\\,\\d+)*$")) {
			list = convertFieldValues(values, Long.class);
		} else {
			list = convertFieldValues(values, String.class);
		}
		in(field, list);
		return this;
	}

	/**
	 * 在SQL最后面加一个in子句，in子句之前有空格.<br>
	 * 
	 * @param field 字段名
	 * @param values 值列表，会作参数化处理
	 * @param type 值要转换成的目标类型
	 */
	public Q in(String field, String values, Class<?> type) {// NO_UCD
		in(field, convertFieldValues(values, type));
		return this;
	}

	/**
	 * 在SQL最后面加一个in子句，in子句之前有空格.<br>
	 * 
	 * @param field 字段名
	 * @param values 值列表，会作参数化处理
	 */
	public Q in(String field, Object... values) {
		return in(field, ObjectUtil.toList(values));
	}

	/**
	 * 在SQL最后面加一个in子句，in子句之前有空格.<br>
	 * 
	 * @param field 字段名
	 * @param values 值列表，会作参数化处理
	 */
	public Q in(String field, String[] values) {
		return in(field, ObjectUtil.toList(values));
	}

	/**
	 * 在SQL最后面加一个in子句，in子句之前有空格.<br>
	 * 
	 * @param field 字段名
	 * @param values 值列表，会作参数化处理
	 */
	public Q in(String field, Collection<?> values) {
		return in(field, values, true);
	}

	/**
	 * 在SQL最后面加一个in子句，in子句之前有空格.<br>
	 * 
	 * @param field 字段名
	 * @param values 值列表，会作参数化处理
	 */
	public Q in(String field, Collection<?> values, boolean ignoreNull) {
		if (ignoreNull && values == null) {
			return ignoreNull();
		}
		verifyName(field);
		sql.append(" ");
		sql.append(field);
		sql.append(" in (");
		boolean first = true;
		boolean isID = false;
		if (field.toLowerCase().endsWith("id")) {
			isID = true;
		}
		if (values != null && values.size() > 0) {
			for (Object obj : values) {
				if (obj == null) {
					continue;
				}
				if (first) {
					first = false;
				} else {
					sql.append(",");
				}
				if (isID && obj instanceof String && NumberUtil.isLong((String) obj)) {
					obj = Long.parseLong((String) obj);
				}
				append("?", obj);
			}
		} else {
			sql.append("null");
		}
		sql.append(")");
		return this;
	}

	/**
	 * 在SQL最后面增加一个等式，等式之前会加一个空格.
	 * 
	 * @param field 字段名
	 * @param value 要比较的值或子查询
	 */
	public Q eq(String field, Object value) {
		verifyName(field);
		sql.append(" ");
		sql.append(field);
		if (field.equals("1") && value instanceof String) {// 对于常用的1=1的兼容
			append("=?", Integer.parseInt(value.toString()));
		} else {
			append("=?", value);
		}
		return this;
	}

	/**
	 * 在SQL最后面增加一个字段等式，等式之前会加一个空格.<br>
	 * 注意：字段等式会将两个参数都视为数据库字段，不会进行参数化.
	 * 
	 * @param field 字段1
	 * @param field2 字段2
	 */
	public Q eq2(String field, String field2) {
		verifyName(field);
		verifyName(field2);
		sql.append(" ");
		sql.append(field);
		sql.append("=");
		sql.append(field2);
		return this;
	}

	/**
	 * 在SQL最后面增加一个不等于条件，条件之前会加一个空格.
	 * 
	 * @param field 字段名
	 * @param value 要比较的值或子查询
	 */
	public Q ne(String field, Object value) {
		verifyName(field);
		sql.append(" ");
		sql.append(field);
		if (field.equals("1") && value instanceof String) {// 对于常用的1=1的兼容
			append("<>?", Integer.parseInt(value.toString()));
		} else {
			append("<>?", value);
		}
		return this;
	}

	/**
	 * 在SQL最后面增加一个大于条件，条件之前会加一个空格.
	 * 
	 * @param field 字段名
	 * @param value 要比较的值
	 */
	public Q gt(String field, Object value) {
		verifyName(field);
		sql.append(" ");
		sql.append(field);
		append(">?", value);
		return this;
	}

	/**
	 * 在SQL最后面增加一个小于条件，条件之前会加一个空格.
	 * 
	 * @param field 字段名
	 * @param value 要比较的值
	 */
	public Q lt(String field, Object value) {
		verifyName(field);
		sql.append(" ");
		sql.append(field);
		append("<?", value);
		return this;
	}

	/**
	 * 在SQL最后面增加一个大于等于条件，条件之前会加一个空格.
	 * 
	 * @param field 字段名
	 * @param value 要比较的值或子查询
	 */
	public Q ge(String field, Object value) {
		verifyName(field);
		sql.append(" ");
		sql.append(field);
		append(">=?", value);
		return this;
	}

	/**
	 * 在SQL最后面增加一个小于等于条件，条件之前会加一个空格.
	 * 
	 * @param field 字段名
	 * @param value 要比较的值或子查询
	 */
	public Q le(String field, Object value) {
		verifyName(field);
		sql.append(" ");
		sql.append(field);
		append("<=?", value);
		return this;
	}

	/**
	 * 在SQL最后面增加一个like条件
	 * 
	 * @param field 字段名
	 * @param value like条件值，不需要带%号，会自动在左右各加一个%
	 */
	public Q like(String field, String value) {
		return like(field, value, true);
	}

	public Q like(String field, String value, boolean ignoreNull) {
		if (ignoreNull && ObjectUtil.empty(value)) {
			return ignoreNull();
		}
		verifyName(field);
		sql.append(" ");
		sql.append(field);
		append(" like ?", "%" + value + "%");
		return this;
	}

	/**
	 * 在SQL最后面增加一个右like条件
	 * 
	 * @param field 字段名
	 * @param value like条件值，不需要带%号，会自动在最右侧加一个%
	 */
	public Q likeRight(String field, String value) {
		return likeRight(field, value, true);
	}

	public Q likeRight(String field, String value, boolean ignoreNull) {
		if (ignoreNull && ObjectUtil.empty(value)) {
			return ignoreNull();
		}
		verifyName(field);
		sql.append(" ");
		sql.append(field);
		append(" like ?", value + "%");
		return this;
	}

	/**
	 * 在SQL最后面增加一个左like条件
	 * 
	 * @param field 字段名
	 * @param value like条件值，不需要带%号，会自动在最左侧加一个%
	 */
	public Q likeLeft(String field, String value) {// NO_UCD
		return likeLeft(field, value, true);
	}

	public Q likeLeft(String field, String value, boolean ignoreNull) {// NO_UCD
		if (ignoreNull && ObjectUtil.empty(value)) {
			return ignoreNull();
		}
		verifyName(field);
		sql.append(" ");
		sql.append(field);
		append(" like ?", "%" + value);
		return this;
	}

	/**
	 * 在SQL最后面增加一个左括号，左括号之前会加一个空格.
	 */
	public Q braceLeft() {
		sql.append(" (");
		return this;
	}

	/**
	 * 在SQL最后面增加一个右括号 .
	 */
	public Q braceRight() {
		sql.append(")");
		return this;
	}

	/**
	 * 添加子查询，子查询会被括号包裹起来，并大左括号之前添加一个空格。
	 */
	public Q sub(Q q) {
		sql.append(" (");
		addPart(q);
		sql.append(")");
		return this;
	}

	/**
	 * 在SQL最后面增加一个order by子句，子句之前会加一个空格.<br>
	 * 
	 * @param orderby 排序字段及排序方向，可以有多个排序字段，以逗号隔开
	 */
	public Q orderby(String orderby) {
		int last = 0;
		boolean nameFlag = true;// 表明当前处于一个字段名之类
		for (int i = 0; i < orderby.length(); i++) {
			char c = orderby.charAt(i);
			if (c == ' ') {
				if (!nameFlag) {
					throw new QueryException("Maybe a SQL Injection: orderby=" + orderby);
				}
				verifyName(orderby.substring(last, i));
				last = i + 1;
				nameFlag = false;
			} else if (c == ',') {
				String str = orderby.substring(last, i).trim();
				if (nameFlag) {
					verifyName(str);
				} else {
					if (str.equals("") || str.equalsIgnoreCase("asc") || str.equalsIgnoreCase("desc")) {
						last = i + 1;
					} else {
						throw new QueryException("Maybe a SQL Injection: orderby=" + orderby);
					}
				}
				while (i + 1 < orderby.length() && orderby.charAt(i + 1) == ' ') {
					last = ++i;
				}
				nameFlag = true;
			}
		}
		if (nameFlag) {
			verifyName(orderby.substring(last));
		} else {
			String order = orderby.substring(last);
			if (!(order.equals("") || order.equalsIgnoreCase("asc") || order.equalsIgnoreCase("desc"))) {
				throw new QueryException("Maybe a SQL Injection: orderby=" + orderby);
			}
		}
		sql.append(" order by " + orderby);
		return this;
	}

	/**
	 * 在SQL最后面增加一个group by子句，子句之前会加一个空格.<br>
	 * 
	 * @param field group by 后面跟承受的字段名，字段名后面可以有“ desc”和“ asc”
	 */
	public Q groupby(String field) {
		verifyName(field);
		sql.append(" group by " + field);
		return this;
	}

	/**
	 * 在SQL最后面加一个and关键字，此关键字之前会加一个空格.
	 */
	public Q having() {// NO_UCD
		sql.append(" having");
		return this;
	}

	/**
	 * 在SQL最后面加一个as关键字，此关键字之前会加一个空格.
	 */
	public Q as(String name) {
		verifyName(name);
		sql.append(" as ").append(name);
		return this;
	}

	/**
	 * 在SQL最后加一个逗号
	 */
	public Q comma() {
		sql.append(',');
		return this;
	}

	/**
	 * 在SQL最后加一个空格
	 */
	public Q space() {
		sql.append(' ');
		return this;
	}

	/**
	 * 添加一个count函数
	 */
	public Q count(String field) {
		verifyName(field);
		sql.append("count(");
		sql.append(field);
		sql.append(')');
		return this;
	}

	/**
	 * 添加一个sum函数
	 */
	public Q sum(String field) {
		verifyName(field);
		sql.append("sum(");
		sql.append(field);
		sql.append(')');
		return this;
	}

	/**
	 * 添加一个avg函数
	 */
	public Q avg(String field) {
		verifyName(field);
		sql.append("avg(");
		sql.append(field);
		sql.append(')');
		return this;
	}

	/**
	 * 在SQL最后面加一个exists关键字，此关键字之前会加一个空格.
	 */
	public Q exists() {
		sql.append(" exists");
		return this;
	}

	/**
	 * 在SQL最后面加一个exists子句，exists关键字之前会加一个空格.
	 */
	public Q exists(Q sub) {
		sql.append(" exists");
		sub(sub);
		return this;
	}

	/**
	 * 加一个Select子句,columns为可变参数，用来指定字段列表.
	 */
	public Q select(String... columns) {
		sql.append("select");
		boolean first = true;
		for (String column : columns) {
			if (first) {
				first = false;
				sql.append(" ");
			} else {
				sql.append(",");
			}
			verifyName(column);
			sql.append(column);
		}
		return this;
	}

	/**
	 * 加一个Select子句
	 * 
	 * @param columns 字段列表
	 */
	public Q select(Collection<String> columns) {
		sql.append("select");
		boolean first = true;
		for (String column : columns) {
			if (first) {
				first = false;
				sql.append(" ");
			} else {
				sql.append(",");
			}
			verifyName(column);
			sql.append(column);
		}
		return this;
	}

	/**
	 * 在SQL最后面加一个from关键字，此关键词之前会加一个空格.
	 * 
	 * @param tables from后面跟随的一个或多个表名
	 */
	public Q from(String... tables) {
		sql.append(" from");
		boolean first = true;
		for (String table : tables) {
			verifyName(table);
			if (first) {
				sql.append(" ");
				first = false;
			} else {
				sql.append(",");
			}
			sql.append(table);
		}
		return this;
	}

	/**
	 * 忽略空条件。<br>
	 * 某些方法允许使用ignoreEmpty变量，用来在条件中的变量值为null时忽略这个条件，这时需要调用本方法以清空掉前置的"and"。
	 */
	private Q ignoreNull() {
		String str = sql.toString().trim().toLowerCase();
		if (str.endsWith(" and")) {
			sql.delete(sql.length() - 4, sql.length());
		} else if (str.endsWith(" having")) {
			sql.delete(sql.length() - 5, sql.length());
		} else if (str.endsWith(" where")) {
			sql.delete(sql.length() - 6, sql.length());
		} else {
			throw new QueryException("Invalid SQL:" + sql);
		}
		return this;
	}

	/**
	 * 将in的字符串形式的值列表转为List
	 * 
	 * @param values 字符串形式的值列表，形如"1,2,3,4,5"
	 * @param type 要转换的对象类型
	 * @return 转换后的List
	 */
	private List<Object> convertFieldValues(String values, Class<?> type) {
		if (values == null) {
			return null;
		}
		boolean literal = false;
		List<Object> list = new ArrayList<Object>();
		int last = 0;
		for (int i = 0; i < values.length(); i++) {
			char c = values.charAt(i);
			if (c == ',' && !literal) {
				String str = values.substring(last, i).trim();
				if (str.startsWith("\'")) {
					str = str.substring(1, str.length() - 1);
				}
				if (type == Integer.class) {
					list.add(Integer.parseInt(str));
				} else if (type == Long.class) {
					list.add(Long.parseLong(str));
				} else if (type == Double.class) {
					list.add(Double.parseDouble(str));
				} else if (type == Float.class) {
					list.add(Float.parseFloat(str));
				} else {
					list.add(str);
				}
				last = i + 1;
				continue;
			}
			if (c == '\'') {
				literal = !literal;
				continue;
			}
		}
		if (last != values.length()) {
			String str = values.substring(last).trim();
			if (str.startsWith("\'")) {
				str = str.substring(1, str.length() - 1);
				list.add(str);
			} else {
				if (type == Integer.class) {
					list.add(Integer.parseInt(str));
				} else if (type == Long.class) {
					list.add(Long.parseLong(str));
				} else if (type == Double.class) {
					list.add(Double.parseDouble(str));
				} else if (type == Float.class) {
					list.add(Float.parseFloat(str));
				} else {
					list.add(str);
				}
			}
		}
		return list;
	}

	/**
	 * 加一个delete关键字.
	 */
	public Q delete() {
		sql.append("delete");
		return this;
	}

	/**
	 * 加一个update关键字.
	 * 
	 * @param table update后跟随的数据表名
	 */
	public Q update(String table) {
		sql.append("update ");
		verifyName(table);
		sql.append(table);
		return this;
	}

	/**
	 * 加一个set关键字，此关键字前后会加一个空格.
	 */
	public Q set() {
		sql.append(" set ");
		return this;
	}

	/**
	 * 加一个set及赋值等式，如果前面已经有赋值等式了，则改为追加逗号及赋值等式.
	 * 
	 * @param field 字段名
	 * @param value 要比较的值或子查询
	 */
	public Q set(String field, Object value) {
		if (sql.toString().toLowerCase().indexOf(" set ") > 0) {// 前面己有赋值语句
			sql.append(",");
		} else {
			sql.append(" set ");
		}
		verifyName(field);
		sql.append(field);
		append("=?", value);
		return this;
	}

	/**
	 * 检查SQL语句放置字段名的位置是否有可能是SQL注入.<br>
	 * 允许在字段名上加函数，以及使用字段别名.
	 * 
	 * @param field 待检查的字段名
	 */
	private void verifyName(String field) {
		int i1 = field.indexOf('(');// 如果有括号,则不允许有空格
		if (i1 > 0) {
			int i2 = field.indexOf(' ', i1);
			if (i2 > 0) {
				throw new QueryException("Maybe a SQL Injection: name=" + field);
			}
		} else {
			if (field.indexOf(' ') > 0) {
				String[] arr = field.split("\\s");
				if (arr.length > 3) {
					throw new QueryException("Maybe a SQL Injection: name=" + field);
				} else if (arr.length == 3 && !arr[1].equalsIgnoreCase("as")) {// 如果有两个空格，则必须是as形式的别名
					throw new QueryException("Maybe a SQL Injection: name=" + field);
				}// 如果只有一个空格，则会因不符合SQL语法执行出错，从而不需要检查
			}
		}
	}

	/**
	 * 追加部分SQL语句，同时追加SQL参数。<br>
	 * 注意：如果只有一个参数并且参数值类型为Q，则会加入一个子查询
	 */
	@Override
	public Q append(String sqlPart, Object... params) {
		if (params != null && params.length == 1 && params[0] != null && params[0] instanceof Q) {// 子查询
			if (sql.charAt(sql.length() - 1) == '?') {
				sql.deleteCharAt(sql.length() - 1);// 最后一个是占位符则删除掉
			}
			sql.append(sqlPart);
			sub((Q) params[0]);
			return this;
		}
		super.append(sqlPart, params);
		return this;
	}

	/**
	 * 加一个wherePart
	 */
	public Q addPart(Q wherePart) {
		if (wherePart == null) {
			return this;
		}
		sql.append(wherePart.getSQL());
		getParams().addAll(wherePart.getParams());
		return this;
	}

	/**
	 * 加入一个between部分，between之前会加一个空格.
	 * 
	 * @param field 字段名称
	 * @param start 起始值
	 * @param end 终止值
	 */
	public Q between(String field, Object start, Object end) {
		sql.append(" ");
		verifyName(field);
		sql.append(field);
		append(" between ? and ?", start, end);
		return this;
	}

	/**
	 * 加入一个形如count=count+1、count=count-1、count=count/10的字段自操作表达式,表达式之前会加一个空格.
	 * 
	 * @param field 字段名
	 * @param operator 操作
	 * @param value 值
	 */
	public Q self(String field, String operator, Object value) {
		if (operator.indexOf(' ') > 0) {
			throw new QueryException("Maybe a SQL Injection: operator=" + operator);
		}
		sql.append(" ");
		verifyName(field);
		sql.append(field);
		sql.append("=");
		sql.append(field);
		sql.append(operator);
		sql.append("?");
		getParams().add(value);
		return this;
	}

	/**
	 * 增加一个insert into语句
	 * 
	 * @param table 数据表
	 * @param columns 字段列表
	 * @param values 值列表
	 * @return
	 */
	public Q insertInto(String table, String[] columns, Object[] values) {// NO_UCD
		if (columns.length == 0 || columns.length != values.length) {
			throw new QueryException("Insert into failed:values's length is not equal columns.length!");
		}
		sql.append("insert into ");
		sql.append(table);
		sql.append(" (");
		for (int i = 0; i < columns.length; i++) {
			if (i != 0) {
				sql.append(",");
			}
			sql.append(columns[i]);
		}
		sql.append(") values (");
		for (int i = 0; i < columns.length; i++) {
			if (i != 0) {
				sql.append(",");
			}
			append("?", values[i]);
		}
		sql.append(")");
		return this;
	}

	/**
	 * 增加一个insert into语句
	 * 
	 * @param table 数据表
	 * @param columns 字段列表
	 * @param values 值列表
	 * @return
	 */
	public Q insertInto(String table, Collection<String> columns, Collection<?> values) {
		if (columns.size() == 0 || columns.size() != values.size()) {
			throw new QueryException("Insert into failed:values's length is not equal columns.length!");
		}
		sql.append("insert into ");
		sql.append(table);
		sql.append(" (");
		boolean first = true;
		for (String column : columns) {
			if (!first) {
				sql.append(",");
			} else {
				first = false;
			}
			append(column);
		}
		sql.append(") values (");
		first = true;
		for (Object value : values) {
			if (!first) {
				sql.append(",");
			} else {
				first = false;
			}
			append("?", value);
		}
		sql.append(")");
		return this;
	}
}
