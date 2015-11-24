package com.zving.framework.core;

import java.util.Comparator;

import com.zving.framework.collection.ReadOnlyList;
import com.zving.framework.extend.AbstractExtendService;
import com.zving.framework.utility.ObjectUtil;

/**
 * URL处理者扩展服务
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-5
 */
public class URLHandlerService extends AbstractExtendService<IURLHandler> {
	public static URLHandlerService getInstance() {
		return findInstance(URLHandlerService.class);
	}

	@Override
	protected void prepareItemList() {
		itemList = ObjectUtil.toList(itemMap.values());
		itemList = ObjectUtil.sort(itemList, new Comparator<IURLHandler>() {
			@Override
			public int compare(IURLHandler o1, IURLHandler o2) {
				return o1.getOrder() - o2.getOrder();
			}
		});
		itemList = new ReadOnlyList<IURLHandler>(itemList);
	}
}
