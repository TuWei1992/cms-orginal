package com.zving.framework.template;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.zving.framework.Config;
import com.zving.framework.collection.CaseIgnoreMapx;
import com.zving.framework.collection.Mapx;
import com.zving.framework.collection.Treex;
import com.zving.framework.collection.Treex.TreeNode;
import com.zving.framework.template.exception.TemplateCompileException;
import com.zving.framework.ui.html.HtmlElement;
import com.zving.framework.ui.html.HtmlParseException;
import com.zving.framework.ui.html.HtmlParser;
import com.zving.framework.ui.resource.UIResourceFile;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.StringFormat;
import com.zving.framework.xml.XMLParser;
import com.zving.framework.xml.XMLParser.ElementAttribute;

/**
 * 模板解析器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-6-26
 */
public class TemplateParser {
	static HashMap<String, IncludeTemplate> cache = new HashMap<String, IncludeTemplate>(64);

	protected HashMap<String, Long> includeFiles = new HashMap<String, Long>();

	protected String fileName;

	protected String html;

	protected Treex<TemplateFragment> tree;

	protected TreeNode<TemplateFragment> current;

	protected boolean sessionFlag = true;// 本页面是否产生session

	protected String contentType = null;// 本页面是否声明过contentType

	protected ITemplateManagerContext managerContext;

	protected char[] cs;

	protected int lastText = 0;

	List<Integer> lineNumList = new ArrayList<Integer>(128);

	private static class IncludeTemplate {
		long LastModified;
		TemplateParser IncludeTemplateParser;
		String FileName;
		boolean isFile;
	}

