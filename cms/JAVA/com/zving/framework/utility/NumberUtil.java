package com.zving.framework.utility;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * 数字工具类
 * 
 * @Author 王育春
 * @Date 2006-12-20
 * @Mail wyuch@midding.com
 */
public class NumberUtil {// NO_UCD
	private static Pattern numberPatter = Pattern.compile("^[\\d\\.E\\,\\+\\-]*$");
	private static final int DEFAULT_SCALE = 10;// 默认精度
	private static final RoundingMode DEFAULT_ROUNDINGMODE = RoundingMode.HALF_UP;// 浮点取舍-默认四舍五入

	/**
	 * 是否是数字
	 */
	public static boolean isNumber(String str) {
		if (StringUtil.isEmpty(str)) {
			return false;
		}
		return numberPatter.matcher(str).find();
	}

	/**
	 * 是否是整形数据
	 */
	public static boolean isInt(String str) {
		if (StringUtil.isEmpty(str)) {
			return false;
		}
		try {
			Integer.parseInt(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	/**
	 * 是否是整形数据
	 */
	public static boolean isInteger(String str) {
		return isInt(str);
	}

	/**
	 * 是否是长整形数据
	 */
	public static boolean isLong(String str) {
		if (StringUtil.isEmpty(str)) {
			return false;
		}
		try {
			Long.parseLong(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	/**
	 * 是否是浮点数
	 */
	public static boolean isFloat(String str) {
		if (StringUtil.isEmpty(str)) {
			return false;
		}
		try {
			Float.parseFloat(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	/**
	 * 是否是双字节浮点数
	 */
	public static boolean isDouble(String str) {
		if (StringUtil.isEmpty(str)) {
			return false;
		}
		try {
			Double.parseDouble(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	// static BigDecimal one = new BigDecimal(1);

	/**
	 * 四舍五入
	 */
	public static double round(double v, int scale) {
		BigDecimal b = new BigDecimal(Double.toString(v));
		return b.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	private static Random rand = new Random();

	/**
	 * 在0-max范围内获取随机整数
	 */
	public static int getRandomInt(int max) {
		return rand.nextInt(max);
	}

	/**
	 * 二进制转为整形
	 */
	public static int toInt(byte[] bs) {
		return toInt(bs, 0);
	}

	/**
	 * 从指定位置开始读取4位二进制，转换为整形
	 */
	public static int toInt(byte[] bs, int start) {
		int i = 0;
		i += (bs[start] & 255) << 24;
		i += (bs[start + 1] & 255) << 16;
		i += (bs[start + 2] & 255) << 8;
		i += bs[start + 3] & 255;
		return i;
	}

	/**
	 * 整形转为二进制
	 */
	public static byte[] toBytes(int i) {
		byte[] bs = new byte[4];
		bs[0] = (byte) (i >> 24);
		bs[1] = (byte) (i >> 16);
		bs[2] = (byte) (i >> 8);
		bs[3] = (byte) (i & 255);
		return bs;
	}

	/**
	 * 整形转为4位二进制数，写入到指定数组的指定位置
	 */
	public static void toBytes(int i, byte[] bs, int start) {
		bs[start] = (byte) (i >> 24);
		bs[start + 1] = (byte) (i >> 16);
		bs[start + 2] = (byte) (i >> 8);
		bs[start + 3] = (byte) (i & 255);
	}

	/**
	 * 读取2位二进制，转为短整形
	 */
	public static short toShort(byte[] bs) {
		return toShort(bs, 0);
	}

	/**
	 * 从指定数组的指定位置开始，读取2位二进制，转为短整形
	 */
	public static short toShort(byte[] bs, int start) {
		short i = 0;
		i += (bs[start + 0] & 255) << 8;
		i += bs[start + 1] & 255;
		return i;
	}

	/**
	 * 短整形转为二进制
	 */
	public static byte[] toBytes(short i) {
		byte[] bs = new byte[2];
		bs[0] = (byte) (i >> 8);
		bs[1] = (byte) (i & 255);
		return bs;
	}

	/**
	 * 短整形转为2位二进制数，写入到指定数组的指定位置
	 */
	public static void toBytes(short i, byte[] bs, int start) {
		bs[start + 0] = (byte) (i >> 8);
		bs[start + 1] = (byte) (i & 255);
	}

	/**
	 * 长整形转为8位二进制
	 */
	public static byte[] toBytes(long i) {
		byte[] bs = new byte[8];
		bs[0] = (byte) (i >> 56);
		bs[1] = (byte) (i >> 48);
		bs[2] = (byte) (i >> 40);
		bs[3] = (byte) (i >> 32);
		bs[4] = (byte) (i >> 24);
		bs[5] = (byte) (i >> 16);
		bs[6] = (byte) (i >> 8);
		bs[7] = (byte) (i & 255);
		return bs;
	}

	/**
	 * 长整形转为8位二进制数，写入到指定数组的指定位置
	 */
	public static void toBytes(long l, byte[] bs, int start) {
		byte[] arr = toBytes(l);
		for (int i = 0; i < 8; i++) {
			bs[start + i] = arr[i];
		}
	}

	/**
	 * 二进制转长整形
	 */
	public static long toLong(byte[] bs) {
		return toLong(bs, 0);
	}

	/**
	 * 从指定数据的指定位置开始，读取8位二进制，转为长整形
	 */
	public static long toLong(byte[] bs, int index) {
		return ((long) bs[index] & 0xff) << 56 | ((long) bs[index + 1] & 0xff) << 48 | ((long) bs[index + 2] & 0xff) << 40
				| ((long) bs[index + 3] & 0xff) << 32 | ((long) bs[index + 4] & 0xff) << 24 | ((long) bs[index + 5] & 0xff) << 16
				| ((long) bs[index + 6] & 0xff) << 8 | ((long) bs[index + 7] & 0xff) << 0;

	}

	/**
	 * 以参数format指定的格式格式化参数n
	 */
	public static String format(Number n, String format) {
		DecimalFormat df = new DecimalFormat(format);
		return df.format(n);
	}

	/**
	 * 传入指定数字，返回对应的二进制数组(所有二进制位为1的十进制数值集合)
	 * 
	 * @param num
	 * @return
	 */
	public static ArrayList<Integer> getBinaryList(int num) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		String binStr = Integer.toBinaryString(num);
		for (int i = 0; i < binStr.length(); i++) {
			if (binStr.charAt(i) == '1') {
				int intpow = (int) Math.pow(2, binStr.length() - i - 1);
				list.add(intpow);
			}
		}
		return list;
	}

	public static String getBinaryString(int num) {
		ArrayList<Integer> list = getBinaryList(num);
		return StringUtil.join(list.toArray());
	}

	/**
	 * 将数字或数字字符串封装成BigDecimal
	 * 
	 * @param number
	 * @return
	 */
	public static BigDecimal getBigDecimal(Object number) {
		if (number == null) {
			return null;
		}
		if (number instanceof BigDecimal) {
			return (BigDecimal) number;
		} else if (number instanceof Double || number instanceof Float) {
			return new BigDecimal(number.toString());
		} else if (number instanceof Integer) {
			return new BigDecimal((Integer) number);
		} else if (number instanceof Long) {
			return new BigDecimal((Long) number);
		} else {
			return new BigDecimal(number.toString());
		}
	}

	/**
	 * 求商运算(a/b)
	 * <p>
	 * 默认采用10位浮点精度计算 (用来解决浮点运算时精度丢失,不理想的情况)
	 * </p>
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static Number div(Number a, Number b) {
		return getBigDecimal(a).divide(getBigDecimal(b), DEFAULT_SCALE, DEFAULT_ROUNDINGMODE);
	}

	/**
	 * 求商运算(a/b)
	 * <p>
	 * 用来解决浮点运算时精度丢失,不理想的情况
	 * </p>
	 * 
	 * @param a
	 * @param b
	 * @param scale 使用的浮点精度位数
	 * @return
	 */
	public static Number div(Number a, Number b, int scale) {
		return getBigDecimal(a).divide(getBigDecimal(b), scale, DEFAULT_ROUNDINGMODE);
	}

	/**
	 * 求和运算(a+b)
	 * <p>
	 * 用来解决浮点运算时精度丢失,不理想的情况
	 * </p>
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static Number sum(Number a, Number b) {
		return getBigDecimal(a).add(getBigDecimal(b));
	}

	/**
	 * 求差运算(a-b)
	 * <p>
	 * 用来解决浮点运算时精度丢失,不理想的情况
	 * </p>
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static Number sub(Number a, Number b) {
		return getBigDecimal(a).subtract(getBigDecimal(b));
	}

	/**
	 * 求积运算(a*b)
	 * <p>
	 * 用来解决浮点运算时精度丢失,不理想的情况
	 * </p>
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static Number mul(Number a, Number b) {
		return getBigDecimal(a).multiply(getBigDecimal(b));
	}

	/**
	 * 比较两个数字是否相等
	 */
	public static boolean equal(Number a, Number b) {
		if (a instanceof Float || b instanceof Float || a instanceof Double || b instanceof Double) {
			// 因为计算存储浮点型不精确的问题，会导致两个应该相等的浮点型有一个很小的差异
			return Math.abs(a.floatValue() - b.floatValue()) < 0.000001;
		}
		return a.equals(b);
	}
}
