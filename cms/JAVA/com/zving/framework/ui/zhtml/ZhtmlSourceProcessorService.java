package com.zving.framework.ui.zhtml;

import java.util.concurrent.locks.ReentrantLock;

import com.zving.framework.extend.AbstractExtendService;
import com.zving.framework.template.ITemplateSourceProcessor;

/**
 * Zhtml源代码处理器扩展服务。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-4-12
 */
public class ZhtmlSourceProcessorService extends AbstractExtendService<ITemplateSourceProcessor> {
	private static ZhtmlSourceProcessorService instance;
	private static ReentrantLock lock = new ReentrantLock();

	public static ZhtmlSourceProcessorService getInstance() {
		if (instance == null) {
			lock.lock();
			try {
				if (instance == null) {
					instance = findInstance(ZhtmlSourceProcessorService.class);
				}
			} finally {
				lock.unlock();
			}
		}
		return instance;
	}
}
