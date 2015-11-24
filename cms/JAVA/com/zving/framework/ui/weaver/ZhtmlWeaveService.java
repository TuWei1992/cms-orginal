package com.zving.framework.ui.weaver;

import java.util.concurrent.locks.ReentrantLock;

import com.zving.framework.extend.AbstractExtendService;
import com.zving.framework.utility.RegexParser;

/**
 * Zhtml织入服务类
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-3-9
 */
public class ZhtmlWeaveService extends AbstractExtendService<IZhtmlWeaver> {
	private static ReentrantLock lock = new ReentrantLock();

	public static ZhtmlWeaveService getInstance() {
		return findInstance(ZhtmlWeaveService.class);
	}

	/**
	 * 执行织入
	 */
	public static String weave(String fileName, String source) {
		ZhtmlWeaveHelper helper = null;

		lock.lock();// 需要加锁，因为RegexParser线程不安全
		try {
			for (IZhtmlWeaver w : getInstance().getAll()) {
				if (match(w.getTarget(), fileName)) {
					if (helper == null) {
						helper = new ZhtmlWeaveHelper(source);
					}
					w.weave(helper);
				}
			}
		} finally {
			lock.unlock();
		}

		if (helper == null) {
			return source;
		}
		return helper.getResult();
	}

	/**
	 * 指定的文件名是否是匹配的织入目标
	 */
	private static boolean match(String target, String fileName) {
		if (target.indexOf('{') > 0) {
			RegexParser rp = new RegexParser(target);
			rp.setText(fileName);
			return rp.match();
		}
		return target.equals(fileName);
	}
}
