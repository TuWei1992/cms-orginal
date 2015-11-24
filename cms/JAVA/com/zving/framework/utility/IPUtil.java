package com.zving.framework.utility;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * IP辅助工具类
 * </p>
 * <p>
 * 提供IP的文本、字节码的转换,及IP范围的比较判定
 * </p>
 * 
 * @author ChangSong
 * @mail cs@zving.com
 * @date 2014年11月14日 下午7:17:59
 */
public class IPUtil {
	/**
	 * IPv4字节数
	 */
	public final static int INADDR4SZ = 4;
	/**
	 * IPv6字节数
	 */
	public final static int INADDR16SZ = 16;
	private final static int INT16SZ = 2;
	private final static int BYTELEN = 8;
	private static byte[][] special_address;// 内网保留地址
	private static int[] special_bit;//
	static {
		String[] ips = new String[] {

		/**
		 * IPv4保留地址
		 */
		"0.0.0.0/8",//
				"10.0.0.0/8",//
				"127.0.0.0/8", //
				"169.254.0.0/16",//
				"172.16.0.0/12",//
				"192.0.0.0/24",//
				"192.0.2.0/24",//
				"192.88.99.0/24",//
				"192.168.0.0/16",//
				"198.18.0.0/15",//
				"198.51.100.0/24",//
				"203.0.113.0/24",//
				"224.0.0.1/4",//
				"240.0.0.0/4",//
				"255.255.255.255/32",
				/**
				 * IPv6保留地址
				 */
				"::/128",//
				"::1/128",//
				"fe80::/10",//
				"fec0::/10",//
				"fc00::/7",//
				"ff00::/8",//
				"ff02::1:ff00:0000/104"//

		};
		special_address = new byte[ips.length][];
		special_bit = new int[ips.length];
		for (int i = 0; i < ips.length; i++) {
			String[] item = StringUtil.splitEx(ips[i], "/");
			special_address[i] = textToIPBytes(item[0]);
			special_bit[i] = Integer.parseInt(item[1]);
		}
		ips = null;
	}

	/**
	 * 判断地址是否保留IP
	 * 
	 * @param address
	 * @return
	 */
	public static boolean isSpecialAddress(String address) {
		byte[] dat = textToIPv4Bytes(address);
		if (dat == null) {
			dat = textToIPv6Bytes(address);
		}
		return isSpecialAddress(dat);
	}

	/**
	 * 检测字符串中的第一个非保留IP
	 * 
	 * @param ips
	 * @return 如果没有检测到,则返回第一个IP
	 */
	public static String checkFirstInternetIP(String ips) {
		String[] ipArray = splitIPs(ips);
		if (ipArray == null) {
			return ips;
		}
		for (String ip : ipArray) {
			if (!isSpecialAddress(ip)) {
				return ip;
			}
		}
		return ipArray[0];
	}

