package com.zving.framework.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * 日期工具类
 * 
 * @author 王育春<br>
 * @date 2006-7-6<br>
 * @email wyuch@zving.com<br>
 */
public class DateUtil {
	/**
	 * 全局默认日期格式
	 */
	public static final String Format_Date = "yyyy-MM-dd";

	/**
	 * 全局默认时间格式
	 */
	public static final String Format_Time = "HH:mm:ss";

	/**
	 * 最后更新时间，通常用于http请求返回的header信息
	 */
	public static final String Format_LastModified = "EEE, dd MMM yyyy HH:mm:ss";

	/**
	 * 全局默认日期时间格式
	 */
	public static final String Format_DateTime = "yyyy-MM-dd HH:mm:ss";

	private static ThreadLocal<Formats> formats = new ThreadLocal<Formats>();

	private static class Formats {
		private SimpleDateFormat DateOnly;
		private SimpleDateFormat TimeOnly;
		private SimpleDateFormat DateTime;
		private SimpleDateFormat LastModified;
		private HashMap<String, SimpleDateFormat> Others;
	}

	public static SimpleDateFormat getFormat(String format) {
		if (formats.get() == null) {
			formats.set(new Formats());
		}
		if (format.equals(Format_Date)) {
			return getDefaultDateFormat();
		}
		if (format.equals(Format_Time)) {
			return getDefaultTimeFormat();
		}
		if (format.equals(Format_DateTime)) {
			return getDefaultDateTimeFormat();
		}
		if (format.equals(Format_LastModified)) {
			return getLastModifiedFormat();
		}
		if (formats.get().Others == null) {
			formats.get().Others = new HashMap<String, SimpleDateFormat>();
		}
		if (!formats.get().Others.containsKey(format)) {
			formats.get().Others.put(format, new SimpleDateFormat(format, Locale.ENGLISH));
		}
		return formats.get().Others.get(format);
	}

	public static SimpleDateFormat getDefaultDateTimeFormat() {
		if (formats.get() == null) {
			formats.set(new Formats());
		}
		if (formats.get().DateTime == null) {
			formats.get().DateTime = new SimpleDateFormat(Format_DateTime, Locale.ENGLISH);
		}
		return formats.get().DateTime;
	}

	public static SimpleDateFormat getDefaultDateFormat() {
		if (formats.get() == null) {
			formats.set(new Formats());
		}
		if (formats.get().DateOnly == null) {
			formats.get().DateOnly = new SimpleDateFormat(Format_Date, Locale.ENGLISH);
		}
		return formats.get().DateOnly;
	}

	public static SimpleDateFormat getDefaultTimeFormat() {
		if (formats.get() == null) {
			formats.set(new Formats());
		}
		if (formats.get().TimeOnly == null) {
			formats.get().TimeOnly = new SimpleDateFormat(Format_Time, Locale.ENGLISH);
		}
		return formats.get().TimeOnly;
	}

	public static SimpleDateFormat getLastModifiedFormat() {
		if (formats.get() == null) {
			formats.set(new Formats());
		}
		if (formats.get().LastModified == null) {
			formats.get().LastModified = new SimpleDateFormat(Format_LastModified, Locale.ENGLISH);
		}
		return formats.get().LastModified;
	}

	/**
	 * 得到以yyyy-MM-dd格式表示的当前日期字符串
	 */
	public static String getCurrentDate() {
		return getDefaultDateFormat().format(new Date());
	}

	/**
	 * 得到以format格式表示的当前日期字符串
	 */
	public static String getCurrentDate(String format) {
		return getFormat(format).format(new Date());
	}

	/**
	 * 得到以yyyy-MM-dd HH:mm:ss表示的当前时间字符串
	 */
	public static String getCurrentDateTime() {
		return getDefaultDateTimeFormat().format(new Date());
	}

