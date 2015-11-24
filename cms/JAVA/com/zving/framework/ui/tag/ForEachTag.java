package com.zving.framework.ui.tag;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.zving.framework.core.bean.BeanUtil;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTypes;
import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.utility.Primitives;

/**
 * ForEach标签，用于循环输出集合及JavaBean
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2012-2-5
 */
public class ForEachTag extends AbstractTag {
	Object data;
	int count;
	int begin = 0;
	int i = 0;
	Object[] arr;
	Map<?, ?> map;
	Iterator<?> mapIterator;
	Iterator<?> iterator;

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "foreach";
	}

	@Override
	public int doStartTag() throws TemplateRuntimeException {
		i = begin;
		if (count <= 0) {
			count = Integer.MAX_VALUE;
		}
		if (data == null) {
			return SKIP_BODY;
		}
		if (Primitives.isPrimitives(data) || data instanceof String) {
			return SKIP_BODY;
		}
		if (data instanceof DataRow) {
			data = ((DataRow) data).toCaseIgnoreMapx();// 处理DataRow的遍历
		}
		if (data.getClass().isArray()) {
			arr = new Object[Array.getLength(data)];
			for (int i = 0; i < Array.getLength(data); i++) {
				arr[i] = Array.get(data, i);
			}
		} else if (data instanceof Collection) {
			iterator = ((Collection<?>) data).iterator();
		} else if (data instanceof Map) {
			map = (Map<?, ?>) data;
			mapIterator = map.keySet().iterator();
		} else if (data instanceof Iterable) {
			iterator = ((Iterable<?>) data).iterator();
		} else {// 作为bean处理
			map = BeanUtil.toMap(data, true);
		}
		for (int j = 0; j <= begin; j++) {
			if (!hasNext()) {
				return SKIP_BODY;
			}
			prepareNext();
		}
		context.addDataVariable("i", i - begin);
		context.addDataVariable("first", true);
		i++;
		if (hasNext()) {// 已经执行过一次prepareNext()了
			context.addDataVariable("last", false);
		} else {
			context.addDataVariable("last", true);
		}
		return EVAL_BODY_INCLUDE;
	}

	@Override
	public int doAfterBody() throws TemplateRuntimeException {
		if (!hasNext() || i - begin >= count) {
			return EVAL_PAGE;
		}
		prepareNext();
		context.addDataVariable("first", false);
		context.addDataVariable("i", i - begin);
		i++;
		if (i - begin + 1 == count || !hasNext()) {
			context.addDataVariable("last", true);
		}
		return EVAL_BODY_AGAIN;
	}

	private void prepareNext() {
		Object obj = null;
		if (iterator != null) {
			obj = iterator.next();
		} else if (arr != null) {
			obj = arr[i];
		} else if (map != null) {
			Object k = mapIterator.next();
			obj = map.get(k);
			context.addDataVariable("key", k);
		}
		context.addDataVariable("value", obj);
	}

	private boolean hasNext() {
		if (iterator != null) {
			return iterator.hasNext();
		} else if (arr != null) {
			return i < arr.length;
		} else if (map != null) {
			return mapIterator.hasNext();
		}
		return false;
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("data", true, DataTypes.OBJECT, "@{Framework.CycleEnd}"));
		list.add(new TagAttr("count", DataTypes.INTEGER, "@{Framework.ListTag.Count}"));
		list.add(new TagAttr("begin", DataTypes.INTEGER, "@{Framework.ListTag.Begin}"));
		return list;
	}

	@Override
	public String getPluginID() {
		return FrameworkPlugin.ID;
	}

	@Override
	public String getDescription() {
		return "@{Framework.ZForEachTag.Desc}";
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.ZForeachTagNmae}";
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getBegin() {
		return begin;
	}

	public void setBegin(int begin) {
		this.begin = begin;
	}
}
