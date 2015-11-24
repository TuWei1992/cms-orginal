package com.zving.framework.expression;

import java.util.Map;


/**
 * Map变量查找器，将Map中的键当成变量
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-4
 */
public class MapVariableResolver implements IVariableResolver {// NO_UCD
	private Map<?, ?> map;

	/**
	 * 构造器
	 * 
	 * @param map 变量所在的Map
	 */
	public MapVariableResolver(Map<?, ?> map) {
		this.map = map;
	}

	@Override
	public Object resolveVariable(String varName) throws ExpressionException {
		return map.get(varName);
	}
}
