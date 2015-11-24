package com.zving.platform.bl;

import java.util.Date;

import com.zving.framework.Current;
import com.zving.framework.RequestData;
import com.zving.framework.ResponseData;
import com.zving.framework.User;
import com.zving.framework.cache.CacheManager;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.i18n.Lang;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.security.Privilege;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.config.AdminUserName;
import com.zving.platform.privilege.AbstractMenuPriv;
import com.zving.platform.service.MenuPrivService;
import com.zving.platform.util.PlatformCache;
import com.zving.schema.ZDBranch;
import com.zving.schema.ZDPrivilege;
import com.zving.schema.ZDUser;

/**
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-6-15
 */
public class PrivBL {
	/**
	 * 返回指定type和id的权限集合
	 */
	public static Privilege getPrivilege(String type, String id) {
		Privilege p = new Privilege();
		if (Privilege.OwnerType_Role.equals(type) && ObjectUtil.notEmpty(id)) {
			p = getRolePriv(id);
		}
		if (Privilege.OwnerType_Branch.equals(type) && ObjectUtil.notEmpty(id)) {
			p = getBranchPriv(id);
		}
		if (Privilege.OwnerType_User.equals(type) && ObjectUtil.notEmpty(id)) {
			ZDUser user = PlatformCache.getUser(id);
			Privilege bp = getBranchPriv(user.getBranchInnerCode());
			ZDPrivilege privilege = new ZDPrivilege();
			privilege.setOwnerType(Privilege.OwnerType_User);
			privilege.setOwner(id);
			if (privilege.fill()) {
				String privs = privilege.getPrivs();
				p.parse(privs);
			}
			if (!getFullPrivFlag(Privilege.OwnerType_Branch, user.getBranchInnerCode())) {
				p.intersect(bp);
			}
			String roleCodes = PlatformCache.getUserRole(id);
			for (String code : StringUtil.splitEx(roleCodes, ",")) {
				if (ObjectUtil.empty(code)) {
					continue;
				}
				Privilege p2 = getRolePriv(code);
				if (p2 != null) {
					p.union(p2);
				}
			}
		}
		if (p == null) {// 未为用户或者角色设置过权限
			p = new Privilege();
		}
		return p;
	}

	/**
	 * 获得初始化Script
	 */
	public static String getInitScript(String type, String id) {
		StringBuilder sb = new StringBuilder();
		sb.append("var OwnPriv = new Privilege(" + PrivBL.getPrivilege(type, id) + ");\n");
		sb.append("var UncheckablePriv = new Privilege(" + getUncheckablePrivilege(type, id) + ");\n");
		if (getFullPrivFlag(type, id)) {// 对于最全权限拥有机构或者用户,所有权限都不可编辑
			sb.append("_FullPrivFlag = true;\n");
		}
		return sb.toString();
	}

	/**
	 * 不可选择的项输出成JS
	 */
	public static Privilege getUncheckablePrivilege(String type, String id) {
		Privilege p = new Privilege();
		if (Privilege.OwnerType_User.equals(type) && ObjectUtil.notEmpty(id)) {
			// 将继承自角色的权限放到UncheckableMap中
			String roleCodes = PlatformCache.getUserRole(id);
			for (String code : StringUtil.splitEx(roleCodes, ",")) {
				Privilege p2 = getRolePriv(code);
				if (p2 != null) {
					p.union(p2);
				}
			}
		}
		return p;
	}

	/**
	 * 判断指定对象是否具有全部的权限
	 */
	public static boolean getFullPrivFlag(String type, String id) {
		boolean fullPrivFlag = false;
		if (Privilege.OwnerType_User.equals(type)) {
			fullPrivFlag = fullPrivFlag || AdminUserName.getValue().equals(id);
		}
		if (Privilege.OwnerType_Branch.equals(type)) {
			fullPrivFlag = fullPrivFlag || PlatformCache.getBranch(id).getTreeLevel() == 1;
		}
		return fullPrivFlag;
	}

	/**
	 * 获得指定的对象的DAO
	 */
	public static ZDPrivilege getDAO(String type, String id) {
		ZDPrivilege privilege = new ZDPrivilege();
		privilege.setOwnerType(type);
		privilege.setOwner(id);
		return privilege;
	}

