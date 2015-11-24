package com.zving.framework.ui.weaver;

import com.zving.framework.extend.IExtendItem;

/**
 * Zhtml文件织入器，用于向zhtml文件中织入代码。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-10-17
 */
public interface IZhtmlWeaver extends IExtendItem {
	/**
	 * 织入片段到zhtml文件
	 */
	public void weave(ZhtmlWeaveHelper w);

	/**
	 * 表示待织入的目标Zhtml文件,可以是简易正则表达式
	 */
	public String getTarget();
}
