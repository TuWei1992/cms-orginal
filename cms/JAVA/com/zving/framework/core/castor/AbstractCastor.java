package com.zving.framework.core.castor;

/**
 * 类型转换器虚拟类
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-5
 */
public abstract class AbstractCastor implements ICastor {

	@Override
	public String getExtendItemID() {
		return this.getClass().getName();
	}

	@Override
	public String getExtendItemName() {
		return this.getClass().getName();
	}

}
