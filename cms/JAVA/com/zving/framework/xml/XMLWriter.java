package com.zving.framework.xml;

import java.io.File;

import com.zving.framework.utility.FileUtil;

/**
 * XML写入工具类
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-10
 */
public class XMLWriter {
	/**
	 * 将XML文档写入到目标文件
	 * 
	 * @param doc XML文档
	 * @param f 目标文件
	 */
	public static void writeTo(XMLDocument doc, File f) {
		FileUtil.writeText(f.getAbsolutePath(), doc.toString(), doc.getEncoding());
	}

}
