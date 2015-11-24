package com.zving.framework.template;

import com.zving.framework.data.DataRow;
import com.zving.framework.expression.ITagData;
import com.zving.framework.ui.tag.IListTag;

/**
 * 标签上下文数据
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-3-20
 */
public class TagContextData implements ITagData {
	AbstractExecuteContext context;
	AbstractTag tag;
	private boolean found;

	public void init(AbstractExecuteContext context, AbstractTag tag) {
		this.context = context;
		this.tag = tag;
		found = false;
	}

	@Override
	public ITagData getParent() {
		if (tag == null || tag.getParent() == null) {
			return null;
		}
		return tag.getParent().getTagContextData();
	}

	@Override
	public Object getValue(String var) {
		if (tag != null) {
			if (tag instanceof IListTag) {
				DataRow dr = ((IListTag) tag).getCurrentDataRow();
				if (dr != null && dr.getDataColumn(var) != null) {// 如果有字段，则null值也返回
					found = true;// 对于List标签中存在null值的情况，应告诉外层循环结束查找
					return dr.get(var);
				}
			}
			Object v = tag.getVariable(var);
			if (v != null) {
				return v;
			} else {
				found = tag.getVariables().containsKey(var);
				return null;
			}
		}
		return null;
	}

	@Override
	public boolean isFound() {
		return found;
	}
}
