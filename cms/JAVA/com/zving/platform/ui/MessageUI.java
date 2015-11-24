package com.zving.platform.ui;

import javax.servlet.http.HttpSession;

import com.zving.framework.Config;
import com.zving.framework.Constant;
import com.zving.framework.UIFacade;
import com.zving.framework.User;
import com.zving.framework.User.UserData;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.cache.CacheManager;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.i18n.Lang;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.security.PrivCheck;
import com.zving.framework.thirdparty.commons.ArrayUtils;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.NumberUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.point.AfterGetNewMessage;
import com.zving.platform.util.MessageCache;
import com.zving.preloader.facade.HttpSessionListenerFacade;
import com.zving.schema.ZDMessage;

/**
 * @Author 兰军
 * @Date 2008-01-29
 * @Mail lanjun@zving.com
 * @edit xuzhe
 */
@Alias("Message")
public class MessageUI extends UIFacade {
	@Priv
	public void init() {
	}

	@Priv
	public void initDetailDialog() {
		long id = $L("ID");
		String type = Request.getString("Type");
		if (id == 0) {
			return;
		}
		ZDMessage m = new ZDMessage();
		m.setID(id);
		if (m.fill()) {
			Request.putAll(m.toMapx());
			if ("history".equals(type)) {
				$S("UserType", "收");
				$S("FromUser", "");
			} else {
				$S("UserType", "发");
				$S("ToUser", "");
				// 更新读取标记
				if (m.getReadFlag() == 0) {
					new Q().update("ZDMessage").set("ReadFlag", "1").where("ID", id).executeNoQuery();
					Q q = new Q().select("count(1)").from("ZDMessage").where("ReadFlag", 0).and().eq("ToUser", User.getUserName());
					CacheManager.set("Message", "Count", User.getUserName(), q.executeInt());
				}
			}
		}
	}

	@Priv
	public void dg1DataBind(DataGridAction dga) {
		Q q = new Q().select("*").from("ZDMessage").where("ToUser", User.getUserName()).and().ne("DelByToUser", 1L);
		q.append(dga.getSortString());
		DataTable dt = q.fetch(dga.getPageSize(), dga.getPageIndex());
		dt.insertColumn("ReadFlagIcon");
		dt.insertColumn("ReadFlagStr");
		dt.insertColumn("Color");
		for (DataRow dr : dt) {
			long flag = dr.getLong("ReadFlag");
			if (flag != 1) {
				dr.set("ReadFlagIcon", "<img src='../../icons/icon037a7.png'>");
				dr.set("ReadFlagStr", Lang.get("Platform.Readed"));
				dr.set("Color", "red");
			} else {
				dr.set("ReadFlagIcon", "<img src='../../icons/icon037a17.png'>");
				dr.set("ReadFlagStr", Lang.get("Platform.Unread"));
				dr.set("Color", "");
			}
			String redirectUrl = dr.getString("RedirectURL");
			if (StringUtil.isNotEmpty(redirectUrl)) {
				dr.set("RedirectURL", null);
				if (redirectUrl.indexOf("?") > -1) {
					Mapx<String, String> map = StringUtil.splitToMapx(redirectUrl.substring(redirectUrl.lastIndexOf("?") + 1), "&", "=");
					if (map.containsKey("CatalogID")) {
						if (NumberUtil.isLong(map.getString("CatalogID"))) {
							if (PrivCheck.check("com.zving.cms.Catalog.Content." + map.getString("CatalogID"))) {
								dr.set("RedirectURL", "<a href=\"" + redirectUrl
										+ "\" target=\"_blank\"><img src=\"../../icons/icon403a10.png\" width=\"20\" height=\"20\" /></a>");
							}
						}
					}
				} else {
					dr.set("RedirectURL", "<a href=\"" + redirectUrl
							+ "\" target=\"_blank\"><img src=\"../../icons/icon403a10.png\" width=\"20\" height=\"20\" /></a>");
				}
			}
		}
		dga.setTotal(q);
		dga.bindData(dt);
	}

