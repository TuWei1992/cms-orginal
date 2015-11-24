package com.zving.framework.security;

import com.zving.framework.utility.NumberUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 密码加盐工具类
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-12-31
 */
public class PasswordUtil {
	/**
	 * 生成含有随机盐的密码
	 */
	public static String generate(String password) {
		String salt = StringUtil.leftPad(String.valueOf(NumberUtil.getRandomInt(99999999)), '0', 8)
				+ StringUtil.leftPad(String.valueOf(NumberUtil.getRandomInt(99999999)), '0', 8);
		password = StringUtil.md5Hex(password + salt);
		char[] cs = new char[48];
		for (int i = 0; i < 48; i += 3) {
			cs[i] = password.charAt(i / 3 * 2);
			char c = salt.charAt(i / 3);
			cs[i + 1] = c;
			cs[i + 2] = password.charAt(i / 3 * 2 + 1);
		}
		return new String(cs);
	}

	/**
	 * 校验密码是否正确
	 */
	public static boolean verify(String password, String md5) {
		char[] cs1 = new char[32];
		char[] cs2 = new char[16];
		for (int i = 0; i < 48; i += 3) {
			cs1[i / 3 * 2] = md5.charAt(i);
			cs1[i / 3 * 2 + 1] = md5.charAt(i + 2);
			cs2[i / 3] = md5.charAt(i + 1);
		}
		String salt = new String(cs2);
		return StringUtil.md5Hex(password + salt).equals(new String(cs1));
	}
}
