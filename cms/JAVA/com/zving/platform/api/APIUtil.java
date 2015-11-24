package com.zving.platform.api;

import java.util.Map;
import java.util.concurrent.TimeoutException;

import net.rubyeye.xmemcached.MemcachedClient;

import com.zving.framework.Config;
import com.zving.framework.User;
import com.zving.framework.cache.MemCachedManager;
import com.zving.framework.collection.CaseIgnoreMapx;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataColumn;
import com.zving.framework.data.DataTable;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.security.PasswordUtil;
import com.zving.framework.security.Privilege;
import com.zving.framework.utility.Errorx;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.IAPIDataFormat;
import com.zving.platform.IAPIMethod;
import com.zving.platform.api.format.JSONFormat;
import com.zving.platform.bl.PrivBL;
import com.zving.platform.code.Enable;
import com.zving.platform.code.YesOrNo;
import com.zving.platform.config.APIIPRanges;
import com.zving.platform.config.APIMethodCache;
import com.zving.platform.config.AdminUserName;
import com.zving.platform.service.APIDataFormatService;
import com.zving.platform.service.APIMethodService;
import com.zving.platform.service.UserPreferencesService;
import com.zving.schema.ZDUser;

/**
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-4-3
 */
public class APIUtil {
	public static final String Prefix = "com.zving.api.";

	/**
	 * 校验接口调用请求是否合法
	 * 
	 * @param request 接口请求
	 */
	public static boolean authenticate(APIRequest request) {
		String username = request.getUserName();
		String password = request.getPassword();
		if (StringUtil.isEmpty(username)) {
			Errorx.addError("@{Platform.API.UserNameEmpty}");
			return false;
		}
		if (StringUtil.isEmpty(password)) {
			Errorx.addError("@{Platform.API.PasswordEmpty}");
			return false;
		}

		ZDUser user = new ZDUser();
		user.setUserName(username);
		if (!user.fill()) {
			Errorx.addError("@{Platform.API.UserNotFound}");
			return false;
		}
		if (!Config.isInstalled() && !user.getUserName().equalsIgnoreCase(AdminUserName.getValue())) {
			Errorx.addError("@{User.DenyLoginTemp}");
			return false;
		}
		if (!PasswordUtil.verify(password, user.getPassword())) {
			Errorx.addError("@{Common.UserNameOrPasswordWrong}");
			return false;
		}
		if (!AdminUserName.getValue().equalsIgnoreCase(user.getUserName()) && Enable.isDisable(user.getStatus())) {
			Errorx.addError("@{User.UserStopped}");
			return false;
		}

		// 检查IP范围
		if (!APIIPRanges.isInRange(request.getClientIP())) {
			Errorx.addError("@{Platform.API.IPNotInRanges}");
			return false;
		}
		Privilege privilege = PrivBL.getUserPriv(user.getUserName());
		// 用戶权限校验
		if (!AdminUserName.getValue().equals(user.getUserName())&&!privilege.hasPriv(Prefix + request.getMethodID())) {
			Errorx.addError("@{Platform.PrivCheckNoPriv}");
			return false;
		}
		String language = User.getLanguage();
		User.destory();
		User.setLanguage(language);
		User.setUserName(user.getUserName());
		User.setRealName(user.getRealName());
		User.setBranchInnerCode(user.getBranchInnerCode());
		User.setBranchAdministrator(YesOrNo.isYes(user.getIsBranchAdmin()));
		User.setType(user.getType());
		Mapx<String, Object> map = user.toMapx();
		map.remove("Password");
		User.getCurrent().putAll(map);
		User.getCurrent().putAll(UserPreferencesService.getUerPreferences(user.getUserName()));
		User.setLogin(true);
		User.setPrivilege(privilege);

		return true;
	}

