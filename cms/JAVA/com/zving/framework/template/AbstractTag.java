package com.zving.framework.template;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import com.zving.framework.collection.CacheMapx;
import com.zving.framework.collection.CaseIgnoreMapx;
import com.zving.framework.collection.Mapx;
import com.zving.framework.expression.ExpressionException;
import com.zving.framework.extend.IExtendItem;
import com.zving.framework.i18n.Lang;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.template.command.TagCommand;
import com.zving.framework.template.exception.TemplateCompileException;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.utility.Primitives;
import com.zving.framework.utility.StringUtil;

/**
 * 标签虚拟类，所有标签都必须继承这个类。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-5
 */
public abstract class AbstractTag implements IExtendItem, Cloneable {
	private static CacheMapx<String, CaseIgnoreMapx<String, Method>> AllTagMethods = new CacheMapx<String, CaseIgnoreMapx<String, Method>>();

	private static ReentrantLock lock = new ReentrantLock();
	/**
	 * doStartTag和doEndTag都可用的返回值，要求中止页面执行
	 */
	public final static int SKIP_PAGE = 5;
	/**
	 * doStartTag可用的返回值，要求跳过标签体
	 */
	public final static int SKIP_BODY = 0;
	/**
	 * doStartTag方法可用的返回值，要求执行标签体并将执行结果放到一个缓冲区（不直接输出以便于在doEndTag中对执行结果进行处理）
	 */
	public final static int EVAL_BODY_BUFFERED = 2;
	/**
	 * doStartTag方法可用的返回值，要求执行标签体并直接将执行结果输出（不能在doEndTag中对执行结果进行处理）
	 */
	public final static int EVAL_BODY_INCLUDE = 1;
	/**
	 * doEndTag方法可用的返回值，要求再次执行标签体
	 */
	public final static int EVAL_BODY_AGAIN = 2;
	/**
	 * doEndTag方法可用的返回值，要求继续执行页面
	 */
	public final static int EVAL_PAGE = 6;
	/**
	 * 执行的上下文，等同于context
	 */
	protected AbstractExecuteContext pageContext;
	/**
	 * 执行的上下文
	 */
	protected AbstractExecuteContext context;
	/**
	 * 文件名
	 */
	protected String urlFile;
	/**
	 * 行号
	 */
	protected int startLineNo;
	/**
	 * 标签从模板中的第几个字符开始
	 */
	protected int startCharIndex;
	/**
	 * 父标签，如果没有父标签则为null
	 */
	protected AbstractTag parent;
	/**
	 * 使用了表达式的属性
	 */
	protected Mapx<String, String> holderAttributes = new CaseIgnoreMapx<String, String>();
	/**
	 * 所有属性值
	 */
	protected Mapx<String, String> attributes = new CaseIgnoreMapx<String, String>();
	/**
	 * 变量值。这些变量仅在本标签及子标签内有效。
	 */
	protected Mapx<String, Object> variables = new CaseIgnoreMapx<String, Object>();
	/**
	 * 标签的源代码
	 */
	protected String tagSource;
	/**
	 * 标签体的源代码
	 */
	protected String tagBodySource;

	protected TagContextData tagContextData;

	/**
	 * 从上下文中替换标签属性中的占位符
	 */
	public void init() throws ExpressionException {
		for (Entry<String, String> e : holderAttributes.entrySet()) {
			try {
				String v = e.getValue();
				Object obj = pageContext.evalExpression(v);
				if (obj == null) {
					obj = "";// null必须转为空串
				}
				setAttributeInternal(e.getKey(), obj);
				attributes.put(e.getKey(), obj.toString());
			} catch (java.lang.NullPointerException t) {
				t.printStackTrace();
			}
		}
	}

	/**
	 * 设置标签执行的上下文
	 */
	public void setPageContext(AbstractExecuteContext pageContext) {
		this.pageContext = context = pageContext;
	}

	/**
	 * 返回标签的唯一ID
	 */
	@Override
	public String getExtendItemID() {
		return getPrefix() + ":" + getTagName();
	}

	/**
	 * 返回标签的名称
	 */
	@Override
	public abstract String getExtendItemName();

	/**
	 * 返回标签的描述
	 */
	public abstract String getDescription();

