package com.zving.platform.ui;

import java.io.IOException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.zving.cxdata.bl.LdapBL;
import com.zving.framework.Config;
import com.zving.framework.Constant;
import com.zving.framework.Current;
import com.zving.framework.SessionListener;
import com.zving.framework.UIFacade;
import com.zving.framework.User.UserData;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.data.Q;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.i18n.Lang;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.security.LicenseInfo;
import com.zving.framework.security.PasswordUtil;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.ServletUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.bl.UserBL;
import com.zving.platform.code.Enable;
import com.zving.platform.code.YesOrNo;
import com.zving.platform.config.AdminUserName;
import com.zving.platform.handler.AuthCodeURLHandler;
import com.zving.platform.point.AfterSSOLogin;
import com.zving.platform.util.ExpiringSet;
import com.zving.preloader.facade.HttpSessionListenerFacade;
import com.zving.schema.ZDUser;

/**
 * @Author 王育春
 * @Date 2007-6-18
 * @Mail wyuch@zving.com
 */
@Alias("Login")
public class LoginUI extends UIFacade {

	private static Set<String> wrongList = new ExpiringSet<String>();

	@Priv(login = false)
	public void init() {
		$S("Title", Config.getAppName());
		if (LicenseUI.needWarning()) {
			$S("LicenseWarning", Lang.get("Login.LicenseWarning", new Object[] { DateUtil.toString(LicenseInfo.getEndDate()) }) + "<br>");
		} else {
			$S("LicenseWarning", "");
		}
		if (LicenseUI.nearEndDateWarning()) {
			$S("LinceseNearEndWarning",
					Lang.get("Login.LinceseNearEndWarning", new Object[] { DateUtil.toString(LicenseInfo.getEndDate()) }));
		} else {
			$S("LinceseNearEndWarning", "");
		}
	}

	public static void ssoLogin(HttpServletRequest request, HttpServletResponse response, UIFacade ui) {
		String username = ui.$V("u");
		if (username == null) {
			return;
		}
		if (Config.isFrontDeploy()) {
			return;
		}
		ZDUser user = new ZDUser();
		user.setUserName(username);
		DAOSet<ZDUser> userSet = user.query();
		if (!Config.isAllowLogin() && !username.equalsIgnoreCase(AdminUserName.getValue())) {
			return;
		}
		if (userSet == null || userSet.size() < 1) {

		} else {
			user = userSet.get(0);
			String ip = ServletUtil.getRealIP(request);
			Current.getRequest().setClientIP(ip);

			// 踢掉同用户名的其他用户
			UserData[] loggedUsers = SessionListener.getUsers(user.getUserName());
			for (UserData u : loggedUsers) {
				HttpSession session = HttpSessionListenerFacade.getSession(u.getSessionID());
				if (ObjectUtil.notEmpty(session)) {
					session.invalidate();
				}
			}

			UserBL.login(user);
			ExtendManager.invoke(AfterSSOLogin.ExtendPointID, new Object[] { ui });
			try {
				String path = request.getParameter("Referer");
				LogUtil.info("SSOLogin,Referer:" + path);
				if ("JSONP".equalsIgnoreCase(request.getParameter("j"))) {
					String serverUrl = request.getParameter("serverUrl");
					if (StringUtil.isNotEmpty(path)) {
						if (StringUtil.isNotEmpty(request.getParameter("t")) && !"null".equalsIgnoreCase(request.getParameter("t"))) {
							response.getWriter().write(
									StringUtil.concat("window.location.href = '", serverUrl, path, "?t=", request.getParameter("t"), "';"));
						} else {
							response.getWriter().write(StringUtil.concat("window.location.href = '", serverUrl, path, "';"));
						}
					} else {
						response.getWriter().write(StringUtil.concat("window.location.href = '", serverUrl, "application.zhtml';"));
					}
				} else {
					if (StringUtil.isNotEmpty(path)) {
						if (StringUtil.isNotEmpty(request.getParameter("t")) && !"null".equalsIgnoreCase(request.getParameter("t"))) {
							response.sendRedirect(path + "?t=" + request.getParameter("t"));
						} else {
							response.sendRedirect(path);
						}
					} else {
						response.sendRedirect("application.zhtml");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Priv(login = false)
	public void needVerifyCode() {
		String username = $V("UserName");
		if (StringUtil.isNotEmpty(username) && wrongList.contains(username)) {
			$S("Need", true);
		} else {
			$S("Need", false);
		}
	}

	@Priv(login = false)
	public void submit() {
		if (Config.isFrontDeploy()) {
			return;
		}
		String username = $V("UserName");
		String verifyCode = $V("VerifyCode");
		boolean isVerifyCode = Request.getBoolean("IsVerifyCode");
		// 如果用户名在wrongList中并且没有验证码的时候提示请输入验证码！
		if (wrongList.contains(username) && StringUtil.isEmpty(verifyCode)) {
			fail(Lang.get("Platform.PleaseInputVerifyCode"));
			return;
		}
		if (isVerifyCode) {
			if (!AuthCodeURLHandler.verify(verifyCode)) {
				Response.put("Focus", "VerifyCode");
				fail(Lang.get("Common.InvalidVerifyCode"));
				return;
			}
		}
		
		ZDUser user = new ZDUser();
		user.setUserName(username);
		if (!Config.isAllowLogin() && !user.getUserName().equalsIgnoreCase(AdminUserName.getValue())) {
			fail(Lang.get("User.DenyLoginTemp"));
			return;
		}
		
		boolean isLdapLogin = "ldap".equals($V("loginType"));
		if (isLdapLogin) {
			if (!LdapBL.authUser(username, $V("Password"))) {
				fail(Lang.get("LDAP用户名密码错误"));
				return;
			}
		}
		
		DAOSet<ZDUser> userSet = user.query(new Q(" where UserName=?", username));
		if (userSet == null || userSet.size() < 1) {
			if (isLdapLogin) {
				fail("系统未配置用户 <b>"+ username +"</b> 的权限");
				return;
			}
			fail(Lang.get("Common.UserNameOrPasswordWrong"));
			wrongList.add(username);
			return;
		} else {
			user = userSet.get(0);
			if (!isLdapLogin && !PasswordUtil.verify($V("Password"), user.getPassword())) {
				Response.put("Focus", "Password");
				fail(Lang.get("Common.UserNameOrPasswordWrong"));
				wrongList.add(username);
				return;
			}
			if (!AdminUserName.getValue().equalsIgnoreCase(user.getUserName()) && Enable.isDisable(user.getStatus())) {
				fail(Lang.get("User.UserStopped"));
				return;
			}
			UserData[] loggedUsers = SessionListener.getUsers(user.getUserName());
			if (ObjectUtil.notEmpty(loggedUsers)) {// 处理单用户登录
				if (YesOrNo.isYes($V("logout"))) {
					for (UserData u : loggedUsers) {
						HttpSession session = HttpSessionListenerFacade.getSession(u.getSessionID());
						if (ObjectUtil.notEmpty(session)) {
							session.invalidate();
						}
					}
				} else if (Request.getClientIP().equals(user.getLastLoginIP())) {
					for (UserData u : loggedUsers) {
						HttpSession session = HttpSessionListenerFacade.getSession(u.getSessionID());
						if (ObjectUtil.notEmpty(session)) {
							session.invalidate();
						}
					}
				} else {
					Response.setStatus(10000);// 前台来响应此状态
					return;
				}
			}
			UserBL.login(user, isLdapLogin);
			Response.setStatus(1);
			wrongList.remove(username);
			$S(Constant.ResponseScriptAttr, "window.location=\"application.zhtml\";");
		}
	}
}
