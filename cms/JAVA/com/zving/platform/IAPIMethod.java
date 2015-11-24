package com.zving.platform;

import java.util.List;

import com.zving.framework.extend.IExtendItem;
import com.zving.platform.api.APIOutput;
import com.zving.platform.api.APIParam;
import com.zving.platform.api.APIRequest;
import com.zving.platform.api.APIResponse;

public interface IAPIMethod extends IExtendItem {
	// 失败状态
	public final static int STATUS_FAILED = 0;
	// 成功状态
	public final static int STATUS_SUCCESS = 1;
	public static final int Status_AuthenticateFailed = 501;
	public static final int Status_DataFormatNotFound = 405;
	public static final int Status_MethodNotFound = 404;

	/**
	 * 调用API方法
	 */
	public APIResponse invoke(APIRequest request);

	/**
	 * 获取API接口支持的参数信息
	 */
	public List<APIParam> getParams();

	/**
	 * 获取API接口输出的数据项信息
	 */
	public List<APIOutput> getOutput();
}
