package com.zving.framework.security;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zving.framework.Config;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.ObjectUtil;

/**
 * 系统信息工具类
 * 
 * @Author 王育春
 * @Date 2009-4-17
 * @Mail wyuch@zving.com
 */
public class SystemInfo {
	public final static String getMacAddress() {
		String os = System.getProperty("os.name").toLowerCase();
		String output = null;
		try {
			String cmd = "ipconfig /all";
			if (os.indexOf("windows") < 0) {
				cmd = "ifconfig";
			}
			Process proc = Runtime.getRuntime().exec(cmd);
			InputStream is = proc.getInputStream();
			output = FileUtil.readText(is, Config.getFileEncode());
		} catch (Exception ex) {
			String cmd = "ipconfig /all";
			if (os.indexOf("windows") < 0) {
				cmd = "/sbin/ifconfig";// 尝试/sbin/ifconfig
			}
			try {
				Process proc = Runtime.getRuntime().exec(cmd);
				InputStream is = proc.getInputStream();
				output = FileUtil.readText(is, Config.getFileEncode());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (output == null) {
			return null;
		}
		output = output.toUpperCase();
		// mac地址00开头的在solaris上会只有1个0
		Pattern p = Pattern.compile("([0-9A-Fa-f]{1,2}[\\:\\-]){5}[0-9A-Fa-f]{2}", Pattern.DOTALL);
		Matcher m = p.matcher(output);
		int lastIndex = 0;
		StringBuilder sb = new StringBuilder();
		while (m.find(lastIndex)) {
			if (m.end() < output.length() - 1) {
				String next = output.substring(m.end(), m.end() + 1);
				if (ObjectUtil.in(next, "-", ":")) {
					lastIndex = m.end();
					continue;
				}
			}
			if (lastIndex != 0) {
				sb.append(",");
			}
			sb.append(m.group(0));
			lastIndex = m.end();
		}
		String mac = sb.toString().replace(':', '-');
		return mac;
	}
}