	/**
	 * 获得当前的机构权限
	 */
	private static ZDBranch getBranch(String type, String id) {
		String branchInnerCode = null;
		if (Privilege.OwnerType_Role.equals(type) && ObjectUtil.notEmpty(id)) {
			branchInnerCode = PlatformCache.getRole(id).getBranchInnerCode();
		}
		if (Privilege.OwnerType_Branch.equals(type) && ObjectUtil.notEmpty(id)) {
			String ub = User.getBranchInnerCode();
			if (id.equals(ub)) {
				branchInnerCode = ub;
			} else {
				branchInnerCode = id.substring(0, id.length() - 4);
			}
		}
		if (Privilege.OwnerType_User.equals(type) && ObjectUtil.notEmpty(id)) {
			ZDUser user = new ZDUser();
			user.setUserName(id);
			if (user.fill()) {
				branchInnerCode = user.getBranchInnerCode();
			}
		}
		return PlatformCache.getBranch(branchInnerCode);
	}

	/**
	 * 初始化当前上下文变量。因为权限项会在前台页面中多次展现，所以需要将一些变量缓存到上下文中，以提高性能。
	 */
	private static void initCurrent(String type, String id) {
		Privilege p = getPrivilege(type, id);
		Current.put("_CurrentPriv", p);
		Current.put("_CurrentBranchPriv", getBranchPrivilegeRange(type, id));
		Current.put("_FullPrivFlag", getFullPrivFlag(type, id));
		ZDBranch branch = getBranch(type, id);
		boolean flag = false;
		if (branch != null) {
			flag = getFullPrivFlag(Privilege.OwnerType_Branch, branch.getBranchInnerCode());
		}
		Current.put("_BranchFullPrivFlag", flag);
	}

	/**
	 * 权限item是否在当前操作对象所属机构允许的权限范围之内
	 */
	public static boolean isInBranchPrivRange(String type, String id, String item) {
		Privilege p = (Privilege) Current.get("_CurrentBranchPriv");
		if (p == null) {
			initCurrent(type, id);
			p = (Privilege) Current.get("_CurrentBranchPriv");
		}
		if ((Boolean) Current.get("_BranchFullPrivFlag") == true) {
			return true;
		}
		if (p != null && p.hasPriv(item)) {
			return true;
		}
		return false;
	}

	/**
	 * 获取缓存在上下文中的当前操作对象的权限集合
	 */
	public static Privilege getCurrentPrivilege(String type, String id) {
		Privilege p = (Privilege) Current.get("_CurrentPriv");
		if (p == null) {
			initCurrent(type, id);
			p = (Privilege) Current.get("_CurrentPriv");
		}
		if (p == null) {
			p = new Privilege();
		}
		return p;
	}

	/**
	 * 保存权限项,filter参数用于判断哪些项需要先清空
	 */
	public static void save(RequestData request, ResponseData response) {
		String id = request.getString("ID");
		String type = request.getString("Type");
		@SuppressWarnings("unchecked")
		Mapx<String, String> map = (Mapx<String, String>) request.get("Data");
		DAOSet<ZDPrivilege> set = new DAOSet<ZDPrivilege>();
		Transaction tran = new Transaction();
		setPriv(set, id, type, tran, map);
		if (tran.commit()) {
			for (ZDPrivilege p : set) { // 更新机构和角色的权限缓存
				if (Privilege.OwnerType_Role.equals(p.getOwnerType())) {
					CacheManager.remove(PlatformCache.ProviderID, PlatformCache.Type_RolePriv, p.getOwner());
				} else if (Privilege.OwnerType_Branch.equals(p.getOwnerType())) {
					CacheManager.remove(PlatformCache.ProviderID, PlatformCache.Type_BranchPriv, p.getOwner());
				}
			}
			response.setSuccessMessage(Lang.get("Common.SaveSuccess"));
		} else {
			response.setFailedMessage(Lang.get("Common.SaveFailed") + tran.getExceptionMessage());
		}
	}

