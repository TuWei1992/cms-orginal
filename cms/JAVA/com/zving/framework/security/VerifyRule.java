package com.zving.framework.security;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zving.framework.utility.DateUtil;

/**
 * 数据检验规则类
 * 
 * @Author 王育春
 * @Date 2006-10-2
 * @Mail wyuch@m165.com
 */
public class VerifyRule {
	private static final String regEmail = "^(\\w)+(\\.\\w+)*@(\\w)+((\\.\\w+)+)$";

	/**
	 * 字符串格式要求：字符串中可以是任何字符，但不能包括有潜在危险性的SQL语句和Script语句
	 */
	public static final String F_String = "String";

	/**
	 * 字符串格式要求：字符串中可以是任何字符，包括有潜在危险性的SQL语句和Script语句
	 */
	public static final String F_Any = "Any";

	/**
	 * 字符串格式要求：字符串必须代表一个数值，可以是整数或者浮点，可以是负数，或者是科学记数
	 */
	public static final String F_Number = "Number";

	/**
	 * 字符串格式要求：字符串必须代表一个有效日期，格式为指定格式，默认为YYYY-MM-DD
	 */
	public static final String F_Date = "Date";

	/**
	 * 字符串格式要求：字符串必须代表一个有效时间,格式为HH:MM:SS
	 */
	public static final String F_Time = "Time";

	/**
	 * 字符串格式要求：字符串必须代表一个有效日期时间,格式为指定格式，默认为YYYY-MM-DD HH:MM:SS
	 */
	public static final String F_DateTime = "DateTime";

	/**
	 * 字符串格式要求：字符串必须代表一个整数，可以为负数
	 */
	public static final String F_Int = "Int";

	/**
	 * 字符串格式要求：字符串不能为空
	 */
	public static final String F_NotNull = "NotNull";

	/**
	 * 字符串格式要求：必须是合法的电子邮箱
	 */
	public static final String F_Email = "Email";

	/**
	 * 字符串格式要求：字符串值应该是CodeSelect中某一code的范围之内
	 */
	public static final String F_Code = "Code";

	/**
	 * 属性：字符串长度
	 */
	public static final String A_Len = "Length";

	private static Pattern patternEmail = null;

	private String Rule;

	private String[] Features;

	private ArrayList<String> Messages;

	/**
	 * 创建一个空白校验规则对象
	 */
	public VerifyRule() {
	}

	/**
	 * 创建含有指定规则的校验规则对象
	 * 
	 * @param rule
	 */
	public VerifyRule(String rule) {
		Rule = rule;
	}

	/**
	 * 校验指定的值是否符合本对象的校验规则
	 * 
	 * @param value
	 * @return
	 */
	public boolean verify(String value) {
		Messages = new ArrayList<String>();
		Features = Rule.split("\\&\\&");
		boolean sqlFlag = true;
		boolean verifyFlag = true;
		try {
			for (String feature : Features) {
				String op = "=";
				if (feature.indexOf('>') > 0) {
					op = ">";
				} else if (feature.indexOf('<') > 0) {
					op = "<";
				}
				String[] f = feature.split("\\" + op);
				String fName = f[0];
				String fValue = null;
				if (f.length > 1) {
					fValue = f[1];
				}
				if (fName.equals(VerifyRule.F_Any)) {
					sqlFlag = false;
				} else if (fName.equals(VerifyRule.F_NotNull)) {
					if (value == null || value.equals("")) {
						Messages.add("Can't be empty");
						return false;
					}
				} else if (fName.equals(VerifyRule.F_Code)) {
					if (value == null || value.equals("")) {
						continue;
					}
				} else if (fName.equals(VerifyRule.F_Date)) {
					if (value == null || value.equals("")) {
						continue;
					}
					if (!DateUtil.isDate(value)) {
						Messages.add("Invalid date");
						verifyFlag = false;
					}
				} else if (fName.equals(VerifyRule.F_Time)) {
					if (value == null || value.equals("")) {
						continue;
					}
					if (!DateUtil.isTime(value)) {
						Messages.add("Invalid time");
						verifyFlag = false;
					}
				} else if (fName.equals(VerifyRule.F_DateTime)) {
					if (value == null || value.equals("")) {
						continue;
					}
					String[] arr = value.split(" ");
					if (arr.length == 1 && !DateUtil.isDate(arr[0])) {
						Messages.add("Invalid date");
						verifyFlag = false;
					} else if (arr.length == 2) {
						if (!DateUtil.isDate(arr[0]) || !DateUtil.isTime(arr[1])) {
							Messages.add("Invalid datetime");
							verifyFlag = false;
						}
					} else {
						Messages.add("Invalid datetime");
						verifyFlag = false;
					}
				} else if (fName.equals(VerifyRule.F_Number)) {
					if (value == null || value.equals("")) {
						continue;
					}
					try {
						Double.parseDouble(value);
					} catch (Exception e) {
						Messages.add("Invalid number");
						verifyFlag = false;
					}
				} else if (fName.equals(VerifyRule.F_Int)) {
					if (value == null || value.equals("")) {
						continue;
					}
					try {
						Integer.parseInt(value);
					} catch (Exception e) {
						Messages.add("Invalid integer");
						verifyFlag = false;
					}
				} else if (fName.equals(VerifyRule.F_String)) {
					if (value == null || value.equals("")) {
						continue;
					}
					if (value.indexOf('\'') >= 0 || value.indexOf('\"') >= 0) {
						Messages.add("Illegal string");
						verifyFlag = false;
					}
				} else if (fName.equals(VerifyRule.F_Email)) {
					if (value == null || value.equals("")) {
						continue;
					}
					if (patternEmail == null) {
						patternEmail = Pattern.compile(regEmail);
					}
					Matcher m = patternEmail.matcher(value);
					if (!m.find()) {
						Messages.add("Invalid email address");
						verifyFlag = false;
					}
				} else if (fName.equals(VerifyRule.A_Len)) {
					if (value == null || value.equals("")) {
						continue;
					}
					if (fValue == null || fValue.equals("")) {
						throw new RuntimeException("Length must not be empty");
					} else {
						try {
							int len = Integer.parseInt(fValue);
							if (op.equals("=") && value.length() != len) {
								Messages.add("Length must be " + len);
								verifyFlag = false;
							} else if (op.equals(">") && value.length() <= len) {
								Messages.add("Length must greater than" + len);
								verifyFlag = false;
							} else if (op.equals("<") && value.length() >= len) {
								Messages.add("Length must less than" + len);
								verifyFlag = false;
							}
						} catch (Exception e) {
							e.printStackTrace();
							throw new RuntimeException("Length must be integer");
						}
					}
				}
			}
			if (sqlFlag) {// 校验SQL入侵
				if (value != null) {
					if ((value.indexOf(" and ") > 0 || value.indexOf(" or ") > 0)
							&& (value.indexOf('!') > 0 || value.indexOf(" like ") > 0 || value.indexOf('=') > 0 || value.indexOf('>') > 0 || value
									.indexOf('<') > 0)) {
						Messages.add("Illegal string ,maybe is SQL Inject");
						verifyFlag = false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Invalid verify rule:" + Rule);
		}
		return verifyFlag;
	}
}