	/**
	 * 调用API方法
	 * 
	 * @param methodID API方法ID
	 * @param APIUserName API用户名
	 * @param APIPassword API密码
	 * @param clientIP API调用者的IP地址
	 * @param language 语言(多语言时会影响返回数据中的文本数据)
	 * @param dataFormat 返回数据格式（json或xml）
	 * @param params 参数，参数的格式要求和 dataFormat保持一致
	 * @return 调用结果，是APIResponse按dataFormat指定的格式输出的字符串
	 * @throws Exception
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	public static String invoke(String methodID, String username, String password, String clientIP, String language, String dataFormat,
			String params) throws Exception {
		APIRequest request = new APIRequest();
		if (ObjectUtil.empty(language)) {
			language = LangUtil.getDefaultLanguage();
		}
		request.setMethodID(methodID);
		request.setPassword(password);
		request.setUserName(username);
		request.setClientIP(clientIP);
		request.setLanguage(language);
		request.setDataFormat(dataFormat);

		IAPIDataFormat format = null;
		if (StringUtil.isNotEmpty(dataFormat)) {
			format = APIDataFormatService.getInstance().get(dataFormat);
		}
		if (format == null) {
			format = APIDataFormatService.getInstance().get(JSONFormat.ID);
		}
		Mapx<String, Object> map = format.parse(params);
		map = new CaseIgnoreMapx<String, Object>(map);
		request.setParameters(map);
		// 缓存返回
		MemcachedClient client = null;
		String hash = null;
		if (APIMethodCache.isEnabled()) {
			hash = request.hash();
			client = MemCachedManager.getClient();
			Object o = client.get(hash);
			if (o != null) {
				return o.toString();
			}
		}

		APIResponse response = new APIResponse();
		if (!authenticate(request)) {
			response.setStatus(IAPIMethod.Status_AuthenticateFailed);
			response.setMessage("API user " + username + " authenticate failed:" + Errorx.getAllMessage());
		} else {
			IAPIMethod m = APIMethodService.getInstance().get(methodID);
			if (m == null) {
				response.setStatus(IAPIMethod.Status_MethodNotFound);
				response.setMessage("APIMethod " + methodID + " not found!");
			} else {
				response = m.invoke(request);
			}
		}
		String ret = format.toString(response);
		if (APIMethodCache.isEnabled() && client != null && hash != null) {
			client.add(hash, APIMethodCache.getExpires(), ret);
		}
		return ret;

	}

	/**
	 * 调用API方法
	 * 
	 * @param methodID API方法ID
	 * @param APIUserName API用户名
	 * @param APIPassword API密码
	 * @param clientIP API调用者的IP地址
	 * @param language 语言(多语言时会影响返回数据中的文本数据)
	 * @param dataFormat 返回数据格式（json或xml）
	 * @param params 参数，参数的格式要求和 dataFormat保持一致
	 * @param fileMap 上传文件信息
	 * @return 调用结果，是APIResponse按dataFormat指定的格式输出的字符串
	 * @throws Exception
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	public static String invoke(String methodID, String username, String password, String clientIP, String language, String dataFormat,
			String params, Map<String, Object> fileMap) throws Exception {
		APIRequest request = new APIRequest();
		if (ObjectUtil.empty(language)) {
			language = LangUtil.getDefaultLanguage();
		}
		request.setMethodID(methodID);
		request.setPassword(password);
		request.setUserName(username);
		request.setClientIP(clientIP);
		request.setLanguage(language);
		request.setDataFormat(dataFormat);

		IAPIDataFormat format = null;
		if (StringUtil.isNotEmpty(dataFormat)) {
			format = APIDataFormatService.getInstance().get(dataFormat);
		}
		if (format == null) {
			format = APIDataFormatService.getInstance().get(JSONFormat.ID);
		}
		Mapx<String, Object> map = format.parse(params);
		if (map == null) {
			map = new Mapx<String, Object>();
		}
		// 添加上传文件参数
		map.putAll(fileMap);
		request.setParameters(map);
		// 缓存返回
		MemcachedClient client = null;
		String hash = null;
		if (APIMethodCache.isEnabled()) {
			hash = request.hash();
			client = MemCachedManager.getClient();
			Object o = client.get(hash);
			if (o != null) {
				return o.toString();
			}
		}

		APIResponse response = new APIResponse();
		if (!authenticate(request)) {
			response.setStatus(IAPIMethod.Status_AuthenticateFailed);
			response.setMessage("API user " + username + " authenticate failed:" + Errorx.getAllMessage());
		} else {
			IAPIMethod m = APIMethodService.getInstance().get(methodID);
			if (m == null) {
				response.setStatus(IAPIMethod.Status_MethodNotFound);
				response.setMessage("APIMethod " + methodID + " not found!");
			} else {
				response = m.invoke(request);
			}
		}
		String ret = format.toString(response);
		if (APIMethodCache.isEnabled() && client != null && hash != null) {
			client.add(hash, APIMethodCache.getExpires(), ret);
		}
		return ret;

	}

	public static DataTable filterColumns(String columns, DataTable dt) {
		if (dt == null || ObjectUtil.empty(columns)) {
			return dt;
		}
		if (StringUtil.isNotNull(columns)) {
			String[] arr = columns.split("\\,");
			for (DataColumn dc : dt.getDataColumns()) {
				boolean flag = false;
				for (String column : arr) {
					if (dc.getColumnName().equalsIgnoreCase(column)) {
						flag = true;
						break;
					}
				}
				if (!flag) {
					dt.deleteColumn(dc.getColumnName());
				}
			}
		}
		return dt;
	}
}