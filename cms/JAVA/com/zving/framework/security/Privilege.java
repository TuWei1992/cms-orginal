package com.zving.framework.security;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Map.Entry;
import java.util.Set;

import com.zving.framework.collection.Mapx;
import com.zving.framework.json.JSON;
import com.zving.framework.json.JSONObject;
import com.zving.framework.thirdparty.commons.ArrayUtils;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.framework.utility.ZipUtil;

/**
 * 权限集合。每一个用户会话中都有一个本类的实例。
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-6-30
 */
public class Privilege implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String OwnerType_Branch = "B";

	public static final String OwnerType_Role = "R";

	public static final String OwnerType_User = "U";

	public static final int Flag_Allow = 1;

	public static final int Flag_NotSet = 0;

	public static final String Or = "||";

	private static final String CompressedPrefix = "Compressed\n";

	Mapx<String, Integer> old = new Mapx<String, Integer>();

	Mapx<String, Integer> types = new Mapx<String, Integer>();

	Mapx<String, String> ids = new Mapx<String, String>();

	Mapx<String, Integer> keys = new Mapx<String, Integer>();

	private static char[] bitChars = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
			'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '$', '%', 'A', 'B', 'C', 'D', 'E',
			'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

	private static boolean isID(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	private static int charToFlag(char flag) {
		int i = 0;
		if (flag == 36 || flag == 37) {
			i = flag;
		} else if (flag < 58) {// 数字
			i = flag - 48;
		} else if (flag < 91) {// 大写字母
			i = flag - 27;
		} else if (flag < 123) {// 小写字母
			i = flag - 87;
		}
		return i;
	}

	public boolean hasPriv(String k) {
		if (k == null) {
			return false;
		}
		int index = k.lastIndexOf('.');
		if (index > 0) {
			String id = k.substring(index + 1);
			if (isID(id)) {
				k = k.substring(0, index);
				String flags = ids.get(id);
				if (flags == null) {
					return false;
				}
				Integer order = types.get(k);
				if (order == null || order >= flags.length() * 6) {
					return false;
				}
				char flag = flags.charAt(order / 6);
				return (charToFlag(flag) & 1 << 5 - order % 6) != 0;
			} else {
				return keys.containsKey(k);
			}
		} else {
			return keys.containsKey(k);
		}
	}

	public void add(String k) {
		put(Flag_Allow, k);
	}

	public void put(int flag, String k) {
		if (k == null) {
			return;
		}
		int index = k.lastIndexOf('.');
		if (index > 0) {
			String id = k.substring(index + 1);
			if (isID(id)) {
				k = k.substring(0, index);
				Integer order = types.get(k);
				if (order == null) {
					order = types.size();
					types.put(k, types.size());
				}
				String flags = ids.get(id);
				char[] arr = null;
				if (flags == null) {
					arr = new char[types.size() % 6 == 0 ? types.size() / 6 : types.size() / 6 + 1];
					for (int i = 0; i < arr.length; i++) {
						arr[i] = '0';
					}
				} else {
					arr = flags.toCharArray();
					if (order / 6 >= arr.length) {
						char[] arr2 = new char[order / 6 + 1];
						for (int i = 0; i < arr.length; i++) {
							arr2[i] = arr[i];
						}
						for (int i = arr.length; i < arr2.length; i++) {
							arr2[i] = '0';
						}
						arr = arr2;
					}
				}
				char c = arr[order / 6];
				int f = charToFlag(c);
				int v = 1 << 5 - order % 6;
				if ((f & v) == 0 && flag == Flag_Allow) {
					f += v;
					c = bitChars[f];
					arr[order / 6] = c;
					ids.put(id, new String(arr));
				}
				if ((f & v) != 0 && flag == Flag_NotSet) {
					f -= v;
					c = bitChars[f];
					arr[order / 6] = c;
					ids.put(id, new String(arr));
				}
			} else {
				keys.put(k, 1);
			}
		} else {
			keys.put(k, 1);
		}
	}

	public void remove(String k) {
		if (k == null) {
			return;
		}
		int index = k.lastIndexOf('.');
		if (index > 0) {
			String id = k.substring(index + 1);
			if (isID(id)) {
				k = k.substring(0, index);
				Integer order = types.get(k);
				if (order == null) {
					return;
				}
				String flags = ids.get(id);
				if (flags == null) {
					return;
				}
				char[] arr = flags.toCharArray();
				int i = order / 6;
				if (i >= arr.length) {
					return;
				}
				char c = arr[i];
				int f = charToFlag(c);
				int v = 1 << 5 - order % 6;
				if ((f & v) != 0) {
					f -= v;
					c = bitChars[f];
					arr[order / 6] = c;
					ids.put(id, new String(arr));
				}
			} else {
				keys.remove(k);
			}
		} else {
			keys.remove(k);
		}
	}

	public boolean containsKey(String k) {
		return hasPriv(k);
	}

	public String getString(String k) {
		return hasPriv(k) ? "1" : "0";
	}

	/**
	 * 权限交集操作。<br>
	 * 在用户登录后获得权限集合时必须要先将自身权限和所在机构权限范围求交集。这是为了处理以下情况：
	 * 用户A在机构Org1下获得了权限P，保存在自身的权限记录里。然后修改A的机构为Org2，但Org2的权限范围不包括P。
	 */
	public void intersect(Privilege p) {
		if (p == null) {
			return;
		}
		for (String k : keys.keySet()) {
			if (!p.keys.containsKey(k)) {
				keys.remove(k);
			}
		}
		String[] typeArr = new String[types.size()];
		typeArr = types.keySet().toArray(typeArr);

		for (Entry<String, String> entry : ids.entrySet()) {
			String k = entry.getKey();
			String flags = ids.get(k);
			char[] arr = new char[types.size()];
			if (flags != null) {
				for (int i = 0; i < flags.length(); i++) {
					int f = charToFlag(flags.charAt(i));
					for (int j = 0; j < 6; j++) {
						if ((f & 1 << (5 - j)) != 0) {
							String t = typeArr[i * 6 + j];
							if (p.types.containsKey(t)) {
								String pflags = p.ids.get(k);
								if (StringUtil.isEmpty(pflags)) {
									arr[i * 6 + j] = '0';
								} else {
									int pi = p.types.getInt(t);
									int pf = charToFlag(pflags.charAt(pi / 6));
									if ((pf & 1 << (5 - (pi % 6))) != 0) {
										arr[i * 6 + j] = '1';
									} else {
										arr[i * 6 + j] = '0';
									}
								}
							} else {
								arr[i * 6 + j] = '0';
							}
						}
					}
				}
			}
			for (int i = 0; i < arr.length; i += 6) {
				int num = 0;
				for (int j = 0; j < 6 && i + j < arr.length; j++) {
					if (arr[i + j] == '1') {
						num += 1 << 6 - j - 1;
					}
				}
				arr[i / 6] = bitChars[num];
			}
			flags = new String(arr, 0, arr.length % 6 == 0 ? arr.length / 6 : arr.length / 6 + 1);
			ids.put(k, flags);
		}
	}

	/**
	 * 合并另一个权限对象的值。本方法主要用于两个用途：<br>
	 * 1、用户登录时将用户所属角色权限赋予用户。<br>
	 * 2、用户代理其他用户操作。
	 */
	public void union(Privilege p) {
		if (p == null) {
			return;
		}
		keys.putAll(p.keys);
		for (String k : p.types.keySet()) {
			if (!types.containsKey(k)) {
				types.put(k, types.size());
			}
		}
		String[] oldTypes = new String[p.types.size()];
		oldTypes = p.types.keySet().toArray(oldTypes);
		for (Entry<String, String> entry : p.ids.entrySet()) {
			char[] arr = new char[types.size()];
			String k = entry.getKey();
			String flags = ids.get(k);
			if (flags != null) {
				for (int i = 0; i < flags.length(); i++) {
					int f = charToFlag(flags.charAt(i));
					for (int j = 0; j < 6; j++) {
						if ((f & 1 << 5 - j) != 0) {
							arr[i * 6 + j] = '1';
						}
					}
				}
			}
			String flags2 = entry.getValue();
			for (int i = 0; i < flags2.length(); i++) {
				int f = charToFlag(flags2.charAt(i));
				for (int j = 0; j < 6; j++) {
					if ((f & 1 << 5 - j) != 0) {
						if (i * 6 + j < p.types.size()) {
							String type = oldTypes[i * 6 + j];
							Integer order = types.get(type);
							arr[order] = '1';
						}
					}
				}
			}
			for (int i = 0; i < arr.length; i += 6) {
				int num = 0;
				for (int j = 0; j < 6 && i + j < arr.length; j++) {
					if (arr[i + j] == '1') {
						num += 1 << 6 - j - 1;
					}
				}
				arr[i / 6] = bitChars[num];
			}
			flags = new String(arr, 0, arr.length % 6 == 0 ? arr.length / 6 : arr.length / 6 + 1);
			ids.put(k, flags);
		}
	}

	public void parse(String str) {
		clear();
		if (ObjectUtil.empty(str)) {
			return;
		}
		if (str.startsWith("{")) {// 位标识的字符串
			JSONObject all = JSON.parseJSONObject(str);
			JSONObject jo = all.getJSONObject("types");
			for (Entry<String, Object> entry : jo.entrySet()) {
				types.put(entry.getKey(), Integer.parseInt(entry.getValue().toString()));
			}
			jo = all.getJSONObject("keys");
			for (Entry<String, Object> entry : jo.entrySet()) {
				keys.put(entry.getKey(), Integer.parseInt(entry.getValue().toString()));
			}
			jo = all.getJSONObject("ids");
			for (Entry<String, Object> entry : jo.entrySet()) {
				ids.put(entry.getKey(), entry.getValue().toString());
			}
			old = null;
		} else if (str.startsWith(CompressedPrefix)) {// 第一次优化的字符串
			str = str.substring(CompressedPrefix.length());
			try {
				str = new String(ZipUtil.unzip(StringUtil.base64Decode(str)), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			Mapx<String, Object> map = JSON.parseJSONObject(str);
			oldConvert(map, "");
			bitConvert(old);
		} else {// 最原始的字符串
			int i = 0;
			while (true) {
				int pos1 = str.indexOf('=', i);
				if (pos1 < 0) {
					break;
				}
				int pos2 = str.indexOf('\n', pos1);
				if (pos2 < 0) {
					break;
				}
				String key = str.substring(i, pos1);
				String value = str.substring(pos1 + 1, pos2);
				old.put(key, value.charAt(0) == '1' ? 1 : 0);
				i = pos2 + 1;
			}
			bitConvert(old);
		}
	}

	public void bitConvert(Mapx<String, Integer> old) {
		Mapx<String, Integer> types = new Mapx<String, Integer>();
		Mapx<String, char[]> ids = new Mapx<String, char[]>();
		Mapx<String, Integer> keys = new Mapx<String, Integer>();
		for (Entry<String, Integer> entry : old.entrySet()) {
			String k = entry.getKey();
			if (entry.getValue() == null || entry.getValue() != 1) {
				continue;
			}
			int index = k.lastIndexOf('.');
			if (index > 0) {
				String id = k.substring(index + 1);
				if (isID(id)) {
					k = k.substring(0, index);
					if (!types.containsKey(k)) {
						types.put(k, types.size());
					}
				}
			}
		}
		char[] cs = new char[types.size()];
		for (int i = 0; i < cs.length; i++) {
			cs[i] = '0';
		}
		for (Entry<String, Integer> entry : old.entrySet()) {
			String k = entry.getKey();
			if (entry.getValue() == null || entry.getValue() != 1) {
				continue;
			}
			int index = k.lastIndexOf('.');
			if (index > 0) {
				String id = k.substring(index + 1);
				if (isID(id)) {
					k = k.substring(0, index);
					Integer order = types.get(k);
					char[] arr = ids.get(id);
					if (arr == null) {
						arr = ArrayUtils.clone(cs);
						ids.put(id, arr);
					}
					arr[order] = '1';
				} else {
					keys.put(k, Flag_Allow);
				}
			} else {
				keys.put(k, Flag_Allow);
			}
		}
		for (Entry<String, char[]> entry : ids.entrySet()) {
			char[] arr = entry.getValue();
			for (int i = 0; i < arr.length; i += 6) {
				int num = 0;
				for (int j = 0; j < 6 && i + j < arr.length; j++) {
					if (arr[i + j] == '1') {
						num += 1 << 6 - j - 1;
					}
				}
				arr[i / 6] = bitChars[num];
			}
		}

		this.types = types;
		this.keys = keys;
		for (Entry<String, char[]> entry : ids.entrySet()) {
			char[] arr = entry.getValue();
			this.ids.put(entry.getKey(), new String(entry.getValue(), 0, arr.length % 6 == 0 ? arr.length / 6 : arr.length / 6 + 1));
		}
		old = null;
	}

	@SuppressWarnings("unchecked")
	private void oldConvert(Mapx<String, Object> map, String prefix) {
		for (java.util.Map.Entry<String, Object> entry : map.entrySet()) {
			String k = entry.getKey();
			Object v = entry.getValue();
			if (v instanceof Mapx) {
				oldConvert((Mapx<String, Object>) v, prefix + k + ".");
			} else {
				if (k.endsWith("_VALUE")) {
					k = k.substring(0, k.length() - 6);
				}
				old.put(prefix + k, Integer.parseInt(v.toString()));
			}
		}
	}

	public void clear() {
		types.clear();
		keys.clear();
		ids.clear();
		old.clear();
	}

	@Override
	public String toString() {
		Mapx<String, Object> map = new Mapx<String, Object>();
		map.put("types", types);
		map.put("keys", keys);
		map.put("ids", ids);
		return JSON.toJSONString(map);
	}

	public Set<String> idSet() {
		return ids.keySet();
	}

}
