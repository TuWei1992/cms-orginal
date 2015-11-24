package com.zving.framework.ui.tag;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.data.DataTypes;
import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.exception.TemplateRuntimeException;

/**
 * For循环标签。<br>
 * 用于指定步长的递增循环,from属性指明循环变量的起始值，to属性指明循环变量的结束值，step属性指明循环的步长。<br>
 * 注意：在循环体内可以获取到${i}用于表明循环变量的当前值，${first}用来表明是不是第一次循环，${last}用来表明是否是最后一次循环
 * 
 * @Author 王育春
 * @Mail wyuch@zving.com
 * @Date 2012-2-5
 */
public class ForTag extends AbstractTag {
	int from;
	int to;
	int step;
	int pos;

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "for";
	}

	@Override
	public int doStartTag() throws TemplateRuntimeException {
		if (step == 0) {
			step = 1;
		}
		if (from == to || step > 0 && from > to || step < 0 && from < to) {
			return SKIP_BODY;
		} else {
			pos = from;
			context.addDataVariable("i", pos);
			context.addDataVariable("first", true);
			if (step > 0 && pos + step == to || step < 0 && pos - step == to) {
				context.addDataVariable("last", true);
			} else {
				context.addDataVariable("last", false);
			}
			return EVAL_BODY_INCLUDE;
		}
	}

	@Override
	public int doAfterBody() throws TemplateRuntimeException {
		pos += step;
		if (step > 0 && pos < to || step < 0 && pos > to) {
			context.addDataVariable("i", pos);
			context.addDataVariable("first", false);
			if (step > 0 && pos + step == to || step < 0 && pos - step == to) {
				context.addDataVariable("last", true);
			}
			return EVAL_BODY_AGAIN;
		} else {
			return SKIP_BODY;
		}
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public int getTo() {
		return to;
	}

	public void setTo(int to) {
		this.to = to;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("from", true, DataTypes.INTEGER, "@{Framework.CycleFrom}"));
		list.add(new TagAttr("to", true, DataTypes.INTEGER, "@{Framework.CycleEnd}"));
		list.add(new TagAttr("step", false, DataTypes.INTEGER, "@{Framework.CycleStep}"));
		return list;
	}

	@Override
	public String getPluginID() {
		return FrameworkPlugin.ID;
	}

	@Override
	public String getDescription() {
		return "@{Framework.ZForTagDescription}";
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.ZForTagName}";
	}

}
