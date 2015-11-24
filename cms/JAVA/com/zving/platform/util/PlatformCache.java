package com.zving.platform.util;

import java.util.Map;

import com.zving.framework.cache.CacheDataProvider;
import com.zving.framework.cache.CacheManager;
import com.zving.framework.collection.CacheMapx;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.security.Privilege;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.meta.MetaModel;
import com.zving.schema.ZDBranch;
import com.zving.schema.ZDMetaModel;
import com.zving.schema.ZDPrivilege;
import com.zving.schema.ZDRole;
import com.zving.schema.ZDUser;
import com.zving.schema.ZDUserRole;

/**
 * 平台相关的缓存项，包括用户、角色、用户角色关联 <br>
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2008-10-5
 */
public class PlatformCache extends CacheDataProvider {
	public static final String ProviderID = "Platform";
	public static final String Type_UserRole = "UserRole";
	public static final String Type_User = "User";
	public static final String Type_Role = "Role";
	public static final String Type_Branch = "Branch";
	public static final String Type_MetaModel = "MetaModel";
	public static final String Type_MetaModel_Code = "MetaModel_Code";
	public static final String Type_RolePriv = "RolePriv";
	public static final String Type_BranchPriv = "BranchPriv";

	@Override
	public String getExtendItemID() {
		return ProviderID;
	}

	@Override
	public String getExtendItemName() {
		return "平台缓存";
	}

	@Override
	public void onKeyNotFound(String type, String key) {
		if (type.equals(Type_UserRole)) {
			ZDUserRole dao = new ZDUserRole();
			dao.setUserName(key);
			DAOSet<ZDUserRole> set = dao.query();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < set.size(); i++) {
				if (i != 0) {
					sb.append(",");
				}
				sb.append(set.get(i).getRoleCode());
			}
			if (set.size() > 0) {
				CacheManager.set(ProviderID, type, key, sb.toString());
			} else {
				CacheManager.set(ProviderID, type, key, "");// 说明没有任何角色
			}
		}

		if (type.equals(Type_Role)) {
			ZDRole dao = new ZDRole();
			dao.setRoleCode(key);

			if (dao.fill()) {
				CacheManager.set(ProviderID, type, key, dao);
			}
		}

