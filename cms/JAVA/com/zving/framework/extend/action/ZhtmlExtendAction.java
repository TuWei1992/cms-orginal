package com.zving.framework.extend.action;

import java.util.Map.Entry;

import com.zving.framework.Current;
import com.zving.framework.collection.Mapx;
import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.ServletUtil;

/**
 * Zhtml扩展行为虚拟类。 <br>
 * 
 * @date 2009-11-7 <br>
 * @author 王育春 <br>
 * @email wangyc@zving.com <br>
 */
public abstract class ZhtmlExtendAction implements IExtendAction {
	@Override
	public Object execute(Object[] args) throws ExtendException {
		AbstractExecuteContext pageContext = (AbstractExecuteContext) args[0];
		ZhtmlContext context = new ZhtmlContext(Current.getRequest());
		execute(context);
		if (!ObjectUtil.empty(context.getOut())) {
			pageContext.getOut().write(context.getOut());
		}
		if (context.getIncludes().size() > 0) {
			for (String file : context.getIncludes()) {
				try {
					AbstractExecuteContext includeContext = pageContext.getIncludeContext();
					if (file.indexOf('?') > 0) {
						Mapx<String, String> map = ServletUtil.getMapFromQueryString(file);
						for (Entry<String, String> e : map.entrySet()) {
							includeContext.addRootVariable(e.getKey(), e.getValue());
						}
					}
					includeContext.getManagerContext().getTemplateManager().execute(file, includeContext);
				} catch (Exception e) {
					e.printStackTrace();
					throw new ExtendException(e.getMessage());
				}
			}
		}
		return null;
	}

	public abstract void execute(ZhtmlContext context) throws ExtendException;
}
