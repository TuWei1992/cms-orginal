package com.zving.platform.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.zving.framework.Config;
import com.zving.framework.collection.Mapx;
import com.zving.framework.i18n.Lang;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.NumberUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.FixedConfigItem;
import com.zving.platform.PlatformPlugin;
import com.zving.platform.code.ControlType;
import com.zving.platform.code.DataType;

/**
 * 允许调用API的IP地址范围
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-9-11
 */
public class APIIPRanges extends FixedConfigItem {
	public static final String ID = "Platform.APIIPRanges";

	static Mapx<String, String> singleIPs = new Mapx<String, String>();

	static List<long[]> ranges = new ArrayList<long[]>();

	static String configValue = null;

	static ReentrantLock lock = new ReentrantLock();

	public APIIPRanges() {
		super(ID, DataType.LargeText, ControlType.TextArea, "@{Platform.APIIPRanges}", PlatformPlugin.ID);
	}

	public static long convertIP(String ip) {
		String[] arr1 = StringUtil.splitEx(ip, ".");
		try {
			long t = Long.parseLong(arr1[0]) * 16777216L + Long.parseLong(arr1[1]) * 65536L + Long.parseLong(arr1[2]) * 256L
					+ Long.parseLong(arr1[3]) - 1L;
			return t;
		} catch (Exception e) {
			// IPV6的地址
		}
		return 0;
	}

	private static void init() {
		String v = Config.getValue(ID);
		if (v == null) {
			v = "";
		}
		if (!v.equals(configValue)) {
			lock.lock();
			try {
				if (v != null && !v.equals(configValue)) {
					singleIPs.clear();
					ranges.clear();

					v = v.replaceAll("[\\s\\,]+", "\n").trim();
					String[] arr = StringUtil.splitEx(v, "\n");
					for (String str : arr) {
						int i = str.indexOf('-');
						if (i > 0) {
							String ip1 = str.substring(0, i);
							String ip2 = str.substring(i + 1);
							if (!isIP(ip1)) {
								LogUtil.warn(Lang.get("Platform.InvalidIP") + ":" + ip1);
								continue;
							}
							if (!isIP(ip2)) {
								LogUtil.warn(Lang.get("Platform.InvalidIP") + ":" + ip2);
								continue;
							}
							long start = convertIP(ip1);
							long end = convertIP(ip2);
							ranges.add(new long[] { start, end });
						} else {
							if (!isIP(str)) {
								LogUtil.warn(Lang.get("Platform.InvalidIP") + ":" + str);
								continue;
							}
							singleIPs.put(str, "");
						}
					}
					configValue = v;
				}
			} finally {
				lock.unlock();
			}
		}
	}

	private static boolean isIP(String ip) {
		if (ip == null) {
			return false;
		}
		String[] arr = ip.split("[\\.\\:]+");
		if (arr.length != 4) {
			return false;
		}
		for (String str : arr) {
			if (!NumberUtil.isInt(str)) {
				return false;
			}
			int i = Integer.parseInt(str);
			if (i < 0 || i > 255) {
				return false;
			}
		}
		return true;
	}

	public static boolean isInRange(String str) {
		init();
		if (configValue == null || ObjectUtil.empty(configValue.trim())) {
			return true;
		}
		if (str.equals("0:0:0:0:0:0:0:1")) {// ipv6下的本机
			str = "127.0.0.1";
		}
		if (singleIPs.containsKey(str)) {
			return true;
		}
		long ip = convertIP(str);
		for (int i = 0; i < ranges.size(); i++) {
			long[] arr = ranges.get(i);
			if (arr[0] <= ip && ip <= arr[1]) {
				return true;
			}
		}
		return false;
	}
}
