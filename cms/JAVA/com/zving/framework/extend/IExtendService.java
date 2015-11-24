package com.zving.framework.extend;

import java.util.List;

/**
 * 扩展服务接口
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-3-9
 */
public interface IExtendService<T extends IExtendItem> {
	public void register(IExtendItem item);

	public T get(String id);

	public T remove(String id);

	public List<T> getAll();

	public void destory();
}
