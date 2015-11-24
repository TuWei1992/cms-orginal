package com.zving.framework.ui.zhtml;

import java.util.concurrent.locks.ReentrantLock;

import com.zving.framework.extend.AbstractExtendService;
import com.zving.framework.extend.action.ExtendTag;
import com.zving.framework.i18n.LangButtonTag;
import com.zving.framework.i18n.LangTag;
import com.zving.framework.security.PrivTag;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.ui.control.ButtonTag;
import com.zving.framework.ui.control.CheckboxTag;
import com.zving.framework.ui.control.ChildTabTag;
import com.zving.framework.ui.control.DataGridTag;
import com.zving.framework.ui.control.DataListTag;
import com.zving.framework.ui.control.MenuTag;
import com.zving.framework.ui.control.PageBarTag;
import com.zving.framework.ui.control.PanelHeaderTag;
import com.zving.framework.ui.control.RadioTag;
import com.zving.framework.ui.control.ScrollPanelTag;
import com.zving.framework.ui.control.SelectTag;
import com.zving.framework.ui.control.SliderTag;
import com.zving.framework.ui.control.TabTag;
import com.zving.framework.ui.control.ToolBarTag;
import com.zving.framework.ui.control.TreeTag;
import com.zving.framework.ui.control.UploaderTag;
import com.zving.framework.ui.tag.ActionTag;
import com.zving.framework.ui.tag.ChooseTag;
import com.zving.framework.ui.tag.ConfigTag;
import com.zving.framework.ui.tag.ElseIfTag;
import com.zving.framework.ui.tag.ElseTag;
import com.zving.framework.ui.tag.EvalTag;
import com.zving.framework.ui.tag.ForEachTag;
import com.zving.framework.ui.tag.ForTag;
import com.zving.framework.ui.tag.IfTag;
import com.zving.framework.ui.tag.IncludeTag;
import com.zving.framework.ui.tag.InitTag;
import com.zving.framework.ui.tag.InvokeTag;
import com.zving.framework.ui.tag.ListTag;
import com.zving.framework.ui.tag.ParamTag;
import com.zving.framework.ui.tag.SetTag;
import com.zving.framework.ui.tag.SubTag;
import com.zving.framework.ui.tag.WhenTag;

/**
 * Zhtml标签扩展服务
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-4-12
 */
public class ZhtmlTagService extends AbstractExtendService<AbstractTag> {
	private static ZhtmlTagService instance;
	private static ReentrantLock lock = new ReentrantLock();

	public static ZhtmlTagService getInstance() {
		if (instance == null) {
			lock.lock();
			try {
				if (instance == null) {
					ZhtmlTagService tmp = findInstance(ZhtmlTagService.class);
					tmp.register(new DataGridTag());
					tmp.register(new DataListTag());
					tmp.register(new InitTag());
					tmp.register(new PageBarTag());
					tmp.register(new ButtonTag());
					tmp.register(new PanelHeaderTag());
					tmp.register(new ChildTabTag());
					tmp.register(new TabTag());
					tmp.register(new TreeTag());
					tmp.register(new MenuTag());
					tmp.register(new SelectTag());
					tmp.register(new UploaderTag());
					tmp.register(new ListTag());
					tmp.register(new RadioTag());
					tmp.register(new CheckboxTag());
					tmp.register(new ParamTag());
					tmp.register(new IncludeTag());
					tmp.register(new IfTag());
					tmp.register(new ElseTag());
					tmp.register(new ChooseTag());
					tmp.register(new WhenTag());
					tmp.register(new ToolBarTag());
					tmp.register(new ScrollPanelTag());
					tmp.register(new ActionTag());
					tmp.register(new ExtendTag());
					tmp.register(new PrivTag());
					tmp.register(new EvalTag());
					tmp.register(new ForTag());
					tmp.register(new SetTag());
					tmp.register(new LangTag());
					tmp.register(new LangButtonTag());
					tmp.register(new SliderTag());
					tmp.register(new ForEachTag());
					tmp.register(new SubTag());
					tmp.register(new InvokeTag());
					tmp.register(new ConfigTag());
					tmp.register(new ElseIfTag());
					instance = tmp;
				}
			} finally {
				lock.unlock();
			}
		}
		return instance;
	}
}