	@Priv
	public void historyDataBind(DataGridAction dga) {
		Q q = new Q("select * from ZDMessage where fromuser=? and DelByFromUser<>1 ", User.getUserName());
		q.append(dga.getSortString());
		DataTable dt = q.fetch(dga.getPageSize(), dga.getPageIndex());
		dt.insertColumn("ReadFlagIcon");
		dt.insertColumn("ReadFlagStr");
		dt.insertColumn("Color");
		for (DataRow dr : dt) {
			long flag = dr.getLong("ReadFlag");
			if (flag != 1) {
				dr.set("ReadFlagIcon", "<img src='../../icons/icon037a7.png'>");
				dr.set("ReadFlagStr", Lang.get("Platform.Readed"));
				dr.set("Color", "red");
			} else {
				dr.set("ReadFlagIcon", "<img src='../../icons/icon037a17.png'>");
				dr.set("ReadFlagStr", Lang.get("Platform.Unread"));
				dr.set("Color", "");
			}
			if (StringUtil.isNotEmpty(dr.getString("RedirectURL"))) {
				dr.set("RedirectURL", "<a href=\"" + dr.getString("RedirectURL")
						+ "\" target=\"_blank\"><img src=\"../../icons/icon403a10.png\" width=\"20\" height=\"20\" /></a>");
			}
		}
		dga.setTotal(q);
		dga.bindData(dt);
	}

	/**
	 * 本方法非常特殊，会在JS中定时调用，如果不特殊处理会导致后台页面永不失效的问题。
	 */
	@Priv(login = false)
	public void getNewMessage() {
		if (!Config.isInstalled()) {// 有可能是重新安装了
			$S(Constant.ResponseScriptAttr, "window.location=\"" + Config.getContextPath() + "install.zhtml\";");
			return;
		}
		HttpSession session = HttpSessionListenerFacade.getSession($V("SessionID"));
		if (session == null) {
			$S("LogoutFlag", "Y");
			return;
		}
		try {
			User.setCurrent((UserData) session.getAttribute(Constant.UserAttrName));// 正确识别当前用户
		} catch (Throwable e) {
			LogUtil.warn("Message.getNewMessage():" + e.getMessage());
			return;
		}
		$S("Count", MessageCache.getNoReadCount());
		String message = MessageCache.getFirstPopMessage();
		if (StringUtil.isEmpty(message)) {
			$S("PopFlag", "N");
		} else {
			$S("Message", message);
			$S("PopFlag", "Y");
		}
		ExtendManager.invoke(AfterGetNewMessage.ID, new Object[] { this });
	}

	/**
	 * 弹出消息时点击“我知道了”调用本方法
	 */
	@Priv
	public void updateReadFlag() {
		long id = $L("_Param0");
		Q q = new Q().update("ZDMessage").set("ReadFlag", "1").where("ID", id);
		q.executeNoQuery();
		int count = (Integer) CacheManager.get("Message", "Count", User.getUserName());
		CacheManager.set("Message", "Count", User.getUserName(), count - 1);

		DAOSet<ZDMessage> set = new ZDMessage().query(new Q("where ID=?", id));
		MessageCache.removeIDs(set);
	}

