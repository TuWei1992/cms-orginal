package com.zving.cxdata.tag;

import com.zving.cxdata.bl.ResourceURLBL;
import com.zving.framework.expression.AbstractFunction;
import com.zving.framework.expression.IVariableResolver;

public class CMSDynamicResouceURL extends AbstractFunction {
	public Object execute(IVariableResolver resolver, Object... args) {
		return ResourceURLBL.getCMSDynamicResourceURL(resolver, args);
	}

	public Class<?>[] getArgumentTypes() {
		return new Class[] { String.class, String.class, Boolean.class};
	}

	public String getFunctionPrefix() {
		return "";
	}

	public String getFunctionName() {
		return "durl";
	}
}