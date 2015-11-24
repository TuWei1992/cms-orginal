package com.zving.platform.action;

import java.util.Map.Entry;

import com.zving.framework.extend.action.AfterClusteringRefreshAction;
import com.zving.framework.json.JSONObject;
import com.zving.platform.util.PlatformUtil;

/**
 * 缓存刷新扩展行为，供响应集群通知服务器更新配置项使用
 * 
 * @author 蒋海群
 * @mail jhq@zving.com
 * @date 2014年9月18日
 */
public class AfterClusteringRefreshExtend extends AfterClusteringRefreshAction {

	@Override
	public boolean isUsable() {
		return true;
	}

	@Override
	public void execute(JSONObject sj) {
		for (Entry<String, Object> pi : sj.entrySet()) {
			if (pi.getKey().equals(PlatformUtil.CONFIG_PROVIDER)) {
				PlatformUtil.loadDBConfig();
				return;
			}
		}
	}

}
