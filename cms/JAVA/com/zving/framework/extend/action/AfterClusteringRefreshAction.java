package com.zving.framework.extend.action;

import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;
import com.zving.framework.json.JSONObject;

/**
 * 提供集群缓存更新后扩展，此扩展点供非cachemanage管理的缓存扩展使用，
 * 比如配置项更新通知集群中其它主机更新
 * 
 * @author 蒋海群
 * @mail jhq@zving.com
 * @date 2014年9月18日
 */
public abstract class AfterClusteringRefreshAction implements IExtendAction {
	public static final String ExtendPointID = "com.zving.framework.AfterClusteringRefresh";

	@Override
	public Object execute(Object[] args) throws ExtendException {
		JSONObject sj = (JSONObject) args[0];
		execute(sj);
		return null;
	}

	public abstract void execute(JSONObject sj);
}
