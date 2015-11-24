package com.zving.platform;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.List;

import com.sun.star.bridge.oleautomation.Decimal;
import com.sun.star.util.DateTime;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataTypes;
import com.zving.framework.i18n.Lang;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.NumberUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.api.APIOutput;
import com.zving.platform.api.APIParam;
import com.zving.platform.api.APIRequest;
import com.zving.platform.api.APIResponse;

public abstract class AbstractAPIMethod implements IAPIMethod {

	protected List<APIParam> params = new ArrayList<APIParam>();

	protected List<APIOutput> output = new ArrayList<APIOutput>();

	/**
	 * 执行结果逻辑，返回执行结果
	 */
	public abstract void execute(APIRequest request, APIResponse response);

	@Override
	public APIResponse invoke(APIRequest request) {
		APIResponse response = new APIResponse();
		if (!verify(request, response)) {
			return response;
		}
		execute(request, response);
		return response;
	}

	protected boolean verify(APIRequest request, APIResponse response) {
		List<APIParam> params = getParams();
		Mapx<String, Object> map = request.getParameters();
		for (APIParam param : params) {
			Object obj = map.get(param.getName());
			if (!param.isAllowNull()) {
				if (obj == null) {
					response.setMessage(param.getName() + Lang.get("Platform.API.ParamNotNull"));
					response.setStatus(STATUS_FAILED);
					return false;
				}
			}
			int type = param.getType();
			if (!matchesType(type, obj)) {
				response.setMessage(Lang.get("Platform.API.TypeNotMaches"));
				response.setStatus(STATUS_FAILED);
				return false;
			}
		}
		response.setStatus(STATUS_SUCCESS);
		return true;
	}

	private boolean matchesType(int type, Object obj) {
		if (obj == null) {
			return true;
		}
		switch (type) {
		case DataTypes.INTEGER: {
			if (obj instanceof Integer || NumberUtil.isInteger(obj.toString())) {
				return true;
			}
			break;
		}
		case DataTypes.LONG: {
			if (obj instanceof Long || NumberUtil.isLong(obj.toString())) {
				return true;
			}
			break;
		}
		case DataTypes.DOUBLE: {
			if (obj instanceof Double || NumberUtil.isDouble(obj.toString())) {
				return true;
			}
			break;
		}
		case DataTypes.DECIMAL: {
			if (obj instanceof Decimal || NumberUtil.isNumber(obj.toString())) {
				return true;
			}
			break;
		}
		case DataTypes.SMALLINT: {
			if (obj instanceof Short || NumberUtil.isInt(obj.toString())) {
				return true;
			}
			break;
		}
		case DataTypes.BIGDECIMAL: {
			if (obj instanceof BigDecimal || NumberUtil.isNumber(obj.toString())) {
				return true;
			}
			break;
		}
		case DataTypes.DATETIME: {
			if (obj instanceof DateTime || DateUtil.isDateTime(obj.toString())) {
				return true;
			}
			break;
		}
		case DataTypes.BLOB: {
			if (obj instanceof Blob || obj instanceof byte[]) {
				return true;
			}
			break;
		}
		case DataTypes.CLOB: {
			if (obj instanceof Clob || obj instanceof String) {
				return true;
			}
			break;
		}
		case DataTypes.STRING: {
			if (StringUtil.isNotEmpty(obj.toString())) {
				return true;
			}
			break;
		}
		}
		return false;
	}

	@Override
	public List<APIParam> getParams() {
		return params;
	}

	@Override
	public List<APIOutput> getOutput() {
		return output;
	}

	/**
	 * 添加接口参数，仅在初始化时使用
	 */
	protected void addParam(String name, String memo, int type, boolean allowNull) {
		params.add(new APIParam(name, memo, type, allowNull));
	}

	/**
	 * 添加接口输出项，仅在初始化时使用
	 */
	protected void addOutput(String parentName, String name, int type, boolean allowNull, String memo) {
		output.add(new APIOutput(parentName, name, memo, type, allowNull));
	}
}