	public TemplateParser(ITemplateManagerContext context) {
		managerContext = context;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getContent() {
		return html;
	}

	public void setContent(String content) {
		html = content;
	}

	public void parseDirective(String str) {// 解析JSP指令
		str = str.trim();
		if (!str.startsWith("@")) {
			return;
		}
		str = str.substring(1).trim();
		if (str.indexOf(' ') < 0) {
			return;
		}
		String command = str.substring(0, str.indexOf(' '));
		str = str.substring(str.indexOf(' '));
		Mapx<String, Object> attrs = HtmlElement.parseAttr(str);
		if (command.equalsIgnoreCase("page")) {
			sessionFlag = !"false".equals(attrs.getString("session"));
			contentType = attrs.getString("contenttype");
		} else if (command.equalsIgnoreCase("include")) {
			String path = fileName.lastIndexOf("/") >= 0 ? fileName.substring(0, fileName.lastIndexOf("/")) : "";
			String file = attrs.getString("file");
			if (file.startsWith("/")) {
				file = file.substring(1);
			} else {
				while (true) {
					if (file.startsWith("./")) {
						file = file.substring(2);
					} else if (file.startsWith("../")) {
						path = path.lastIndexOf("/") >= 0 ? path.substring(0, path.lastIndexOf("/")) : "";
						file = file.substring(3);
					} else {
						break;
					}
				}
				file = path + "/" + file;
			}
			file = FileUtil.normalizePath(file);
			IncludeTemplate it = getIncludeTemplate(file, managerContext);
			includeFiles.put(file, it.LastModified);
			includeFiles.putAll(it.IncludeTemplateParser.getIncludeFiles());

			for (TreeNode<TemplateFragment> node : it.IncludeTemplateParser.getTree().getRoot().getChildren()) {
				current.addChildNode(node);
			}
		}
	}

	private static IncludeTemplate getIncludeTemplate(String file, ITemplateManagerContext managerContext) {
		String txt = null;
		if (!file.startsWith(Config.getContextRealPath())) {
			file = Config.getContextRealPath() + file;// 可能会存在UI下的文件和jar中的文件相互包含的问题
		}
		if (!cache.containsKey(file)) {
			IncludeTemplate it = new IncludeTemplate();
			it.FileName = file;
			File f = new File(file);
			if (f.exists()) {
				txt = FileUtil.readText(file);
				it.LastModified = f.lastModified();
				it.isFile = true;
			} else {
				if (file.startsWith(Config.getContextRealPath())) {
					file = file.substring(Config.getContextRealPath().length());
				}
				if (file.startsWith("/")) {
					file = file.substring(1);
				}
				UIResourceFile rf = new UIResourceFile(file);
				txt = rf.readText();
				it.LastModified = rf.lastModified();
			}
			if (file.startsWith(Config.getContextRealPath())) {
				file = file.substring(Config.getContextRealPath().length());
			}
			TemplateParser p = new TemplateParser(managerContext);
			p.setFileName(file);
			p.setContent(txt);
			p.parse();
			it.IncludeTemplateParser = p;
			cache.put(it.FileName, it);
			return it;
		} else {
			IncludeTemplate it = cache.get(file);
			if (it.isFile) {
				File f = new File(file);
				if (f.exists()) {
					if (f.lastModified() != it.LastModified) {
						TemplateParser p = new TemplateParser(managerContext);
						p.setFileName(file);
						p.setContent(FileUtil.readText(f));
						p.parse();
						it.IncludeTemplateParser = p;
						it.LastModified = f.lastModified();
					}
				} else {
					it.IncludeTemplateParser = null;
					it.LastModified = System.currentTimeMillis();
				}
			}
			return it;
		}
	}

	public void parse() {
		lineNumList.clear();
		lastText = 0;
		cs = html.toCharArray();
		tree = new Treex<TemplateFragment>();
		current = tree.getRoot();
		int index = 0;
		for (int i = 0; i < cs.length - 1; i++) {
			char c = cs[i];
			char next = cs[i + 1];
			if (c == '<') {
				if (next == '%') {// 脚本
					i = expectScript(i) - 1;
				} else if (next == '!' && XMLParser.expect(cs, i + 2, "--") == i + 4) {
					i = expectComment(i) - 1;
				} else if (next == '/') {// 可能是标签结束
					for (int j = i + 2; j < cs.length; j++) {
						if (cs[j] == '>' || XMLParser.isSpace(cs[j])) {
							index = j;
							break;
						}
					}
					if (index < 0) {// 后面没有空格和大于号，说明没有什么需要解析了
						break;
					}
					String name = html.substring(i + 2, index);
					if (name.indexOf(':') > 0) {
						i = expectTagEnd(i, name) - 1;
					}
				} else {// 可能是标签
					for (int j = i + 1; j < cs.length; j++) {
						if (XMLParser.isSpace(cs[j]) || cs[j] == '>' || cs[j] == '/') {
							index = j;
							break;
						}
					}
					if (index < 0) {// 后面没有空格和大于号，说明没有什么需要解析了
						break;
					}
					String name = html.substring(i + 1, index);
					if (name.indexOf(':') > 0) {
						i = expectTag(i, name) - 1;
					}
				}
			} else if (c == '$' && next == '{') {// 表达式
				i = expectExpression(i) - 1;
			} else if (c == '@' && next == '{') {// 国际化字符串
				i = expectI18NString(i) - 1;
			}
		}
		addText(lastText, cs.length);
	}

	int expectTag(int start, String name) {
		if (XMLParser.isInvalidName(cs, start + 1, start + 1 + name.length())) {
			return start + 1;
		}
		int index = start + name.length() + 1;
		String prefix = name.substring(0, name.indexOf(':'));
		String tagName = name.substring(name.indexOf(':') + 1);
		AbstractTag tag = managerContext.getTag(prefix, tagName);
		if (tag == null) {
			return index;
		}
		index = XMLParser.ignoreSpace(cs, index);
		addText(lastText, start);

		TemplateFragment tf = new TemplateFragment();
		tf.Type = TemplateFragment.FRAGMENT_TAG;
		tf.StartCharIndex = start;
		tf.StartLineNo = XMLParser.getLineNum(lineNumList, cs, start);
		tf.Attributes = new CaseIgnoreMapx<String, String>();
		tf.TagPrefix = prefix;
		tf.TagName = tagName;
		current = current.addChild(tf);

		start = index;

		// parse attributes
		while (true) {
			ElementAttribute ea = HtmlParser.expectAttribute(cs, start, lineNumList);
			if (ea == null) {
				break;
			}
			if (!tag.hasAttribute(ea.Name)) {
				String message = StringFormat.format("Tag <?:?> has not attribute '?', line number is ?", prefix, tagName, ea.Name,
						tf.StartLineNo);
				throw new TemplateCompileException(message);
			}
			tf.Attributes.put(ea.Name, ea.Value);
			start = ea.EndCharIndex;
		}

		if ((index = XMLParser.expect(cs, start, "/>")) > 0) {
			tf.EndCharIndex = index;
			tf.TagSource = html.substring(tf.StartCharIndex, tf.EndCharIndex);
			current = current.getParent();
			return lastText = index;
		}
		index = XMLParser.expect(cs, start, ">");
		tf.EndCharIndex = index;
		if (index < 0) {
			throw new HtmlParseException("Tag not complete correctly: <" + tag + ", line number is "
					+ XMLParser.getLineNum(lineNumList, cs, start));
		}
		return lastText = index;
	}

	/**
	 * 尝试查找标签结束
	 */
	int expectTagEnd(int start, String name) {
		String prefix = name.substring(0, name.indexOf(':'));
		String tagName = name.substring(name.indexOf(':') + 1);
		if (current.getData() == null || current.getData().TagPrefix == null || !current.getData().TagPrefix.equalsIgnoreCase(prefix)
				|| !current.getData().TagName.equals(tagName)) {
			return start + 2;
		}
		int index = XMLParser.indexOf(cs, '>', start + 2);
		if (index < 0) {
			throw new HtmlParseException("Tag not complete correctly, line number is " + XMLParser.getLineNum(lineNumList, cs, start));
		}
		addText(lastText, start);
		TemplateFragment tf = current.getData();
		tf.FragmentText = html.substring(tf.EndCharIndex, start);
		tf.EndCharIndex = index + 1;
		tf.TagSource = html.substring(tf.StartCharIndex, tf.EndCharIndex);
		current = current.getParent();
		return lastText = index + 1;
	}

	int expectScript(int start) {
		int index = HtmlParser.find(cs, start, "%>");
		if (index > 0) {
			addText(lastText, start);
			TemplateFragment tf = new TemplateFragment();
			tf.FragmentText = html.substring(start + 2, index);
			tf.Type = TemplateFragment.FRAGMENT_SCRIPT;
			tf.StartLineNo = XMLParser.getLineNum(lineNumList, cs, start);
			tf.StartCharIndex = start;
			tf.EndCharIndex = index + 2;
			current.addChild(tf);
			if (XMLParser.expect(cs, start + 2, "@") > 0) {
				parseDirective(tf.FragmentText);
			}
			return lastText = index + 2;
		} else {
			throw new TemplateCompileException("Template script not closed, line number is " + XMLParser.getLineNum(lineNumList, cs, start));
		}
	}

	int expectExpression(int start) {
		char literal = 0;
		for (int j = start + 2; j < cs.length; j++) {
			char h = cs[j];
			if (h == '\'' || h == '\"') {
				if (cs[j - 1] == '\\') {// 转义
					continue;
				}
				if (literal == 0) {
					literal = h;
				} else if (literal == h) {
					literal = 0;
				}
			} else if (h == '}' && cs[j - 1] != '\\') {
				if (literal == 0) {
					addText(lastText, start);
					TemplateFragment tf = new TemplateFragment();
					tf.FragmentText = html.substring(start, j + 1);
					tf.Type = TemplateFragment.FRAGMENT_EXPRESSION;
					tf.StartLineNo = XMLParser.getLineNum(lineNumList, cs, start);
					tf.StartCharIndex = start;
					tf.EndCharIndex = j + 1;
					current.addChild(tf);
					return lastText = j + 1;
				}
			} else if (h == '\n') {
				throw new TemplateCompileException("Template expression not closed, line number is "
						+ XMLParser.getLineNum(lineNumList, cs, start));
			}
		}
		return -1;
	}

	int expectI18NString(int start) {
		for (int j = start + 2; j < cs.length; j++) {
			if (cs[j] == '}') {
				addText(lastText, start);
				TemplateFragment tf = new TemplateFragment();
				tf.FragmentText = html.substring(start, j + 1);
				tf.Type = TemplateFragment.FRAGMENT_EXPRESSION;
				tf.StartLineNo = XMLParser.getLineNum(lineNumList, cs, start);
				tf.StartCharIndex = start;
				tf.EndCharIndex = j + 1;
				current.addChild(tf);
				return lastText = j + 1;
			}
		}
		throw new TemplateCompileException("Template i18N string not closed, line number is "
				+ XMLParser.getLineNum(lineNumList, cs, start));
	}

	int expectComment(int pos) {
		int i = XMLParser.indexOf(cs, "-->", pos + 4);
		if (i > 0) {
			return i + 3;// 不需要进行处理，直接跳过，注释本身会并入到上一个纯HTML段中
		} else {
			throw new HtmlParseException("Template comment not closed, line number is " + XMLParser.getLineNum(lineNumList, cs, pos));
		}
	}

	void addText(int start, int end) {
		if (start == end) {
			return;
		}
		TemplateFragment tf = new TemplateFragment();
		tf.FragmentText = html.substring(start, end);
		tf.Type = TemplateFragment.FRAGMENT_HTML;
		tf.StartLineNo = XMLParser.getLineNum(lineNumList, cs, start);
		tf.StartCharIndex = start;
		tf.EndCharIndex = end;
		current.addChild(tf);
	}

	public Treex<TemplateFragment> getTree() {
		return tree;
	}

	public boolean isSessionFlag() {
		return sessionFlag;
	}

	public String getContentType() {
		return contentType;
	}

	public HashMap<String, Long> getIncludeFiles() {
		return includeFiles;
	}
}
