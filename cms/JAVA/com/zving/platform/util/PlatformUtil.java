package com.zving.platform.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.zving.framework.Config;
import com.zving.framework.cache.CacheManager;
import com.zving.framework.cache.CacheSyncUtil;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.utility.HtmlUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.config.AdminUserName;
import com.zving.schema.ZDRole;
import com.zving.schema.ZDUser;

/**
 * 平台工具类
 * 
 * @Author 王育春
 * @Date 2010-9-6
 * @Mail wyuch@zving.com
 */
public class PlatformUtil {
	public static final String INDENT = "　"; // 缩进

	public static final String CONFIG_PROVIDER = "ConfigProvider"; // 供集群重载配置项用

	private static Object mutex = new Object();

	public static Mapx<String, Object> getCodeMap(String codeType) {
		return HtmlUtil.codeToMapx(codeType);
	}

	public static String getFileIcon(String path) {
		if (path.endsWith("/")) {
			return "framework/images/filetype/folder.gif";
		} else {
			if (path.endsWith(".template.html")) {
				return "icons/extra/icon_template.gif";
			}
			if (path.indexOf(".") > 0) {
				String ext = path.substring(path.lastIndexOf(".") + 1);
				if (ext.equals("htm") || ext.equals("shtml")) {
					ext = "html";
				}
				if (ext.equals("jpeg")) {
					ext = "jpg";
				}
				if (ext.equals("pptx")) {
					ext = "ppt";
				}
				if (ext.equals("xlsx")) {
					ext = "xls";
				}
				if (ext.equals("jar")) {
					ext = "zip";
				}
				if (ext.equals("rar")) {
					ext = "zip";
				}
				if (ObjectUtil.in(ext, "", "asp", "aspx", "avi", "bmp", "doc", "docx", "exe", "fla", "flv", "folder", "gif", "html", "jpg",
						"js", "jsp", "mdb", "mov", "mp3", "mp4", "pdf", "php", "png", "ppt", "rar", "rm", "swf", "txt", "wmp", "wmv",
						"xls", "zip")) {
					return "framework/images/filetype/" + ext + ".gif";
				}
			}
		}
		return "framework/images/filetype/unknown.gif";
	}

	public static String getFileIconBySuffix(String suffix) {
		String ext = suffix;
		if (StringUtil.isNull(ext)) {
			return "framework/images/filetype/unknown.gif";
		}
		if (ext.equals("htm") || ext.equals("shtml")) {
			ext = "html";
		}
		if (ext.equals("jpeg")) {
			ext = "jpg";
		}
		if (ext.equals("pptx")) {
			ext = "ppt";
		}
		if (ext.equals("xlsx")) {
			ext = "xls";
		}
		if (ext.equals("jar")) {
			ext = "zip";
		}
		if (ext.equals("rar")) {
			ext = "zip";
		}
		if (ObjectUtil.in(ext, "", "asp", "aspx", "avi", "bmp", "doc", "docx", "exe", "fla", "flv", "folder", "gif", "html", "jpg", "js",
				"jsp", "mdb", "mov", "mp3", "mp4", "pdf", "php", "png", "ppt", "rar", "rm", "swf", "txt", "wmp", "wmv", "xls", "zip")) {
			return "framework/images/filetype/" + ext + ".gif";
		}
		return "framework/images/filetype/unknown.gif";
	}

	public static String getUserRealName(String userName) {
		if (ObjectUtil.empty(userName)) {
			return "";
		}
		ZDUser user = PlatformCache.getUser(userName);
		if (user == null) {
			return "";
		} else {
			return user.getRealName();
		}
	}

	public static List<String> getRoleCodesByUserName(String userName) {
		String roles = (String) CacheManager.get(PlatformCache.ProviderID, PlatformCache.Type_UserRole, userName);
		if (roles == null) {
			return null;
		}
		String[] arr = roles.split(",");
		Set<String> set = new HashSet<String>();
		for (String seg : arr) {
			if (StringUtil.isNotEmpty(seg)) {
				set.add(seg);
			}
		}
		ArrayList<String> list = new ArrayList<String>();
		if (set.size() > 0) {
			list.addAll(set);
			return list;
		}
		return null;
	}

	public static String getRoleName(String roleCode) {
		ZDRole role = (ZDRole) CacheManager.get(PlatformCache.ProviderID, PlatformCache.Type_Role, roleCode);
		if (role == null) {
			return null;
		}
		return LangUtil.decode(role.getRoleName());
	}

	public static String getRoleNames(List<String> roleCodes) {
		if (roleCodes == null || roleCodes.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		boolean first = false;
		for (int i = 0; i < roleCodes.size(); i++) {
			if (StringUtil.isEmpty(roleCodes.get(i))) {
				continue;
			}
			String roleName = getRoleName(roleCodes.get(i));
			if (StringUtil.isNotEmpty(roleName)) {
				if (first) {
					sb.append(",");
				}
				sb.append(roleName);
				first = true;
			}
		}
		return sb.toString();
	}

	/**
	 * @param dt
	 * @param n
	 *            哪一列需要缩进
	 * @param m
	 *            根据哪一列缩进
	 * @param firstLevel
	 *            第一层级
	 * @return
	 */
	public static void indentDataTable(DataTable dt, int n, int m, int firstLevel) {
		for (int i = 0; i < dt.getRowCount(); i++) {
			int level = Integer.parseInt(dt.getString(i, m));
			StringBuilder sb = new StringBuilder();
			for (int j = firstLevel; j < level; j++) {
				sb.append(INDENT);
			}
			dt.set(i, n, sb.toString() + dt.getString(i, n));
		}
	}

	/**
	 * 载入数据库配置中的配置项
	 */
	public static void loadDBConfig() {
		synchronized (mutex) {
			if (Config.getMapx().containsKey("Database.Default.Type")) {
				if (Config.isInstalled()) {
					try {
						DataTable dt = new Q("select code,value from zdconfig").fetch();
						for (int i = 0; dt != null && i < dt.getRowCount(); i++) {
							Config.getMapx().put(dt.getString(i, 0), dt.getString(i, 1));
						}
						Config.getMapx().put("AdminUserName", AdminUserName.getValue());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * 提供给外部调用更新 在管理后台修改后直接调用更新
	 */
	public static void refresh() {
		CacheSyncUtil.refresh(CONFIG_PROVIDER, null);
		loadDBConfig();
	}
}
