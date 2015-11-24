package com.zving.platform.ui;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zving.framework.Config;
import com.zving.framework.Constant;
import com.zving.framework.Current;
import com.zving.framework.UIFacade;
import com.zving.framework.User;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.collection.Filter;
import com.zving.framework.collection.Mapx;
import com.zving.framework.core.handler.ZAction;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.i18n.Lang;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.security.PasswordUtil;
import com.zving.framework.security.PrivCheck;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.ui.tag.ListAction;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.bl.EntrustBL;
import com.zving.platform.bl.LogBL;
import com.zving.platform.config.AdminUserName;
import com.zving.platform.log.type.SecurityLog;
import com.zving.platform.log.type.UserLog;
import com.zving.platform.service.MenuPrivService;
import com.zving.schema.ZDUser;

/**
 * @Author 王育春
 * @Date 2008-1-18
 * @Mail wyuch@zving.com
 */
@Alias("Application")
public class ApplicationUI extends UIFacade {

	/**
	 * 初始化以获取站点、菜单权限
	 */
	@Priv
	public void init() {
		$S("Privs", User.getPrivilege().toString());// 传递权限字符串到前台
		$S("AdminUserName", AdminUserName.getValue());
		if (LangUtil.getSupportedLanguages().size() > 1) {
			$S("MultiLanguage", true);
		}

		if (ObjectUtil.notIn(User.getLanguage(), "en", "zh-cn")) {
			if (User.getLanguage().equals("zh-tw")) {
				$S("Language", "zh-cn");
			} else {
				$S("Language", "en");// 防止没有LOGO图片
			}
		} else {
			$S("Language", User.getLanguage());
		}
		$S("AppName", Config.getAppName());
		$S("SessionID", User.getSessionID());
	}