	/**
	 * 指定日期是星期几
	 * 
	 * @param date
	 * @return
	 */
	public static int getDayOfWeek(Date date) {// NO_UCD
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * 指定日期是当月的第几天
	 * 
	 * @param date
	 * @return
	 */
	public static int getDayOfMonth(Date date) {// NO_UCD
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 获取某一个月的天数
	 * 
	 * @param date
	 * @return
	 */
	public static int getMaxDayOfMonth(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.getActualMaximum(Calendar.DATE);
	}

	/**
	 * 指定日期是当年的第几天
	 * 
	 * @param date
	 * @return
	 */
	public static int getDayOfYear(Date date) {// NO_UCD
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_YEAR);
	}

	/**
	 * 以指定的格式返回当前日期时间的字符串
	 * 
	 * @param format
	 * @return
	 */
	public static String getCurrentDateTime(String format) {
		return getFormat(format).format(new Date());
	}

	/**
	 * 以yyyy-MM-dd格式输出只带日期的字符串
	 */
	public static String toString(Date date) {
		if (date == null) {
			return "";
		}
		return getDefaultDateFormat().format(date);
	}

	/**
	 * 以yyyy-MM-dd HH:mm:ss输出带有日期和时间的字符串
	 */
	public static String toDateTimeString(Date date) {
		if (date == null) {
			return "";
		}
		return getDefaultDateTimeFormat().format(date);
	}

	/**
	 * 按指定的format输出日期字符串
	 */
	public static String toString(Date date, String format) {
		if (date == null) {
			return "";
		}
		return getFormat(format).format(date);
	}

	/**
	 * 以yyyy-MM-dd解析两个字符串，并比较得到的两个日期的大小
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int compare(String date1, String date2) {
		return compare(date1, date2, Format_Date);
	}

	/**
	 * 以指定格式解析两个字符串，并比较得到的两个日期的大小
	 * 
	 * @param date1
	 * @param date2
	 * @param format
	 * @return
	 */
	public static int compare(String date1, String date2, String format) {
		Date d1 = parse(date1, format);
		Date d2 = parse(date2, format);
		return d1.compareTo(d2);
	}

	/**
	 * 判断指定的字符串是否符合HH:mm:ss格式，并判断其数值是否在正常范围
	 * 
	 * @param time
	 * @return
	 */
	public static boolean isTime(String time) {
		String[] arr = time.split(":");
		if (arr.length < 2) {
			return false;
		}
		try {
			int h = Integer.parseInt(arr[0]);
			int m = Integer.parseInt(arr[1]);
			int s = 0;
			if (arr.length == 3) {
				s = Integer.parseInt(arr[2]);
			}
			if (h < 0 || h > 23 || m < 0 || m > 59 || s < 0 || s > 59) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 判断指定的字符串是否符合yyyy:MM:ss格式，但判断其数据值范围是否正常
	 * 
	 * @param date
	 * @return
	 */
	public static boolean isDate(String date) {
		String[] arr = date.split("-");
		if (arr.length < 3) {
			return false;
		}
		try {
			int y = Integer.parseInt(arr[0]);
			int m = Integer.parseInt(arr[1]);
			int d = Integer.parseInt(arr[2]);
			if (y < 0 || m > 12 || m < 0 || d < 0 || d > 31) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 判断是否是日期或者带时间的日期，日期必须符合格式yy-MM-dd或yy-MM-dd HH:mm:ss
	 */
	public static boolean isDateTime(String str) {
		if (StringUtil.isEmpty(str)) {
			return false;
		}
		if (str.endsWith(".0")) {
			str = str.substring(0, str.length() - 2);
		}
		if (str.indexOf(" ") > 0) {
			String[] arr = str.split(" ");
			if (arr.length == 2) {
				return isDate(arr[0]) && isTime(arr[1]);
			} else {
				return false;
			}
		} else {
			return isDate(str);
		}
	}

	/**
	 * 判断指定日期是否是周末
	 * 
	 * @param date
	 * @return
	 */
	public static boolean isWeekend(Date date) {// NO_UCD
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int t = cal.get(Calendar.DAY_OF_WEEK);
		if (t == Calendar.SATURDAY || t == Calendar.SUNDAY) {
			return true;
		}
		return false;
	}

	/**
	 * 以yyyy-MM-dd解析指定字符串，返回相应java.util.Date对象
	 * 
	 * @param str
	 * @return
	 */
	public static Date parse(String str) {
		if (StringUtil.isEmpty(str)) {
			return null;
		}
		try {
			return getDefaultDateFormat().parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 按指定格式解析字符串，并返回相应的java.util.Date对象
	 * 
	 * @param str
	 * @param format
	 * @return
	 */
	public static Date parse(String str, String format) {
		if (StringUtil.isEmpty(str)) {
			return null;
		}
		try {
			SimpleDateFormat t = new SimpleDateFormat(format);
			return t.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 解析http请求中的最后更新时间
	 * 
	 * @param str
	 * @return
	 */
	public static Date parseLastModified(String str) {
		if (StringUtil.isEmpty(str)) {
			return null;
		}
		try {
			return getLastModifiedFormat().parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 以yyyy-MM-dd HH:mm:ss格式解析字符串，并返回相应的java.util.Date对象
	 * 
	 * @param str
	 * @return
	 */
	public static Date parseDateTime(String str) {
		if (StringUtil.isEmpty(str)) {
			return null;
		}
		if (str.length() <= 10) {
			return parse(str);
		}
		try {
			return getDefaultDateTimeFormat().parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 以指定格式解析字符串，并返回相应的java.util.Date对象
	 * 
	 * @param str
	 * @param format
	 * @return
	 */
	public static Date parseDateTime(String str, String format) {
		if (StringUtil.isEmpty(str)) {
			return null;
		}
		try {
			SimpleDateFormat t = new SimpleDateFormat(format);
			return t.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 日期date上加count秒钟，count为负表示减
	 */
	public static Date addSecond(Date date, int count) {
		return new Date(date.getTime() + 1000L * count);
	}

	/**
	 * 日期date上加count分钟，count为负表示减
	 */
	public static Date addMinute(Date date, int count) {// NO_UCD
		return new Date(date.getTime() + 60000L * count);
	}

	/**
	 * 日期date上加count小时，count为负表示减
	 */
	public static Date addHour(Date date, int count) {
		return new Date(date.getTime() + 3600000L * count);
	}

	/**
	 * 日期date上加count天，count为负表示减
	 */
	public static Date addDay(Date date, int count) {
		return new Date(date.getTime() + 86400000L * count);
	}

	/**
	 * 日期date上加count星期，count为负表示减
	 */
	public static Date addWeek(Date date, int count) {// NO_UCD
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.WEEK_OF_YEAR, count);
		return c.getTime();
	}

	/**
	 * 日期date上加count月，count为负表示减
	 */
	public static Date addMonth(Date date, int count) {
		/* ${_ZVING_LICENSE_CODE_} */

		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MONTH, count);
		return c.getTime();
	}

	/**
	 * 日期date上加count年，count为负表示减
	 */
	public static Date addYear(Date date, int count) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.YEAR, count);
		return c.getTime();
	}

	/**
	 * 将日期中的中文数字转化成阿拉伯数字，以例于使用指定格式解析
	 */
	public static String convertChineseNumber(String strDate) {
		strDate = StringUtil.replaceEx(strDate, "一十一", "11");
		strDate = StringUtil.replaceEx(strDate, "一十二", "12");
		strDate = StringUtil.replaceEx(strDate, "一十三", "13");
		strDate = StringUtil.replaceEx(strDate, "一十四", "14");
		strDate = StringUtil.replaceEx(strDate, "一十五", "15");
		strDate = StringUtil.replaceEx(strDate, "一十六", "16");
		strDate = StringUtil.replaceEx(strDate, "一十七", "17");
		strDate = StringUtil.replaceEx(strDate, "一十八", "18");
		strDate = StringUtil.replaceEx(strDate, "一十九", "19");
		strDate = StringUtil.replaceEx(strDate, "二十一", "21");
		strDate = StringUtil.replaceEx(strDate, "二十二", "22");
		strDate = StringUtil.replaceEx(strDate, "二十三", "23");
		strDate = StringUtil.replaceEx(strDate, "二十四", "24");
		strDate = StringUtil.replaceEx(strDate, "二十五", "25");
		strDate = StringUtil.replaceEx(strDate, "二十六", "26");
		strDate = StringUtil.replaceEx(strDate, "二十七", "27");
		strDate = StringUtil.replaceEx(strDate, "二十八", "28");
		strDate = StringUtil.replaceEx(strDate, "二十九", "29");
		strDate = StringUtil.replaceEx(strDate, "十一", "11");
		strDate = StringUtil.replaceEx(strDate, "十二", "12");
		strDate = StringUtil.replaceEx(strDate, "十三", "13");
		strDate = StringUtil.replaceEx(strDate, "十四", "14");
		strDate = StringUtil.replaceEx(strDate, "十五", "15");
		strDate = StringUtil.replaceEx(strDate, "十六", "16");
		strDate = StringUtil.replaceEx(strDate, "十七", "17");
		strDate = StringUtil.replaceEx(strDate, "十八", "18");
		strDate = StringUtil.replaceEx(strDate, "十九", "19");
		strDate = StringUtil.replaceEx(strDate, "十", "10");
		strDate = StringUtil.replaceEx(strDate, "二十", "20");
		strDate = StringUtil.replaceEx(strDate, "三十", "20");
		strDate = StringUtil.replaceEx(strDate, "三十一", "31");
		strDate = StringUtil.replaceEx(strDate, "零", "0");
		strDate = StringUtil.replaceEx(strDate, "○", "0");
		strDate = StringUtil.replaceEx(strDate, "一", "1");
		strDate = StringUtil.replaceEx(strDate, "二", "2");
		strDate = StringUtil.replaceEx(strDate, "三", "3");
		strDate = StringUtil.replaceEx(strDate, "四", "4");
		strDate = StringUtil.replaceEx(strDate, "五", "5");
		strDate = StringUtil.replaceEx(strDate, "六", "6");
		strDate = StringUtil.replaceEx(strDate, "七", "7");
		strDate = StringUtil.replaceEx(strDate, "八", "8");
		strDate = StringUtil.replaceEx(strDate, "九", "9");
		return strDate;
	}
}