	/**
	 * 返回标签所在插件的插件ID
	 */
	public abstract String getPluginID();

	/**
	 * 返回标签前缀
	 */
	public abstract String getPrefix();

	/**
	 * 返回标签名
	 */
	public abstract String getTagName();

	/**
	 * 返回标签支持的属性列表
	 */
	public abstract List<TagAttr> getTagAttrs();

	/**
	 * 标签开始执行前调用此方法
	 */
	public int doStartTag() throws TemplateRuntimeException {
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * 标签执行结束前调用此方法
	 */
	public int doEndTag() throws TemplateRuntimeException {
		return EVAL_PAGE;
	}

	/**
	 * 标签体执行完成后调用此方法
	 */
	public int doAfterBody() throws TemplateRuntimeException {
		return SKIP_BODY;
	}

	/**
	 * 返回父级标签
	 */
	public AbstractTag getParent() {
		return parent;
	}

	/**
	 * 返回标签体的执行结果
	 */
	public String getBody() {
		String body = context.getOut().getBuffer();
		context.getOut().clearBuffer();
		return body;
	}

	/**
	 * 克隆一个标签
	 */
	@Override
	public Object clone() {
		try {
			AbstractTag tag = (AbstractTag) super.clone();
			tag.attributes = attributes.clone();
			tag.variables = variables.clone();
			tag.tagContextData = null;
			return tag;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 返回标签所在源文件的文件名
	 */
	public String getUrlFile() {
		return urlFile;
	}

	/**
	 * 返回标签在源文件中的行号
	 */
	public int getStartLineNo() {
		return startLineNo;
	}

	private static CaseIgnoreMapx<String, Method> getMethods(AbstractTag tag) {
		CaseIgnoreMapx<String, Method> methods = AllTagMethods.get(tag.getExtendItemID());
		if (methods == null) {
			lock.lock();
			try {
				methods = AllTagMethods.get(tag.getExtendItemID());
				if (methods == null) {
					methods = new CaseIgnoreMapx<String, Method>(true);
					Class<?> Clazz = tag.getClass();
					// 缓存属性到方法的映射
					for (Method m : Clazz.getMethods()) {
						String name = m.getName();

						if (!name.startsWith("set")) {
							continue;
						}
						if (m.getModifiers() != Modifier.PUBLIC) {
							continue;
						}
						if (m.getParameterTypes() == null || m.getParameterTypes().length != 1) {
							continue;
						}
						name = name.substring(3);
						methods.put(name.toLowerCase(), m);
					}
					AllTagMethods.put(tag.getExtendItemID(), methods);
				}
			} finally {
				lock.unlock();
			}
		}
		return methods;
	}

	/**
	 * 判断标签是否支持指定属性名
	 */
	public boolean hasAttribute(String name) {
		for (TagAttr tag : getTagAttrs()) {
			if (tag.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断value是否是属性attr的合法的值
	 */
	public boolean isValidAttributeValue(String attr, Object value) {
		List<TagAttr> all = getTagAttrs();
		if (all == null) {
			return true;
		}
		for (TagAttr ta : all) {
			if (ta.getName().equals(attr)) {
				if (ta.isMandatory()) {
					if (StringUtil.isEmpty(value.toString())) {
						return false;
					}
				}
				break;
			}
		}
		return true;
	}

	/**
	 * 设置属性值
	 */
	public void setAttribute(String attr, String value) throws TemplateRuntimeException {
		setAttributeInternal(attr, value);
		if (value != null && value.indexOf("${") >= 0) {
			holderAttributes.put(attr, value);
		}
		attributes.put(attr, value);
	}

	/**
	 * 获取属性值(转化为字符串)
	 */
	public String getAttribute(String attr) {
		return attributes.getString(attr);
	}

	protected void setAttributeInternal(String attr, Object value) throws TemplateRuntimeException {
		// 需要缓存设置
		try {
			if (value != null && !isValidAttributeValue(attr, value)) {
				throw new TemplateRuntimeException(Lang.get("Framework.Template.AttributeInvalid",
						new Object[] { LangUtil.get(getExtendItemName()), attr, value }));
			}
		} catch (TemplateCompileException e) {
			throw new TemplateRuntimeException(e.getMessage());
		}
		Method m = getMethods(this).get(attr);
		if (m == null) {
			return;// 有可能是标签自己从attributes中取值，不需要调用set方法
		}
		Class<?> cls = m.getParameterTypes()[0];
		try {
			if (value == null) {
				m.invoke(this, new Object[] { null });
				return;
			}
			if (!(value instanceof String) || value.toString().indexOf("${") < 0) {
				if (cls == Integer.class || cls == int.class) {
					m.invoke(this, new Object[] { Primitives.getInteger(value) });
				} else if (cls == Long.class || cls == long.class) {
					m.invoke(this, new Object[] { Primitives.getLong(value) });
				} else if (cls == Float.class || cls == float.class) {
					m.invoke(this, new Object[] { Primitives.getFloat(value) });
				} else if (cls == Double.class || cls == double.class) {
					m.invoke(this, new Object[] { Primitives.getDouble(value) });
				} else if (cls == Boolean.class || cls == boolean.class) {
					m.invoke(this, new Object[] { Primitives.getBoolean(value) });
				} else if (cls == String.class) {
					m.invoke(this, new Object[] { value.toString() });
				} else {
					m.invoke(this, new Object[] { value });
				}
			} else if (cls == String.class) {// 对于值中有表达式的字符串属性，为其赋原始串，以便于编译时获取其值。
				m.invoke(this, new Object[] { value.toString() });
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 设置标签所在文件
	 */
	public void setUrlFile(String fileName) {
		this.urlFile = fileName;
	}

	/**
	 * 设置标签的起始行号
	 */
	public void setStartLineNo(int startLineNo) {
		this.startLineNo = startLineNo;
	}

	/**
	 * 设置标签的父标签
	 */
	public void setParent(AbstractTag parent) {
		this.parent = parent;
	}

	/**
	 * 返回上下文
	 */
	public AbstractExecuteContext getContext() {
		return pageContext;
	}

	/**
	 * 获取标签专有变量的值(此变量仅在标签的标签体和子标签中能够使用)
	 */
	public Object getVariable(String key) {
		return variables.get(key);
	}

	/**
	 * 设置标签专有变量的值(此变量仅在标签的标签体和子标签中能够使用)
	 */
	public void setVariable(String key, Object value) {
		variables.put(key, value);
	}

	/**
	 * 获得标签在源文件中的字符位置
	 */
	public int getStartCharIndex() {
		return startCharIndex;
	}

	/**
	 * 设置标签在源文件中的字符位置
	 */
	public void setStartCharIndex(int startCharIndex) {
		this.startCharIndex = startCharIndex;
	}

	/**
	 * 对编译完成后的command对象进行进一步处理。
	 */
	public void afterCompile(TagCommand command, TemplateExecutor executor) throws TemplateCompileException {

	}

	/**
	 * 返回本标签中定义的变量的列表
	 */
	public Mapx<String, Object> getVariables() {
		return variables;
	}

	/**
	 * 设置本标签中定义的变量的列表
	 */
	public void setVariables(Mapx<String, Object> variables) {
		this.variables = variables;
	}

	/**
	 * 是否在编译后持有本标签的源代码（并可以通过getTagSource()获取到）
	 */
	public boolean isKeepTagSource() {
		return false;
	}

	/**
	 * 返回本标签的标签的源代码。必须重载isKeepTagSource()并返回true才会有值，否则返回null
	 */
	public String getTagSource() {
		return tagSource;
	}

	/**
	 * 返回本标签的标签体的源代码,不包括标签本身。必须重载isKeepTagSource()并返回true才会有值，否则返回null
	 */
	public String getTagBodySource() {
		return tagBodySource;
	}

	public TagContextData getTagContextData() {
		if (tagContextData == null) {
			tagContextData = new TagContextData();
			tagContextData.init(context, this);
		}
		return tagContextData;
	}

	public void setTagContextData(TagContextData tagContextData) {
		this.tagContextData = tagContextData;
	}

	/**
	 * 请使用pageContext.getOut()替代
	 */
	@Deprecated
	protected TemplateWriter getPreviousOut() {
		return pageContext.getOut();
	}
}
