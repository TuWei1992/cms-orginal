package com.zving.framework.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;

import com.zving.framework.collection.Mapx;

/**
 * 属性文件工具类。<br>
 * 属性之间以换行分隔，属性名和属性值之间以等号分隔。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-4-20
 */
public class PropertiesUtil {

	/**
	 * 从流中读取所有属性值
	 */
	public static Mapx<String, String> read(InputStream is) {
		String text = FileUtil.readText(is, "UTF-8");
		return read(text);
	}

	/**
	 * 从文本中读取所有属性值
	 */
	public static Mapx<String, String> read(String text) {
		Mapx<String, String> map = new Mapx<String, String>();
		int last = 0;
		String line = null;
		while (true) {
			if (last < 0) {
				break;
			}
			int i = text.indexOf('\n', last);
			if (i > 0) {
				line = text.substring(last, i);
				last = i + 1;
			} else {
				line = text.substring(last);
				last = -1;
			}
			line = line.trim();
			if (line.length() == 0 || line.startsWith("#")) {
				continue;
			}
			int index = line.indexOf('=');
			if (index < 0) {
				continue;
			}
			String k = line.substring(0, index).trim();
			String v = line.substring(index + 1);
			v = StringUtil.javaDecode(v);
			map.put(k, v);
		}
		return map;
	}

	/**
	 * 读取文件中的所有属性值
	 */
	public static Mapx<String, String> read(File f) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		try {
			return read(fis);
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 将属性值写入文件
	 */
	public static void write(File f, Map<String, String> map) {
		FileOutputStream fis = null;
		try {
			fis = new FileOutputStream(f);
			write(fis, map);
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将属性值写入流
	 */
	public static void write(OutputStream os, Map<String, String> map) {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> entry : map.entrySet()) {
			if (sb.length() != 0) {
				sb.append("\n");
			}
			sb.append(entry.getKey());
			sb.append("=");
			sb.append(StringUtil.javaEncode(entry.getValue()));
		}
		try {
			os.write(sb.toString().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
