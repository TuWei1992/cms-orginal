package com.zving.framework.template;

import com.zving.framework.extend.IExtendItem;

/**
 * 模板源代码处理器接口，在模板编译前执行。<br>
 * 可以在解析模板之前先处理模板源代码中的资源路径。
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-10-31
 */
public interface ITemplateSourceProcessor extends IExtendItem {
	public void process(TemplateParser parser);
}
