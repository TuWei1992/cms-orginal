package com.zving.framework.template;

import java.io.PrintWriter;
import java.util.Map;

import com.zving.framework.User;
import com.zving.framework.collection.CaseIgnoreMapx;
import com.zving.framework.collection.Mapx;
import com.zving.framework.core.bean.BeanManager;
import com.zving.framework.core.bean.BeanProperty;
import com.zving.framework.data.DataRow;
import com.zving.framework.expression.ExpressionException;
import com.zving.framework.expression.IFunctionMapper;
import com.zving.framework.expression.ITagData;
import com.zving.framework.expression.IVariableResolver;
import com.zving.framework.orm.DAO;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.Primitives;

/**
 * 模板执行上下文虚拟类
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-9
 */
public abstract class AbstractExecuteContext implements Cloneable, IVariableResolver {
	protected Mapx<String, Object> variables = new CaseIgnoreMapx<String, Object>();
	protected AbstractTag currentTag;
	protected int pageTotal;
	protected int pageSize;
	protected int pageIndex;
	protected long startTime;
	protected TemplateWriter out;
	protected TemplateExecutor executor;
	protected String language;

	public AbstractExecuteContext() {
		startTime = System.currentTimeMillis();
		variables.put("TimeMillis", startTime);// 让每个页面都可以获得当前时间
	}

	@Override
	public Object resolveVariable(String var) {
		if (var == null) {
			return null;
		}
		// 如果有标签先从标签变量中求值
		if (currentTag != null) {
			if ("list".equalsIgnoreCase(var) || "this".equalsIgnoreCase(var)) {// list/this变量恒定返回当前标签上下文数据
				return currentTag.getTagContextData();
			} else if ("parent".equalsIgnoreCase(var)) {// parent变量恒定返回父标签上下文数据
				return currentTag.getParent() == null ? null : currentTag.getParent().getTagContextData();
			}
			AbstractTag tag = currentTag;
			while (tag != null) {
				Object v = tag.getTagContextData().getValue(var);
				if (v != null) {
					return v;
				} else if (tag.getTagContextData().isFound()) {
					return null;
				}
				tag = tag.getParent();
			}
		}
		return resolveGlobalVariable(var);
	}

	/**
	 * 解析全局变量
	 */
	public abstract Object resolveGlobalVariable(String holder);

	public Object evalExpression(String expression) throws ExpressionException {
		return evalExpression(expression, Object.class);
	}

