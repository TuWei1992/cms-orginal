package com.zving.framework.extend;

/**
 * 扩展行为接口<br>
 * 
 * @date 2009-11-7 <br>
 * @author 王育春 <br>
 * @email wangyc@zving.com <br>
 */
public interface IExtendAction {
	/**
	 * 扩展逻辑
	 */
	public Object execute(Object[] args) throws ExtendException;

	/**
	 * 是否可用
	 */
	public boolean isUsable();
}