	public static void setPriv(DAOSet<ZDPrivilege> set, String id, String type, Transaction tran, Mapx<String, String> map) {
		ZDPrivilege privilege = PrivBL.getDAO(type, id);
		if (privilege.fill()) {
			Privilege p = new Privilege();
			p.parse(privilege.getPrivs());
			for (String key : map.keySet()) {
				if (map.getInt(key) == 0) {
					p.remove(key);
				} else {
					p.add(key);
				}
			}
			privilege.setPrivs(p.toString());
			privilege.setModifyTime(new Date());
			privilege.setModifyUser(User.getUserName());
			// tran.backup(privilege);
			tran.update(privilege);
			set.add(privilege);
			// 如果是修改机构权限，则将本次取消选中的权限项从子机构、机构下用户、机构下角色的权限中去掉
			if (Privilege.OwnerType_Branch.equals(type)) { // 如果是机构权限，则子机构、角色、用户都需要修改(去掉多余的权限)
				Q q = new Q().where();
				// 获取子机构
				q.braceLeft().eq("OwnerType", type).and().likeRight("Owner", id).and().ne("Owner", id).braceRight();
				// 获取机构下角色
				q.or().exists().braceLeft().select("1").from("ZDRole").where().likeRight("BranchInnerCode", id);
				q.and().eq("ZDPrivilege.OwnerType", Privilege.OwnerType_Role).and().append(" ZDPrivilege.Owner=RoleCode").braceRight();
				// 获取机构下用户
				q.or().exists().braceLeft().select("1").from("ZDUser").where().likeRight("BranchInnerCode", id);
				q.and().eq("ZDPrivilege.OwnerType", Privilege.OwnerType_User).and().append(" ZDPrivilege.Owner=UserName").braceRight();
				DAOSet<ZDPrivilege> childSet = new ZDPrivilege().query(q);
				if (ObjectUtil.notEmpty(childSet)) {
					for (ZDPrivilege child : childSet) {
						Privilege priv = new Privilege();
						priv.parse(child.getPrivs());
						for (String key : map.keySet()) {
							if (map.getInt(key) == 0) {
								priv.remove(key);
							}
						}
						child.setPrivs(priv.toString());
						child.setModifyTime(new Date());
						child.setModifyUser(User.getUserName());
					}
					// tran.backup(childSet);
					tran.update(childSet);
					set.addAll(childSet);// 加入set刷新缓存
				}
			}
		} else {
			Privilege p = new Privilege();
			for (String key : map.keySet()) {
				if (map.getInt(key) != 1) {
					continue;
				}
				p.put(Privilege.Flag_Allow, key);
			}
			privilege.setPrivs(p.toString());
			privilege.setAddTime(new Date());
			privilege.setAddUser(User.getUserName());
			tran.insert(privilege);
			set.add(privilege);
		}
	}

	/**
	 * 获取角色的权限
	 */
	public static Privilege getRolePriv(String roleCode) {
		if (StringUtil.isEmpty(roleCode)) {
			return null;
		}
		return (Privilege) CacheManager.get(PlatformCache.ProviderID, PlatformCache.Type_RolePriv, roleCode);
	}

	/**
	 * 获取机构的权限
	 */
	public static Privilege getBranchPriv(String branchInnerCode) {
		return (Privilege) CacheManager.get(PlatformCache.ProviderID, PlatformCache.Type_BranchPriv, branchInnerCode);
	}

	/**
	 * 获得当前的机构权限集合
	 */
	public static Privilege getBranchPrivilegeRange(String type, String id) {
		Privilege p = null;
		ZDBranch b = getBranch(type, id);
		if (b != null && b.getTreeLevel() > 1) {// 顶级机构没有缓存权限
			p = getBranchPriv(b.getBranchInnerCode());
			if (!AdminUserName.getValue().equals(User.getUserName())) {
				Privilege p2 = new Privilege();
				p2.union(p);
				p2.intersect(User.getPrivilege());
				p = p2;
			}
		}
		if (p == null) {
			p = new Privilege();
		}
		return p;
	}

	/**
	 * 获取用户的权限。用户的权限=（用户本身的权限与所在机构权限的交集）+用户所拥有的所有角色的合集
	 */
	public static Privilege getUserPriv(String userName) {
		if (StringUtil.isEmpty(userName)) {
			return null;
		}
		ZDUser user = PlatformCache.getUser(userName);
		if (user == null) {
			return null;
		}
		Privilege p = new Privilege();
		if (AdminUserName.getValue().equals(userName)) {
			for (AbstractMenuPriv priv : MenuPrivService.getInstance().getAll()) {
				p.put(Privilege.Flag_Allow, priv.getExtendItemID());
				for (String privID : priv.getPrivItems().keySet()) {
					p.put(Privilege.Flag_Allow, privID);
				}
			}
		} else {
			Privilege bp = getBranchPriv(user.getBranchInnerCode());
			p = new Privilege();
			ZDPrivilege dao = new ZDPrivilege();
			dao.setOwnerType(Privilege.OwnerType_User);
			dao.setOwner(userName);
			if (dao.fill()) {
				String priv = dao.getPrivs();
				p.parse(priv);
			}
			if (!getFullPrivFlag(Privilege.OwnerType_Branch, user.getBranchInnerCode())) {
				p.intersect(bp);
			}
			String roleCodes = PlatformCache.getUserRole(userName);
			for (String roleCode : StringUtil.splitEx(roleCodes, ",")) {
				if (ObjectUtil.empty(roleCode)) {
					continue;
				}
				Privilege p2 = getRolePriv(roleCode);
				p.union(p2);
			}
		}
		return p;
	}

}
