package com.zving.framework.template;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.zving.framework.collection.Treex;
import com.zving.framework.collection.Treex.TreeNode;
import com.zving.framework.collection.Treex.TreeNodeList;
import com.zving.framework.template.command.ExpressionCommand;
import com.zving.framework.template.command.PrintCommand;
import com.zving.framework.template.command.TagCommand;
import com.zving.framework.template.exception.TemplateCompileException;
import com.zving.framework.utility.Errorx;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 模板编译器
 * 
 * @Author 王育春
 * @Date 2010-5-30
 * @Mail wyuch@zving.com
 */
public class TemplateCompiler {
	protected String fileName;
	protected long lastModified;
	protected TemplateExecutor executor;
	protected ArrayList<ITemplateCommand> commandList = new ArrayList<ITemplateCommand>();
	protected TemplateParser parser;
	protected ITemplateManagerContext managerContext;

	public TemplateCompiler(ITemplateManagerContext managerContext) {
		this.managerContext = managerContext;
		parser = new TemplateParser(managerContext);
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * 从文件中编译
	 * 
	 * @throws TemplateCompileException
	 * @throws NotPrecompileException
	 */
	public void compile(String fileName) throws FileNotFoundException, TemplateCompileException {
		fileName = FileUtil.normalizePath(fileName);
		if (!new File(fileName).exists()) {
			throw new FileNotFoundException("File not found:" + fileName);
		}
		lastModified = new File(fileName).lastModified();
		this.fileName = fileName;
		compileSource(FileUtil.readText(fileName));
	}

	/**
	 * 从源代码字符串编译
	 * 
	 * @throws NotPrecompileException
	 */
	public void compileSource(String source) throws TemplateCompileException {
		long start = System.currentTimeMillis();
		parser.setContent(source);
		parser.setFileName(fileName);
		parser.parse();
		if (Errorx.hasError()) {
			throw new TemplateCompileException(Errorx.getAllMessage());
		}

		// 遍历树，生成待编译的文件
		compile(parser);
		if (Errorx.hasError()) {
			throw new TemplateCompileException(Errorx.getAllMessage());
		}
		if (fileName != null) {
			LogUtil.info("Compile " + fileName + " cost " + (System.currentTimeMillis() - start) + " ms.");
		}
	}

	public void compile(TemplateParser parser) {
		this.parser = parser;
		Treex<TemplateFragment> tree = parser.getTree();
		if (Errorx.hasError()) {
			return;
		}
		if (lastModified == 0) {
			lastModified = System.currentTimeMillis();// 如果是从字符串编译，则将当前时间设为最后修改时间
		}
		executor = new TemplateExecutor(managerContext);
		executor.fileName = fileName;
		executor.lastModified = lastModified;
		executor.sessionFlag = parser.isSessionFlag();
		executor.contentType = parser.getContentType();
		executor.includeFiles = parser.getIncludeFiles();

		// execute()方法
		TreeNodeList<TemplateFragment> list = tree.getRoot().getChildren();
		for (int i = 0; i < list.size(); i++) {
			compileNode(list.get(i), commandList, executor.tree.getRoot());
		}
		executor.init(commandList);
	}

	protected void compileNode(TreeNode<TemplateFragment> node, ArrayList<ITemplateCommand> parentList, TreeNode<AbstractTag> parentTagNode) {
		TemplateFragment tf = node.getData();
		if (tf.Type == TemplateFragment.FRAGMENT_HTML) {
			parentList.add(new PrintCommand(tf.FragmentText));
		} else if (tf.Type == TemplateFragment.FRAGMENT_EXPRESSION) {
			parentList.add(new ExpressionCommand(tf.FragmentText));
		} else if (tf.Type == TemplateFragment.FRAGMENT_SCRIPT) {
			if (tf.FragmentText.trim().startsWith("@")) {
				return;
			}
			if (!tf.FragmentText.startsWith("--") || !tf.FragmentText.endsWith("--")) {
				// 除<%-- --%>外的其他脚本原样输出
				parentList.add(new PrintCommand("<%" + tf.FragmentText + "%>"));
			}
		} else if (tf.Type == TemplateFragment.FRAGMENT_TAG) {
			try {
				compileTag(node, parentList, parentTagNode);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void compileTag(TreeNode<TemplateFragment> node, ArrayList<ITemplateCommand> parentList, TreeNode<AbstractTag> parentTagNode)
			throws Exception {
		TemplateFragment tf = node.getData();
		AbstractTag tag = managerContext.createNewTagInstance(tf.TagPrefix, tf.TagName);
		if (fileName != null) {
			int lastIndex = fileName.lastIndexOf("/");
			tag.setUrlFile(lastIndex == -1 ? fileName : fileName.substring(lastIndex + 1));
		}
		tag.setStartLineNo(tf.StartLineNo);
		tag.setStartCharIndex(tf.StartCharIndex);
		for (String k : tf.Attributes.keySet()) {
			tag.setAttribute(k, tf.Attributes.get(k));
		}
		TreeNode<AbstractTag> tagNode = parentTagNode.addChild(tag);
		tag.setParent(parentTagNode.getData());
		ArrayList<ITemplateCommand> list = new ArrayList<ITemplateCommand>();
		boolean hasBody = StringUtil.isNotEmpty(tf.FragmentText);
		if (hasBody) {
			for (TreeNode<TemplateFragment> child : node.getChildren()) {
				compileNode(child, list, tagNode);
			}
		}
		TagCommand invoke = new TagCommand(tag, list, node.getLevel(), hasBody);
		if (tag.isKeepTagSource()) {
			tag.tagSource = tf.TagSource;// 如果标签在被包含的文件里，则使用parser.getContent.substring();会取不到标签源代码
			if (hasBody) {
				tag.tagBodySource = tf.FragmentText;
			}
		}
		tag.afterCompile(invoke, executor);// 以便于标签可以对编译结果进行干涉
		parentList.add(invoke);
	}

	public TemplateExecutor getExecutor() {
		return executor;
	}
}