		if (type.equals(Type_User)) {
			ZDUser dao = new ZDUser();
			dao.setUserName(key);
			if (dao.fill()) {
				CacheManager.set(ProviderID, type, key, dao);
			}
		}
		if (type.equals(Type_Branch)) {
			ZDBranch dao = new ZDBranch();
			dao.setBranchInnerCode(key);
			if (dao.fill()) {
				CacheManager.set(ProviderID, type, key, dao);
			}
		}
		if (type.equals(Type_MetaModel)) {
			MetaModel mm = MetaModel.load(Long.parseLong(key));
			if (mm != null) {
				CacheManager.set(ProviderID, type, Long.parseLong(key), mm);
			}
		}
		if (type.equals(Type_MetaModel_Code)) {
			Map<String, Object> map = TypeMap.get(Type_MetaModel_Code);
			for (Object obj : map.values()) {
				MetaModel mm = (MetaModel) obj;
				if (mm.getDAO().getCode().equals(key)) {
					CacheManager.set(ProviderID, type, key, mm);
					return;
				}
			}
			MetaModel mm = MetaModel.load(key);
			if (mm != null) {
				CacheManager.set(ProviderID, type, key, mm);
			}
		}
		if (type.equals(Type_RolePriv)) {
			ZDPrivilege dao = new ZDPrivilege();
			dao.setOwnerType(Privilege.OwnerType_Role);
			dao.setOwner(key);
			if (dao.fill()) {
				Privilege p = new Privilege();
				p.parse(dao.getPrivs());
				CacheManager.set(ProviderID, type, key, p);
			}
		}
		if (type.equals(Type_BranchPriv)) {
			ZDPrivilege dao = new ZDPrivilege();
			dao.setOwnerType(Privilege.OwnerType_Branch);
			dao.setOwner(key);
			if (dao.fill()) {
				Privilege p = new Privilege();
				p.parse(dao.getPrivs());
				CacheManager.set(ProviderID, type, key, p);
			}
		}
	}

	@Override
	public void onTypeNotFound(String type) {
		// 最多缓存一万个
		CacheManager.setMapx(ProviderID, type, new CacheMapx<String, Object>(10000));
		if (type.equals(Type_Role)) {
			DAOSet<ZDRole> set = new ZDRole().query();
			for (int i = 0; i < set.size(); i++) {
				CacheManager.set(ProviderID, type, set.get(i).getRoleCode(), set.get(i));
			}
		}
		if (type.equals(Type_Branch)) {
			DAOSet<ZDBranch> set = new ZDBranch().query();
			for (int i = 0; i < set.size(); i++) {
				CacheManager.set(ProviderID, type, set.get(i).getBranchInnerCode(), set.get(i));
			}
		}
		if (type.equals(Type_RolePriv)) {
			ZDPrivilege dao = new ZDPrivilege();
			dao.setOwnerType(Privilege.OwnerType_Role);
			DAOSet<ZDPrivilege> set = dao.query();
			for (ZDPrivilege dao2 : set) {
				Privilege p = new Privilege();
				p.parse(dao2.getPrivs());
				CacheManager.set(ProviderID, type, dao2.getOwner(), p);
			}
		}
		if (type.equals(Type_BranchPriv)) {
			ZDPrivilege dao = new ZDPrivilege();
			dao.setOwnerType(Privilege.OwnerType_Branch);
			DAOSet<ZDPrivilege> set = dao.query();
			for (ZDPrivilege dao2 : set) {
				Privilege p = new Privilege();
				p.parse(dao2.getPrivs());
				CacheManager.set(ProviderID, type, dao2.getOwner(), p);
			}
		}
	}

	public static MetaModel getMetaModel(long id) {
		return (MetaModel) CacheManager.get(ProviderID, Type_MetaModel, id);
	}

	public static MetaModel getMetaModel(String modelCode) {
		return (MetaModel) CacheManager.get(ProviderID, Type_MetaModel_Code, modelCode);
	}

	public static void setMetaModel(MetaModel mm) {
		CacheManager.set(ProviderID, Type_MetaModel, mm.getDAO().getID(), mm);
		CacheManager.set(ProviderID, Type_MetaModel_Code, mm.getDAO().getCode(), mm);
	}

	public static void removeMetaModel(ZDMetaModel mm) {
		CacheManager.remove(ProviderID, Type_MetaModel, mm.getID());
		CacheManager.remove(ProviderID, Type_MetaModel_Code, mm.getCode());
	}

	public static ZDUser getUser(String userName) {
		return (ZDUser) CacheManager.get(ProviderID, Type_User, userName);
	}

	public static ZDRole getRole(String roleCode) {
		return (ZDRole) CacheManager.get(ProviderID, Type_Role, roleCode);
	}

	public static String getUserRole(String userName) {
		return (String) CacheManager.get(ProviderID, Type_UserRole, userName);
	}

	public static ZDBranch getBranch(String innerCode) {
		if (StringUtil.isEmpty(innerCode)) {
			innerCode = "0001";
		}
		return (ZDBranch) CacheManager.get(ProviderID, Type_Branch, innerCode);
	}

	public static void removeRole(String roleCode) {
		CacheManager.remove(ProviderID, Type_RolePriv, roleCode);
		PlatformCache pc = (PlatformCache) CacheManager.getCache(ProviderID);
		Map<String, Object> map = pc.TypeMap.get(Type_UserRole);
		if (map != null) {
			synchronized (map) {
				roleCode = "," + roleCode + ",";
				for (String key : map.keySet()) {
					String ur = "," + map.get(key) + ",";
					if (ur.indexOf(roleCode) >= 0) {
						ur = StringUtil.replaceEx(ur, roleCode, ",");
					}
					ur = ur.substring(0, ur.length() - 1);
					map.put(key, ur);
				}
			}
		}
	}

	public static void addUserRole(String userName, String roleCode) {
		String roles = (String) CacheManager.get(ProviderID, Type_UserRole, userName);
		if (StringUtil.isEmpty(roles)) {
			CacheManager.set(ProviderID, Type_UserRole, userName, roleCode);
		} else {
			CacheManager.set(ProviderID, Type_UserRole, userName, roles + "," + roleCode);
		}
	}

	public static void removeUserRole(String userName, String roleCode) {
		String roles = (String) CacheManager.get(ProviderID, Type_UserRole, userName);
		if (StringUtil.isEmpty(roles)) {
			return;
		} else {
			String ur = "," + roles + ",";
			if (ur.indexOf(roleCode) >= 0) {
				ur = StringUtil.replaceEx(ur, roleCode, ",");
			}
			ur = ur.substring(0, ur.length() - 1);
			CacheManager.set(ProviderID, Type_UserRole, userName, ur);
		}
	}
}
