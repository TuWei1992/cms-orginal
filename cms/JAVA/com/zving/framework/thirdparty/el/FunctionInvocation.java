/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package com.zving.framework.thirdparty.el;

import java.util.Iterator;
import java.util.List;

import com.zving.framework.expression.ExpressionException;
import com.zving.framework.expression.IFunction;
import com.zving.framework.expression.IFunctionMapper;
import com.zving.framework.expression.IVariableResolver;

/**
 * <p>
 * Represents a function call.
 * </p>
 * 
 * @author Shawn Bayern (in the style of Nathan's other classes)
 **/

public class FunctionInvocation extends Expression {
	// -------------------------------------
	// Properties
	// -------------------------------------
	// property index

	private String functionName;
	private List<Expression> argumentList;

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String f) {
		functionName = f;
	}

	public List<Expression> getArgumentList() {
		return argumentList;
	}

	public void setArgumentList(List<Expression> l) {
		argumentList = l;
	}

	public FunctionInvocation(String functionName, List<Expression> argumentList) {
		this.functionName = functionName;
		this.argumentList = argumentList;
	}

	/**
	 * Returns the expression in the expression language syntax
	 **/
	@Override
	public String getExpressionString() {
		StringBuffer b = new StringBuffer();
		b.append(functionName);
		b.append("(");
		Iterator<Expression> i = argumentList.iterator();
		while (i.hasNext()) {
			b.append(i.next().getExpressionString());
			if (i.hasNext()) {
				b.append(", ");
			}
		}
		b.append(")");
		return b.toString();
	}

	// -------------------------------------
	/**
	 * Evaluates by looking up the name in the VariableResolver
	 **/
	@Override
	public Object evaluate(IVariableResolver pResolver, IFunctionMapper functions, Logger pLogger) throws ExpressionException {

		// if the Map is null, then the function is invalid
		if (functions == null) {
			pLogger.logError(Constants.UNKNOWN_FUNCTION, functionName);
		}

		// normalize function name
		String prefix = null;
		String localName = null;
		int index = functionName.indexOf(':');
		if (index == -1) {
			prefix = "";
			localName = functionName;
		} else {
			prefix = functionName.substring(0, index);
			localName = functionName.substring(index + 1);
		}

		// ensure that the function's name is mapped
		IFunction target = functions.resolveFunction(prefix, localName);
		if (target == null) {
			pLogger.logError(Constants.UNKNOWN_FUNCTION, functionName);
		}

		Class<?>[] types = target.getArgumentTypes();
		Object[] arguments = new Object[argumentList.size()];
		for (int i = 0; i < types.length && i < arguments.length; i++) {
			arguments[i] = argumentList.get(i).evaluate(pResolver, functions, pLogger);
			arguments[i] = Coercions.coerce(arguments[i], types[i], pLogger);
		}
		if (types.length < arguments.length) {// 动态参数的情况
			for (int i = types.length; i < arguments.length; i++) {
				arguments[i] = argumentList.get(i).evaluate(pResolver, functions, pLogger);
			}
		}

		// finally, invoke the target method, which we know to be static
		return target.execute(pResolver, arguments);
	}
}