	/**
	 * 判断地址是否保留IP
	 * 
	 * @param address
	 * @return
	 */
	public static boolean isSpecialAddress(byte[] address) {
		for (int i = 0; i < special_address.length; i++) {
			if (inRound(address, special_address[i], special_bit[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 检查IP是否在指定范围内
	 * 
	 * @param compareIP 检查的IP
	 * @param minIP 最小IP
	 * @param maxIP 最大IP
	 * @return
	 */
	public static boolean inRound(String compareIP, String minIP, String maxIP) {
		return inRound(textToIPBytes(compareIP), textToIPBytes(minIP), textToIPBytes(maxIP));
	}

	/**
	 * 检查IP是否在指定范围内
	 * 
	 * @param compareIP 检查的IP
	 * @param minIP 最小IP
	 * @param maxIP 最大IP
	 * @return
	 */
	public static boolean inRound(String compareIP, byte[] minIP, byte[] maxIP) {
		byte[] ipDat = textToIPBytes(compareIP);
		return inRound(ipDat, minIP, maxIP);
	}

	/**
	 * 检查IP是否在指定范围内
	 * 
	 * @param compareIP 检查的IP
	 * @param minIP 最小IP
	 * @param maxIP 最大IP
	 * @return
	 */
	public static boolean inRound(byte[] compareIP, byte[] minIP, byte[] maxIP) {
		try {
			return compareIP(compareIP, minIP) >= 0 && compareIP(compareIP, maxIP) <= 0;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 检查IP是否在指定范围内
	 * 
	 * @param compareIP 检查的IP
	 * @param ipMark 范围IP
	 * @param hiBitLen 高位识别码位数
	 * @return
	 */
	public static boolean inRound(byte[] compareIP, byte[] ipMark, int hiBitLen) {
		if (compareIP == null || ipMark == null) {
			throw new NullPointerException("IP地址错误");
		}
		byte[] dat1 = compareIP, dat2 = ipMark;
		if (dat1.length == INADDR16SZ && dat2.length == INADDR4SZ) {
			dat1 = convertFromIPv4MappedAddress(dat1);
			if (dat1 == null) {
				dat1 = compareIP;
			}
		} else if (dat1.length == INADDR4SZ && dat2.length == INADDR16SZ) {
			dat2 = convertFromIPv4MappedAddress(dat2);
			if (dat2 == null) {
				dat2 = ipMark;
			}
		}
		if (dat2.length == dat1.length) {
			return unsignedCompare(dat1, dat2, hiBitLen);
		} else {
			return false;
		}
	}

	/**
	 * 比较两个IP大小
	 * 
	 * @param address1 地址1
	 * @param address2 地址2
	 * @return <,=,> : -1,0,1
	 * @throws Exception
	 */
	public static int compareIP(byte[] address1, byte[] address2) throws Exception {
		if (address1 == null || address2 == null) {
			throw new NullPointerException("IP地址错误");
		}
		byte[] dat1 = address1, dat2 = address2;
		if (dat1.length == INADDR16SZ && dat2.length == INADDR4SZ) {
			dat1 = convertFromIPv4MappedAddress(dat1);
			if (dat1 == null) {
				dat1 = address1;
			}
		} else if (dat1.length == INADDR4SZ && dat2.length == INADDR16SZ) {
			dat2 = convertFromIPv4MappedAddress(dat2);
			if (dat2 == null) {
				dat2 = address2;
			}
		}
		if (dat1.length == dat2.length) {
			return unsignedCompare(dat1, dat2);
		} else {
			throw new Exception("不同标准IP地址无法比较");
		}

	}

	/**
	 * 将指定字节数组填充到指定长度
	 * 
	 * @param dat 字节数组
	 * @param byteSize 目标长度(字节数)
	 * @param atFront 是否在前面填充
	 * @param fillZero 是否填充0值(否则每一位都填充二进制1)
	 * @return
	 */
	public static byte[] fillBytes(byte[] dat, int byteSize, boolean atFront, boolean fillZero) {
		if (dat.length == byteSize) {
			return dat;
		} else {
			byte[] result = new byte[byteSize];
			System.arraycopy(dat, 0, result, atFront ? byteSize - dat.length : 0, dat.length);
			if (!fillZero) {
				for (int i = atFront ? 0 : dat.length; i < result.length - (atFront ? dat.length : 0); i++) {
					result[i] = (byte) 0xFF;
				}
			}
			return result;
		}
	}

	/**
	 * 对比字节码指前几位是否相同(如果字节码长度不同,会在短的字节码高位填充0值再比较)
	 * 
	 * @param dat1
	 * @param dat2
	 * @param bitLen
	 * @return
	 */
	public static boolean unsignedCompare(byte[] dat1, byte[] dat2, int bitLen) {
		if (dat1.length != dat2.length) {
			if (dat1.length > dat2.length) {
				dat2 = fillBytes(dat2, dat1.length, true, true);
			} else {
				dat1 = fillBytes(dat1, dat2.length, true, true);
			}
		}
		for (int i = 0; i < dat1.length; i++) {
			if (bitLen == 0) {
				return true;
			}
			if (bitLen < BYTELEN) {
				if ((dat1[i] >> (BYTELEN - bitLen)) != (dat2[i] >> (BYTELEN - bitLen))) {
					return false;
				} else {
					return true;
				}
			} else {
				if (dat1[i] != dat2[i]) {
					return false;
				}
			}
			bitLen = bitLen - BYTELEN;
		}
		return true;
	}

	/**
	 * 无符号比较字节码(如果字节码长度不同,会在短的字节码高位填充0值再比较)
	 * 
	 * @param dat1
	 * @param dat2
	 * @return dat1大于dat2返回1,小于返回-1,相等返回0
	 */
	public static int unsignedCompare(byte[] dat1, byte[] dat2) {
		boolean leftIsLen = dat1.length > dat2.length;
		int lenSub = leftIsLen ? dat1.length - dat2.length : dat2.length - dat1.length;
		byte[] lenDat, shortDat;
		if (leftIsLen) {
			lenDat = dat1;
			shortDat = dat2;
		} else {
			lenDat = dat2;
			shortDat = dat1;
		}
		int sub;
		int i = 0;
		for (; i < lenSub; i++) {
			if ((0xFF & lenDat[i]) > 0) {
				return leftIsLen ? 1 : -1;
			}
		}
		for (; i < lenDat.length; i++) {
			sub = (0xFF & lenDat[i]) - (0xFF & shortDat[i - lenSub]);
			if (sub > 0) {
				return leftIsLen ? 1 : -1;
			}
			if (sub < 0) {
				return leftIsLen ? -1 : 1;
			}
		}
		return 0;
	}

	/**
	 * 将IP转换为字节码
	 * 
	 * @param src
	 * @return
	 */
	public static byte[] textToIPBytes(String src) {
		byte[] result = textToIPv4Bytes(src);
		if (result == null) {
			result = textToIPv6Bytes(src);
		}
		return result;
	}

	/**
	 * 将IPv4转换为字节码
	 * 
	 * @param src
	 * @return
	 */
	public static byte[] textToIPv4Bytes(String src) {
		if (src.length() == 0) {
			return null;
		}
		byte[] res = new byte[INADDR4SZ];
		String[] s = StringUtil.splitEx(src, ".");
		long val;
		try {
			switch (s.length) {
			case 1:
				val = Long.parseLong(s[0]);
				if (val < 0 || val > 0xffffffffL)
					return null;
				res[0] = (byte) ((val >> 24) & 0xff);
				res[1] = (byte) (((val & 0xffffff) >> 16) & 0xff);
				res[2] = (byte) (((val & 0xffff) >> 8) & 0xff);
				res[3] = (byte) (val & 0xff);
				break;
			case 2:
				val = Integer.parseInt(s[0]);
				if (val < 0 || val > 0xff)
					return null;
				res[0] = (byte) (val & 0xff);
				val = Integer.parseInt(s[1]);
				if (val < 0 || val > 0xffffff)
					return null;
				res[1] = (byte) ((val >> 16) & 0xff);
				res[2] = (byte) (((val & 0xffff) >> 8) & 0xff);
				res[3] = (byte) (val & 0xff);
				break;
			case 3:
				for (int i = 0; i < 2; i++) {
					val = Integer.parseInt(s[i]);
					if (val < 0 || val > 0xff)
						return null;
					res[i] = (byte) (val & 0xff);
				}
				val = Integer.parseInt(s[2]);
				if (val < 0 || val > 0xffff)
					return null;
				res[2] = (byte) ((val >> 8) & 0xff);
				res[3] = (byte) (val & 0xff);
				break;
			case 4:
				for (int i = 0; i < 4; i++) {
					val = Integer.parseInt(s[i]);
					if (val < 0 || val > 0xff)
						return null;
					res[i] = (byte) (val & 0xff);
				}
				break;
			default:
				return null;
			}
		} catch (NumberFormatException e) {
			return null;
		}
		return res;
	}

	/**
	 * 将IPv6转换为字节码
	 * 
	 * @param src
	 * @return
	 */
	public static byte[] textToIPv6Bytes(String src) {
		// Shortest valid string is "::", hence at least 2 chars
		if (src.length() < 2) {
			return null;
		}
		int colonp;
		char ch;
		boolean saw_xdigit;
		int val;
		char[] srcb = src.toCharArray();
		byte[] dst = new byte[INADDR16SZ];

		int srcb_length = srcb.length;
		int pc = src.indexOf("%");
		if (pc == srcb_length - 1) {
			return null;
		}

		if (pc != -1) {
			srcb_length = pc;
		}

		colonp = -1;
		int i = 0, j = 0;
		/* Leading :: requires some special handling. */
		if (srcb[i] == ':')
			if (srcb[++i] != ':')
				return null;
		int curtok = i;
		saw_xdigit = false;
		val = 0;
		while (i < srcb_length) {
			ch = srcb[i++];
			int chval = Character.digit(ch, 16);
			if (chval != -1) {
				val <<= 4;
				val |= chval;
				if (val > 0xffff)
					return null;
				saw_xdigit = true;
				continue;
			}
			if (ch == ':') {
				curtok = i;
				if (!saw_xdigit) {
					if (colonp != -1)
						return null;
					colonp = j;
					continue;
				} else if (i == srcb_length) {
					return null;
				}
				if (j + INT16SZ > INADDR16SZ)
					return null;
				dst[j++] = (byte) ((val >> 8) & 0xff);
				dst[j++] = (byte) (val & 0xff);
				saw_xdigit = false;
				val = 0;
				continue;
			}
			if (ch == '.' && ((j + INADDR4SZ) <= INADDR16SZ)) {
				String ia4 = src.substring(curtok, srcb_length);
				/* check this IPv4 address has 3 dots, ie. A.B.C.D */
				int dot_count = 0, index = 0;
				while ((index = ia4.indexOf('.', index)) != -1) {
					dot_count++;
					index++;
				}
				if (dot_count != 3) {
					return null;
				}
				byte[] v4addr = textToIPv4Bytes(ia4);
				if (v4addr == null) {
					return null;
				}
				for (int k = 0; k < INADDR4SZ; k++) {
					dst[j++] = v4addr[k];
				}
				saw_xdigit = false;
				break; /* '\0' was seen by inet_pton4(). */
			}
			return null;
		}
		if (saw_xdigit) {
			if (j + INT16SZ > INADDR16SZ)
				return null;
			dst[j++] = (byte) ((val >> 8) & 0xff);
			dst[j++] = (byte) (val & 0xff);
		}

		if (colonp != -1) {
			int n = j - colonp;

			if (j == INADDR16SZ)
				return null;
			for (i = 1; i <= n; i++) {
				dst[INADDR16SZ - i] = dst[colonp + n - i];
				dst[colonp + n - i] = 0;
			}
			j = INADDR16SZ;
		}
		if (j != INADDR16SZ)
			return null;
		byte[] newdst = convertFromIPv4MappedAddress(dst);
		if (newdst != null) {
			return newdst;
		} else {
			return dst;
		}
	}

	/**
	 * 将无符号IP字节码转换为整数字符串形式
	 * 
	 * @param ipBytes
	 * @return
	 */
	public static String bytesToIPText(byte[] ipBytes) {
		if (ipBytes == null) {
			return "";
		}
		int len = ipBytes.length;
		if (len == INADDR4SZ) {
			StringBuilder sb = new StringBuilder(15);
			for (int i = 0; i < len - 1; i++) {
				int num = (0xFF & ipBytes[i]);
				sb.append(num).append('.');
			}
			sb.append((0xFF & ipBytes[len - 1]));
			return sb.toString();
		} else if (len == INADDR16SZ) {
			StringBuilder sb = new StringBuilder(40);
			int num = 0;
			boolean flag = false;
			for (int i = 0; i < INADDR16SZ;) {
				num = num | ((0xFF & ipBytes[i++]) << 8);
				num = num | ((0xFF & ipBytes[i++]));
				if (flag) {
					sb.append(':');
				} else {
					flag = true;
				}
				sb.append(Integer.toHexString(num));
				num = 0;
			}
			return sb.toString();
		} else {
			return "";
		}
	}

	public static void main(String[] args) {
		String[] ary = splitIPs("192.168.23.21,        10.33.0.200");
		for (String string : ary) {
			System.out.println(string);
		}
		System.out.println(checkFirstInternetIP("192.168.23.21, 10.33.0.200"));
	}

	/**
	 * 检查地址是否IPv4
	 * 
	 * @param src
	 * @return
	 */
	public static boolean isIPv4Address(String src) {
		return textToIPv4Bytes(src) != null;
	}

	/**
	 * 检查地址是否IPv6
	 * 
	 * @param src
	 * @return
	 */
	public static boolean isIPv6LiteralAddress(String src) {
		return textToIPv6Bytes(src) != null;
	}

	/**
	 * 将IPv4映射的IPv6地址转换为ipv4
	 * 
	 * @param addr
	 * @return
	 */
	public static byte[] convertFromIPv4MappedAddress(byte[] addr) {
		if (isIPv4MappedAddress(addr)) {
			byte[] newAddr = new byte[INADDR4SZ];
			System.arraycopy(addr, 12, newAddr, 0, INADDR4SZ);
			return newAddr;
		}
		return null;
	}

	/**
	 * 分割多个IP
	 * 
	 * @param ips
	 * @return
	 */
	private static String[] splitIPs(String ips) {
		if (ips == null) {
			return null;
		}
		int len = ips.length();
		if (len < 2) {
			return null;
		}
		List<String> result = new ArrayList<String>(5);
		StringBuilder sb = new StringBuilder(len);
		boolean lastAppend = false;
		for (int i = 0; i < len; i++) {
			char c = ips.charAt(i);
			if (c >= '0' && c <= '9' || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f') || c == '.' || c == ':') {
				sb.append(c);
				lastAppend = false;
			} else if (!lastAppend) {
				result.add(sb.toString());
				sb = new StringBuilder(len - i);
				lastAppend = true;
			}
		}
		if (lastAppend == false) {
			result.add(sb.toString());
		}
		return result.toArray(new String[0]);
	}

	/**
	 * 检查是否IPv4映射的IPv6地址
	 * 
	 * @param addr
	 * @return
	 */
	private static boolean isIPv4MappedAddress(byte[] addr) {
		if (addr.length < INADDR16SZ) {
			return false;
		}
		if ((addr[0] == 0x00) && (addr[1] == 0x00) && (addr[2] == 0x00) && (addr[3] == 0x00) && (addr[4] == 0x00) && (addr[5] == 0x00)
				&& (addr[6] == 0x00) && (addr[7] == 0x00) && (addr[8] == 0x00) && (addr[9] == 0x00) && (addr[10] == (byte) 0xff)
				&& (addr[11] == (byte) 0xff)) {
			return true;
		}
		return false;
	}
}
