package com.zving.framework.ui.util;

import com.zving.framework.template.TemplateCompiler;
import com.zving.framework.template.TemplateExecutor;
import com.zving.framework.ui.zhtml.ZhtmlExecuteContext;
import com.zving.framework.ui.zhtml.ZhtmlManagerContext;

/**
 * Zhtml执行工具类，用于在JAVA中调用Zhtml页面。<br>
 * 注意：如果未调用setExecuteContext()指定上下文，<br>
 * 则会新建一个上下文实例，并且从Current中查找当前请求相关的变量。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-1-2
 */
public class ZhtmlExecutor {// NO_UCD
	String fileName;
	String source;
	ZhtmlExecuteContext executeContext;
	TemplateExecutor te;

	public void compile() {
		TemplateCompiler tc = new TemplateCompiler(ZhtmlManagerContext.getInstance());
		if (fileName == null) {
			tc.setFileName(" Source for ZhtmlExecutor");
		} else {
			tc.setFileName(fileName);
		}
		tc.compileSource(source);
		te = tc.getExecutor();
	}

	/**
	 * 设置上下文变量
	 */
	public void setVariable(String key, Object value) {
		getExecuteContext();
		executeContext.addRootVariable(key, value);
	}

	/**
	 * 执行zhtml源代码并输出执行结果
	 */
	public String execute() {
		if (te == null) {
			compile();
		}
		getExecuteContext();
		te.execute(executeContext);
		return executeContext.getOut().getResult();
	}

	/**
	 * 返回模板的源代码
	 */
	public String getSource() {
		return source;
	}

	/**
	 * 设置模板的源代码
	 */
	public void setSource(String content) {
		source = content;
	}

	/**
	 * 返回模板对应的文件名。
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * 要执行的模板对应的文件名，文件名相对于应用的根目录。<br>
	 * 注意：此处设置文件名只是供模板中的标签能够处理相对路径，<br>
	 * 并不代表从这个文件名中读取内容，内容需要单独调用setContent()方法置入值。
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * 返回zhtml执行的上下文
	 */
	public ZhtmlExecuteContext getExecuteContext() {
		if (executeContext == null) {
			executeContext = new ZhtmlExecuteContext(ZhtmlManagerContext.getInstance(), null, null);
		}
		return executeContext;
	}

	/**
	 * 设置zhtml执行的上下文 ，如果未调用setExecuteContext()指定上下文，<br>
	 * 则会新建一个上下文实例，并且从Current中查找当前请求相关的变量。
	 */
	public void setExecuteContext(ZhtmlExecuteContext executeContext) {
		this.executeContext = executeContext;
	}
}
