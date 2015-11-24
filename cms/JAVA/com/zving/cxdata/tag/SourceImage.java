package com.zving.cxdata.tag;

import com.meidusa.toolkit.common.util.StringUtil;
import com.zving.framework.expression.AbstractFunction;
import com.zving.framework.expression.ExpressionException;
import com.zving.framework.expression.IVariableResolver;

public class SourceImage extends AbstractFunction {

	@Override
	public String getFunctionPrefix() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String getFunctionName() {
		// TODO Auto-generated method stub
		return "sourceImage";
	}

	@Override
	public Class<?>[] getArgumentTypes() {
		// TODO Auto-generated method stub
		return new Class[] { String.class};
	}

	@Override
	public Object execute(IVariableResolver resolver, Object... args) throws ExpressionException {
		String path = (String)args[0];
		if (StringUtil.isEmpty(path)) {
			return path;
		}
		int file = path.lastIndexOf("/");
		if (file < 0) {
			file = 0;
		}
		int size = path.indexOf("_", file);
		int suffixPos = path.lastIndexOf(".");
		String suffix = "";
		if (suffixPos >= 0 && file < suffixPos) {
			suffix = path.substring(suffixPos);
		}
		if (size >= 0) {
			return path.substring(0, size) + suffix;
		} else {
			return path;
		}
	}

}