	private static Pattern PatternIcon = Pattern.compile("[^\"]*icons\\/([^\"\\/]+)\\.png", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private static String getImgSpirite(String imgSrc) {
		Matcher matcher = PatternIcon.matcher(imgSrc);
		String imgTag;
		if (matcher.find()) {
			String fileName = matcher.group(1);
			imgSrc = imgSrc.replaceAll(fileName, "icon000");
			imgTag = "<img src=\"" + imgSrc + "\" class=\"" + fileName + "\" />";
		} else {
			imgTag = "<img src=\"" + imgSrc + "\" />";
		}
		return imgTag;
	}

	@Priv
	public void bindMainMenus(ListAction la) {
		DataTable dt = MenuPrivService.getMainMenus();
		dt.setWebMode(false);
		dt = dt.filter(new Filter<DataRow>() {
			@Override
			public boolean filter(DataRow dr) {
				return PrivCheck.check(dr.getString("id"));
			}
		});
		for (int i = 0; i < dt.getRowCount(); i++) {
			DataRow dr = dt.get(i);
			dr.set("icon", getImgSpirite(dr.getString("icon")));
		}
		la.bindData(dt);
	}

	@Priv
	public void bindChildMenus(ListAction la) {
		String parentID = la.getParentCurrentDataRow().getString("ID");
		DataTable dt = MenuPrivService.getChildMenus(parentID);
		dt.setWebMode(false);
		dt = dt.filter(new Filter<DataRow>() {
			@Override
			public boolean filter(DataRow dr) {
				return PrivCheck.check(dr.getString("id"));
			}
		});
		for (int i = 0; i < dt.getRowCount(); i++) {
			DataRow dr = dt.get(i);
			dr.set("icon", getImgSpirite(dr.getString("icon")));
		}
		la.bindData(dt);
	}

	@Priv(login = false)
	public DataTable getLanguages() {
		return Mapx.toDataTable(LangUtil.getSupportedLanguages());
	}

	@Priv(login = false)
	public void changeLanguage() {
		String lang = $V("Language");
		if (!LangUtil.getSupportedLanguages().containsKey(lang)) {
			lang = LangUtil.getDefaultLanguage();
		}
		Current.getCookies().setCookie(Constant.LanguageCookieName, lang);
		User.setLanguage(lang);
	}

	@Priv
	public void changePassword() {
		if (!$V("Password").equals($V("ConfirmPassword"))) {
			fail("两次输入的密码不一致");
			return;
		}
		String userName = User.getUserName();
		ZDUser user = new ZDUser();
		user.setUserName(userName);
		user.fill();
		if (!PasswordUtil.verify($V("OldPassword"), user.getPassword())) {
			fail("原始密码输入错误");
			return;
		}
		user.setPassword(PasswordUtil.generate($V("Password")));
		if (user.update()) {
			success(Lang.get("Common.ExecuteSuccess"));
		} else {
			fail(Lang.get("Common.ExecuteFailed"));
		}
	}

	@Priv(login = false)
	@Alias(value = "logout", alone = true)
	public void logout(ZAction za) {
		za.getRequest().getSession().invalidate();
		String language = User.getLanguage();
		User.destory();
		User.setLanguage(language);
		redirect(Config.getContextPath() + Config.getLoginPage());
	}

	@Priv
	public void changeMenuClickCount() {
		String menuID = $V("MenuID");
		if (StringUtil.isEmpty(menuID)) {
			return;
		}
		LogBL.addLog(UserLog.ID, menuID, "Visit " + menuID);
	}

	@Priv
	public void entrustInit() {
		if (EntrustBL.isAgent(User.getUserName())) {
			$S("Agent", true);
		}
		if (EntrustBL.isEntrust(User.getUserName())) {
			$S("Entrust", true);
		}
		$S("SelectedUsers", User.getUserName());
	}

	/**
	 * 委托管理，设置代理用户
	 */
	@Priv
	public void addEntrust() {
		String agentUser = $V("UserName");
		if (StringUtil.isEmpty(agentUser)) {
			fail(Lang.get("Platform.HasNoUser"));
			return;
		}
		if (agentUser.equals(User.getUserName())) {
			fail(Lang.get("Platform.CannotEntrustSelf"));
			return;
		}
		EntrustBL.entrust(User.getUserName(), agentUser, new Date(), null);
		success(Lang.get("Common.ExecuteSuccess"));
	}

	/**
	 * 取消委托
	 */
	@Priv
	public void cancelEntrust() {
		if (EntrustBL.cancel(User.getUserName())) {
			success(Lang.get("Platform.CancelEntrustSuccess"));
		} else {
			fail(Lang.get("Common.ExecuteFailed"));
		}
	}

	/**
	 * 代理用户切换登录帐号
	 */
	@Priv
	@Alias(value = "changeaccount", alone = true)
	public void changeLoginAccount(ZAction za) {
		String userName = $V("UserName").toLowerCase();
		String agentUser = User.getUserName();

		// 使用选中用户登录
		if (!EntrustBL.login(userName, agentUser)) {
			za.writeHTML("Change to entrusted account failed!");
			return;
		}
		LogBL.addLog(SecurityLog.ID, SecurityLog.SubType_Entrust, "User " + agentUser + " is now agent of " + userName);
		za.writeHTML("<script>window.location='" + Config.getContextPath() + "application.zhtml';</script>");
	}

	/**
	 * 绑定委托给当前用户的用户列表
	 */
	@Priv
	public void bindEntrustUsers(DataGridAction dga) {
		List<String> list = EntrustBL.getEntrustUsers(User.getUserName());
		DataTable dt = new DataTable();
		if (list != null) {
			dt = new Q().select("u.UserName", "u.RealName", "b.Name as BranchName").from("ZDUser u", "ZDBranch b").where()
					.eq2("b.BranchInnerCode", "u.BranchInnerCode").and().in("u.UserName", list).fetch();
		}
		dga.setTotal(dt.getRowCount());
		dga.bindData(dt);
	}

	/**
	 * 绑定被当前用户委托的用户列表
	 */
	@Priv
	public void bindAgentUser(DataGridAction dga) {
		String agentuser = EntrustBL.getAgentuser(User.getUserName());
		DataTable dt = new DataTable();
		if (StringUtil.isNotNull(agentuser)) {
			dt = new Q().select("u.UserName", "u.RealName", "b.Name as BranchName").from("ZDUser u", "ZDBranch b").where()
					.eq2("b.BranchInnerCode", "u.BranchInnerCode").and().eq("u.UserName", agentuser).fetch();
		}
		dga.setTotal(dt.getRowCount());
		dga.bindData(dt);
	}
}
