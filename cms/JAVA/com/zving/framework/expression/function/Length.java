package com.zving.framework.expression.function;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import com.zving.framework.expression.AbstractFunction;
import com.zving.framework.expression.ExpressionException;
import com.zving.framework.expression.IVariableResolver;

/**
 * 返回对象的长度。<br>
 * 如果是字符串，则调用其length方法;<br>
 * 如果是数组，则返回其length属性;<br>
 * 如果是集合对象，则调用其size方法;<br>
 * 如果是枚举和可遍历的对象，则通过遍历其元素的方式计算其中的元素个数并返回之。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-22
 */
public class Length extends AbstractFunction {

	@Override
	public Object execute(IVariableResolver resolver, Object... args) throws ExpressionException {
		Object obj = args[0];
		if (obj == null) {
			return 0;
		}

		if (obj instanceof String) {
			return ((String) obj).length();
		}
		if (obj.getClass().isArray()) {
			return Array.getLength(obj);
		}
		if (obj instanceof Collection) {
			return ((Collection<?>) obj).size();
		}
		if (obj instanceof Map) {
			return ((Map<?, ?>) obj).size();
		}
		int count = 0;
		if (obj instanceof Iterator) {
			Iterator<?> iter = (Iterator<?>) obj;
			count = 0;
			while (iter.hasNext()) {
				count++;
				iter.next();
			}
			return count;
		}
		if (obj instanceof Enumeration) {
			Enumeration<?> enum_ = (Enumeration<?>) obj;
			count = 0;
			while (enum_.hasMoreElements()) {
				count++;
				enum_.nextElement();
			}
			return count;
		}
		throw new ExpressionException("Invalid length property");
	}

	@Override
	public String getFunctionPrefix() {
		return "";
	}

	@Override
	public Class<?>[] getArgumentTypes() {
		return AbstractFunction.Arg_Object;
	}

	@Override
	public String getFunctionName() {
		return "length";
	}
}
