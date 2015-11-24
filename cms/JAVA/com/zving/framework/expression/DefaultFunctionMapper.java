package com.zving.framework.expression;

import com.zving.framework.collection.CaseIgnoreMapx;
import com.zving.framework.expression.function.CharWidth;
import com.zving.framework.expression.function.ClearHtmlTag;
import com.zving.framework.expression.function.Contains;
import com.zving.framework.expression.function.ContainsIgnoreCase;
import com.zving.framework.expression.function.EndsWith;
import com.zving.framework.expression.function.EscapeXml;
import com.zving.framework.expression.function.Eval;
import com.zving.framework.expression.function.Format;
import com.zving.framework.expression.function.IndexOf;
import com.zving.framework.expression.function.JavaEncode;
import com.zving.framework.expression.function.Join;
import com.zving.framework.expression.function.LastIndexOf;
import com.zving.framework.expression.function.Length;
import com.zving.framework.expression.function.Match;
import com.zving.framework.expression.function.Now;
import com.zving.framework.expression.function.Replace;
import com.zving.framework.expression.function.Split;
import com.zving.framework.expression.function.StartsWith;
import com.zving.framework.expression.function.Substring;
import com.zving.framework.expression.function.SubstringAfter;
import com.zving.framework.expression.function.SubstringBefore;
import com.zving.framework.expression.function.ToLowerCase;
import com.zving.framework.expression.function.ToUpperCase;
import com.zving.framework.expression.function.Trim;
import com.zving.framework.expression.function.UrlEncode;

/**
 * 默认函数查找器，不区分函数名大小写，已经内置了常用函数
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-22
 */
public class DefaultFunctionMapper implements IFunctionMapper {
	private static DefaultFunctionMapper instance = new DefaultFunctionMapper();

	private CaseIgnoreMapx<String, CaseIgnoreMapx<String, IFunction>> all = new CaseIgnoreMapx<String, CaseIgnoreMapx<String, IFunction>>(
			true);

	/**
	 * @return 唯一实例
	 */
	public static DefaultFunctionMapper getInstance() {
		return instance;
	}

	private DefaultFunctionMapper() {
		registerFunction(new CharWidth());
		registerFunction(new ClearHtmlTag());
		registerFunction(new Contains());
		registerFunction(new ContainsIgnoreCase());
		registerFunction(new EndsWith());
		registerFunction(new StartsWith());
		registerFunction(new EscapeXml());
		registerFunction(new JavaEncode());
		registerFunction(new UrlEncode());
		registerFunction(new Format());
		registerFunction(new IndexOf());
		registerFunction(new Join());
		registerFunction(new Length());
		registerFunction(new Replace());
		registerFunction(new Split());
		registerFunction(new Substring());
		registerFunction(new SubstringAfter());
		registerFunction(new SubstringBefore());
		registerFunction(new ToLowerCase());
		registerFunction(new ToUpperCase());
		registerFunction(new Trim());
		registerFunction(new Match());
		registerFunction(new LastIndexOf());
		registerFunction(new Now());
		registerFunction(new Eval());
	}

	@Override
	public IFunction resolveFunction(String prefix, String name) {
		if (prefix == null) {
			prefix = "";
		}
		CaseIgnoreMapx<String, IFunction> map = all.get(prefix);
		if (map == null) {
			return null;
		}
		return map.get(name);
	}

	@Override
	public void registerFunction(IFunction f) {
		String prefix = f.getFunctionPrefix();
		if (prefix == null) {
			prefix = "";
		}
		CaseIgnoreMapx<String, IFunction> map = all.get(prefix);
		if (map == null) {
			map = new CaseIgnoreMapx<String, IFunction>(true);
			all.put(prefix, map);
		}
		map.put(f.getFunctionName(), f);
	}

}