	@Priv
	public void add() {
		String[] userList = null;
		if (StringUtil.isNotEmpty($V("ToUser"))) {
			userList = $V("ToUser").split(",");
		}
		if (StringUtil.isNotEmpty($V("ToRole"))) {
			String[] roleList = $V("ToRole").split(",");
			if (roleList.length > 0) {
				String roles = "";
				for (int j = 0; j < roleList.length; j++) {
					if (StringUtil.isNotEmpty(roleList[j])) {
						if (j == 0) {
							roles += "'" + roleList[j] + "'";
						} else {
							roles += ",'" + roleList[j] + "'";
						}
					}
				}
				if (StringUtil.isNotEmpty(roles)) {
					DataTable dt = new Q().select("UserName").from("ZDUserRole").where().in("RoleCode", roles).fetch();
					for (int k = 0; k < dt.getRowCount(); k++) {
						String userName = dt.getString(k, "UserName");
						if (!(User.getUserName().equals(userName) || ArrayUtils.contains(userList, userName))) {
							userList = ArrayUtils.add(userList, userName);
						}
					}
				}
			}
		}
		if (MessageCache.addMessage($V("Subject"), $V("Content"), userList, User.getUserName())) {
			success(Lang.get("Common.AddSuccess"));
		} else {
			fail(Lang.get("Common.AddFailed"));
		}
	}

	@Priv
	public void reply() {
		String toUser = $V("ToUser");
		if (MessageCache.addMessage($V("Subject"), $V("Content"), toUser)) {
			success(Lang.get("Platform.ReplySuccess"));
		} else {
			fail(Lang.get("Common.ExecuteFailed"));
		}
	}

	@Priv
	public Mapx<String, Object> replyInit() {
		String ID = $V("ID");
		DataTable dt = new Q().select("*").from("ZDMessage").where("ID", ID).fetch();
		Request.put("ToUser", dt.getString(0, "FromUser"));
		Request.put("Subject", Lang.get("Platform.Reply") + ":" + StringUtil.clearHtmlTag(dt.getString(0, "Subject")));
		return Request;
	}

	@Priv
	public void del() {
		String ids = $V("IDs");
		String userType = $V("UserType");
		Transaction tran = new Transaction();
		if (StringUtil.isEmpty(ids)) {
			fail(Lang.get("Common.InvalidID"));
		}
		if (StringUtil.isEmpty(userType) || !userType.equals("FromUser") && !userType.equals("ToUser")) {
			fail(Lang.get("Common.ExecuteFailed"));
			return;
		}
		DAOSet<ZDMessage> set = new ZDMessage().query(new Q().where().in("ID", ids));
		if (set.size() > 0) {
			for (int i = 0; i < set.size(); i++) {
				if (userType.equals("FromUser")) {
					set.get(i).setDelByFromUser(1);
				}
				if (userType.equals("ToUser")) {
					set.get(i).setDelByToUser(1);
					set.get(i).setReadFlag(1);
				}
			}
			tran.update(set);
			if (tran.commit()) {
				success(Lang.get("Common.DeleteSuccess"));
			} else {
				fail(Lang.get("Common.DeleteFailed"));
				return;
			}
		}
		set = new ZDMessage().query(new Q().where().in("ID", ids).and().braceLeft().eq("DelByFromUser", "1").and().eq("DelByToUser", "1")
				.or().eq("FromUser", "SYSTEM").braceRight());
		if (set.size() > 0) {
			tran.deleteAndBackup(set);
			if (tran.commit()) {
				MessageCache.removeIDs(set);
				Q q = new Q().select("count(1)").from("ZDMessage").where("ReadFlag", "0").and().eq("ToUser", User.getUserName());
				CacheManager.set("Message", "Count", User.getUserName(), q.executeInt());
			}
		}
	}

	@Priv
	public void setReadFlag() {
		String ids = $V("IDs");
		DAOSet<ZDMessage> set = new ZDMessage().query(new Q().where("ReadFlag", "0").and().in("ID", ids));
		Q q = new Q().update("ZDMessage").set("ReadFlag", "1").where("ReadFlag", "0").and().in("ID", ids);
		q.executeNoQuery();
		success(Lang.get("Platform.MarkSuccess"));
		MessageCache.removeIDs(set);
		q = new Q().select("count(1)").from("ZDMessage").where("ReadFlag", "0").and().eq("ToUser", User.getUserName());
		CacheManager.set("Message", "Count", User.getUserName(), q.executeInt());
	}
}