	// 如果只是简单的变量查找，则为了提高性能特别处理
	@SuppressWarnings("rawtypes")
	private Object evalQuickly(String expression) {
		if (expression.indexOf("${") < 0) {
			int i = expression.indexOf('.');
			if (i > 0) {
				if (expression.indexOf('.', i + 1) == -1) {// 只有一级
					String prefix = expression.substring(0, i);
					String var = expression.substring(i + 1);
					Object v = this.resolveVariable(prefix);
					if (v == null) {
						return null;
					}
					if (v instanceof Map) {
						return ((Map) v).get(var);
					} else if (v instanceof DataRow) {
						return ((DataRow) v).get(var);
					} else if (v instanceof DAO) {
						return ((DAO) v).getV(var);
					} else if (v instanceof ITagData) {
						return ((ITagData) v).getValue(var);
					} else {
						try {
							BeanProperty p = BeanManager.getBeanDescription(v.getClass()).getProperty(var);
							if (p != null) {
								return p.read(v);
							}
						} catch (Exception e) {
						}
					}
				}
			} else {
				return this.resolveVariable(expression);
			}
		}
		try {
			return evalExpression(expression, Object.class);
		} catch (ExpressionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Object evalExpression(String expression, Class<?> expectedClass) throws ExpressionException {
		if (expression.indexOf("${") < 0) {
			expression = "${" + expression + "}";
		}
		IFunctionMapper fm = getManagerContext().getFunctionMapper();
		return getManagerContext().getEvaluator().evaluate(expression, expectedClass, this, fm);
	}

	public int evalInt(String expression) {
		Object obj = evalQuickly(expression);
		return Primitives.getInteger(obj);
	}

	public long evalLong(String expression) {
		Object obj = evalQuickly(expression);
		return Primitives.getLong(obj);
	}

	public String eval(String expression) {
		Object obj = evalQuickly(expression);
		return obj == null ? null : String.valueOf(obj);
	}

	@SuppressWarnings("unchecked")
	public Mapx<String, Object> evalMap(String expression) {
		try {
			Object obj = evalExpression(expression);
			if (obj != null && obj instanceof Map) {
				return (Mapx<String, Object>) obj;
			}
			return null;
		} catch (ExpressionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void addDataVariable(String key, Object value) {
		if (currentTag == null) {
			addRootVariable(key, value);
		} else {
			currentTag.setVariable(key, value);
		}
	}

	public TemplateWriter getOut() {
		return out;
	}

	public AbstractTag getCurrentTag() {
		return currentTag;
	}

	public void setCurrentTag(AbstractTag currentTag) {
		this.currentTag = currentTag;
	}

	public void setOut(TemplateWriter writer) {
		out = writer;
	}

	@Deprecated
	public void setRootWriter(TemplateWriter writer) {
		out = writer;
	}

	public void setOut(PrintWriter writer) {
		out = new TemplateWriter(writer);
	}

	@Deprecated
	public void setRootWriter(PrintWriter writer) {
		out = new TemplateWriter(writer);
	}

	public int getPageTotal() {
		return pageTotal;
	}

	public void setPageTotal(int pageTotal) {
		this.pageTotal = pageTotal;
		addRootVariable("PageTotal", pageTotal);
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
		addRootVariable("PageSize", pageSize);
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
		addRootVariable("PageIndex", pageIndex);
	}

	/**
	 * 获取模板本次执行消耗的时间
	 */
	public long getExecuteCostTime() {
		return System.currentTimeMillis() - startTime;
	}

	/**
	 * 返回包含子模板时子模板使用的执行上下文
	 */
	public abstract AbstractExecuteContext getIncludeContext();

	/**
	 * 返回模板管理上下文
	 */
	public abstract ITemplateManagerContext getManagerContext();

	public void addRootVariable(String name, Object value) {
		variables.put(name, value);
	}

	public Object getRootVariable(String key) {
		return variables.get(key);
	}

	public void setAttribute(String name, Object value) {
		variables.put(name, value);
	}

	public Object getAttribute(String key) {
		return variables.get(key);
	}

	public Object removeAttribute(String key) {
		return variables.remove(key);
	}

	public Object removeRootVariable(String key) {// NO_UCD
		return variables.remove(key);
	}

	public TemplateExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(TemplateExecutor executor) {
		this.executor = executor;
	}

	/**
	 * @return 模板执行时使用的语言
	 */
	public String getLanguage() {
		if (language == null) {
			language = User.getLanguage();
		}
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	/*-----以下是为了兼容旧的CMS模板写法-----*/
	public static final String PREVIEW_ATTR = "_ZVING_PREVIEW_ATTR";

	public static final String INTERACTIVE_ATTR = "_ZVING_INTERACTIVE_ATTR";

	/**
	 * 当前请求是否是预览
	 */
	public boolean isPreview() {
		return ObjectUtil.isTrue(getRootVariable(PREVIEW_ATTR));
	}

	/**
	 * 当前请求是否是互动页面
	 */
	public boolean isInteractive() {
		return ObjectUtil.isTrue(getRootVariable(INTERACTIVE_ATTR));
	}

	/**
	 * 设置当前请求是否是预览
	 */
	public void setPreview(boolean flag) {
		addRootVariable(PREVIEW_ATTR, flag);
	}

	/**
	 * 设置当前请求是否是互动页面
	 */
	protected void setInteractive(boolean flag) {
		addRootVariable(INTERACTIVE_ATTR, flag);
	}
	/*-----兼容结束-----*/
}
