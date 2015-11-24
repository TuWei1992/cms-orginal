package com.zving.framework.ui.zhtml;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.zving.framework.core.bean.BeanUtil;
import com.zving.framework.expression.CachedEvaluator;
import com.zving.framework.expression.IEvaluator;
import com.zving.framework.expression.IFunctionMapper;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.ITemplateManager;
import com.zving.framework.template.ITemplateManagerContext;
import com.zving.framework.template.ITemplateSourceProcessor;

/**
 * Zhtml管理器上下文
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-9
 */
public class ZhtmlManagerContext implements ITemplateManagerContext {
	private static ZhtmlManagerContext instance;
	private static ReentrantLock lock = new ReentrantLock();
	private ITemplateManager templateManager = new ZhtmlManager();

	public static ZhtmlManagerContext getInstance() {
		if (instance == null) {
			lock.lock();
			try {
				if (instance == null) {
					ZhtmlManagerContext mc = new ZhtmlManagerContext();
					instance = mc;
				}
			} finally {
				lock.unlock();
			}
		}
		return instance;
	}

	private ZhtmlManagerContext() {
	}

	@Override
	public IEvaluator getEvaluator() {
		return new CachedEvaluator();
	}

	@Override
	public IFunctionMapper getFunctionMapper() {
		return ZhtmlFunctionService.getFunctionMappper();
	}

	@Override
	public List<ITemplateSourceProcessor> getSourceProcessors() {
		return ZhtmlSourceProcessorService.getInstance().getAll();
	}

	@Override
	public List<AbstractTag> getTags() {
		return ZhtmlTagService.getInstance().getAll();
	}

	@Override
	public AbstractTag getTag(String prefix, String tagName) {
		for (AbstractTag tag : getTags()) {
			if (tag.getPrefix().equals(prefix) && tag.getTagName().equals(tagName)) {
				return tag;// 不需要克隆
			}
		}
		return null;
	}

	@Override
	public ITemplateManager getTemplateManager() {
		return templateManager;
	}

	@Override
	public AbstractTag createNewTagInstance(String prefix, String tagName) {
		AbstractTag tag = getTag(prefix, tagName);
		if (tag != null) {
			tag = (AbstractTag) BeanUtil.create(tag.getClass());
		}
		return tag;
	}
}
