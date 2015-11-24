package com.zving.platform;

import com.zving.framework.extend.IExtendItem;

public interface IEventType<T> extends IExtendItem {
	public MessageEvent getEvent(T obj);
}
