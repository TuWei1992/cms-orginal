package com.zving.framework.xml;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import com.zving.framework.utility.FileUtil;

/**
 * 多个XML文件载入器，可以在多个XML文件中查找元素
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-7-26
 */
public class XMLMultiLoader {
	private XMLElement root = new XMLElement("_ROOT");

	/**
	 * 载入路径下的所有XML文件，如果路径本身是个文件，则载入单个文件。
	 * 
	 * @param path 路径
	 */
	public void load(String path) {
		File f = new File(path);
		load(f);
	}

	/**
	 * 载入路径下的所有XML文件，如果路径本身是个文件，则载入单个文件。
	 * 
	 * @param f 文件路径
	 */
	public void load(File f) {
		if (!f.exists()) {
			return;
		}
		if (f.isFile()) {
			loadOneFile(f);
		} else {
			File[] fs = f.listFiles();
			if (fs == null || fs.length == 0) {
				return;
			}
			for (File element : fs) {
				f = element;
				if (f.isFile() && (f.getName().toLowerCase().endsWith(".xml") || f.getName().toLowerCase().endsWith(".plugin"))) {
					loadOneFile(f);
				}
			}
		}
	}

	/**
	 * 载入输入流
	 * 
	 * @param is 输入流
	 */
	public void load(InputStream is) {// NO_UCD
		loadOneFile(is);
	}

	/**
	 * 清除掉已经载入的所有节点
	 */
	public void clear() {
		root = new XMLElement("_ROOT");
	}

	/**
	 * 载入输入流
	 */
	private void loadOneFile(InputStream is) {
		XMLParser parser = new XMLParser(is);
		parser.parse();
		XMLElement singleRoot = parser.getDocument().getRoot();
		singleRoot.setParent(root);
	}

	/**
	 * 载入单个文件
	 */
	private void loadOneFile(File f) {
		loadOneFile(FileUtil.readText(f));
	}

	/**
	 * 载入一个字符串
	 */
	private void loadOneFile(String xml) {
		XMLParser parser = new XMLParser(xml);
		parser.parse();
		XMLElement singleRoot = parser.getDocument().getRoot();
		singleRoot.setParent(root);
	}

	/**
	 * 返回虚拟根节点， 所有载入的XML文件中的根节点会变成虚拟根节点的子节点。
	 * 
	 * @return 根节点
	 */
	public XMLElement getRoot() {
		return root;
	}

	/**
	 * 返回指定简易路径下属性值等指定参数的元素列表
	 * 
	 * @param path 简易路径，见XMLDocument中关于简易路径的说明。
	 * @param attrName 属性名
	 * @param attrValue 属性值
	 * @return XML元素列表
	 */
	public XMLElement elements(String path, String attrName, String attrValue) {
		List<XMLElement> list = root.elementsByAttribute(path, attrName, attrValue);
		if (list == null || list.size() == 0) {
			return null;
		}
		return list.get(0);
	}

	/**
	 * 返回符合指定简易路径要求的所有元素
	 * 
	 * @param path 简易路径，见XMLDocument中关于简易路径的说明。
	 * @return XML元素列表
	 */
	public List<XMLElement> elements(String path) {
		return root.elements(path);
	}

}
