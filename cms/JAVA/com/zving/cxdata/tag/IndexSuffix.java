package com.zving.cxdata.tag;

import com.zving.framework.expression.AbstractFunction;
import com.zving.framework.expression.ExpressionException;
import com.zving.framework.expression.IVariableResolver;
import com.zving.framework.utility.StringUtil;

public class IndexSuffix extends AbstractFunction {

	@Override
	public String getFunctionPrefix() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String getFunctionName() {
		// TODO Auto-generated method stub
		return "indexSuffix";
	}

	@Override
	public Class<?>[] getArgumentTypes() {
		// TODO Auto-generated method stub
		return new Class[]{String.class};
	}

	@Override
	public Object execute(IVariableResolver resolver, Object... args)
			throws ExpressionException {
		String link = "";
		if (args.length > 0) {
			link = (String) args[0];
		}
		if (StringUtil.isNotEmpty(link)) {
			if (link.endsWith("/")) {
				link += "index.shtml";
			}
		}
		return link;
	}

}
