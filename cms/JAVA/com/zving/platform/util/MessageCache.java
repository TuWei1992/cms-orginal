package com.zving.platform.util;

import java.util.ArrayList;
import java.util.Date;

import com.zving.framework.User;
import com.zving.framework.cache.CacheDataProvider;
import com.zving.framework.cache.CacheManager;
import com.zving.framework.collection.CacheMapx;
import com.zving.framework.collection.Executor;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.i18n.Lang;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.utility.StringFormat;
import com.zving.framework.utility.StringUtil;
import com.zving.schema.ZDMessage;

/**
 * 短消息工具类，用于解决高并发时短消息频繁查询时性能问题<br>
 * 
 * @Author 王育春
 * @Date 2008-10-30
 * @Mail wyuch@zving.com
 */
public class MessageCache extends CacheDataProvider {
	public static final String ProviderID = "Message";

	/**
	 * 增加一条短消息，加入消息队列，默认发送人为当前用户
	 */
	public static boolean addMessage(String subject, String content, String toUser) {
		Transaction tran = new Transaction();
		addMessage(tran, subject, content, new String[] { toUser }, User.getUserName(), "", true);
		return tran.commit();
	}

	public static boolean addMessage(Transaction tran, String subject, String content, String toUser, String formUser, String RedirectURL) {
		return addMessage(tran, subject, content, new String[] { toUser }, formUser, RedirectURL, true);
	}

	public static boolean addMessage(String subject, String content, String[] userList, String fromUser) {
		Transaction tran = new Transaction();
		addMessage(tran, subject, content, userList, fromUser, "", true);
		return tran.commit();
	}

	public static boolean addMessage(Transaction tran, String subject, String content, String[] userList, String fromUser,
			String RedirectURL, final boolean popFlag) {
		ArrayList<PopMessage> list = new ArrayList<PopMessage>();
		for (int i = 0; i < userList.length; i++) {
			if (!userList[i].equals(fromUser) && StringUtil.isNotEmpty(userList[i])) {
				ZDMessage message = new ZDMessage();
				message.setID(NoUtil.getMaxID("MessageID"));
				message.setSubject(subject);
				message.setBox("outbox");
				message.setContent(content);
				message.setFromUser(fromUser);
				message.setToUser(userList[i]);
				message.setReadFlag(0);
				message.setDelByFromUser(0);
				message.setDelByToUser(0);
				message.setPopFlag(popFlag ? 0 : 1);
				message.setAddTime(new Date());
				message.setRedirectURL(RedirectURL);
				PopMessage pm = new PopMessage(message.getID(), getHtmlMessage(message.getID(), message.getSubject(), content),
						System.currentTimeMillis(), userList[i]);
				list.add(pm);
				tran.add(message, Transaction.INSERT);
			}
		}
		tran.addExecutor(new Executor(list) {
			@Override
			public boolean execute() {
				@SuppressWarnings("unchecked")
				ArrayList<PopMessage> list = (ArrayList<PopMessage>) params[0];
				for (int i = 0; i < list.size(); i++) {
					PopMessage pm = list.get(i);
					PopMessageList pml = (PopMessageList) CacheManager.get(ProviderID, "LastMessage", pm.ToUser);
					synchronized (pml) {
						if (popFlag) {
							pml.list.add(pm);
						}
					}
					String count = String.valueOf(CacheManager.get(ProviderID, "Count", pm.ToUser));
					CacheManager.set(ProviderID, "Count", pm.ToUser, Integer.parseInt(count) + 1);
				}
				return true;
			}
		});
		return true;
	}

	public static void removeIDs(DAOSet<ZDMessage> set) {
		PopMessageList pml = (PopMessageList) CacheManager.get(ProviderID, "LastMessage", User.getUserName());
		int NoReadCount = 0;
		synchronized (pml) {
			for (int j = 0; j < set.size(); j++) {
				for (int i = 0; i < pml.list.size(); i++) {
					PopMessage pm = pml.list.get(i);
					if (set.get(j).getID() == pm.ID) {
						pml.list.remove(pm);
						break;
					}
				}
				if (set.get(j).getReadFlag() == 0) {
					NoReadCount++;
				}

			}
		}
		String count = String.valueOf(CacheManager.get(ProviderID, "Count", User.getUserName()));
		CacheManager.set(ProviderID, "Count", User.getUserName(), Integer.parseInt(count) - NoReadCount);
	}

	public static int getNoReadCount() {
		String count = "" + CacheManager.get(ProviderID, "Count", User.getUserName());
		if (StringUtil.isNull(count)) {
			return 0;
		} else {
			int c = Integer.parseInt(count);
			if (c < 0) {
				CacheManager.set(ProviderID, "Count", User.getUserName(), "0");
				return 0;
			} else {
				return c;
			}
		}
	}

	/**
	 * 获取当前用户第一条弹出消息
	 */
	public static String getFirstPopMessage() {
		return getFirstPopMessage(User.getUserName());
	}

	/**
	 * 获取指定用户第一条弹出消息
	 */
	public static String getFirstPopMessage(String userName) {
		PopMessageList pml = (PopMessageList) CacheManager.get(ProviderID, "LastMessage", userName);
		if (pml == null) {
			return "";
		}
		return pml.getLastMessage();
	}

	@Override
	public String getExtendItemID() {
		return ProviderID;
	}

	@Override
	public String getExtendItemName() {
		return "@{Platform.MessageCache}";
	}

	@Override
	public void onKeyNotFound(String type, String key) {
		if (type.equals("Count")) {// 查找用户未读消息条数
			Q qb = new Q("select count(1) from ZDMessage where ReadFlag=0 and ToUser=?", key);
			int count = qb.executeInt();
			CacheManager.set(ProviderID, type, key, count);
		}
		if (type.equals("LastMessage")) {// 查找用户最新的未弹出的消息
			PopMessageList pml = new PopMessageList(key.toString());
			CacheManager.set(ProviderID, type, key, pml);
		}
	}

	@Override
	public void onTypeNotFound(String type) {
		CacheManager.setMapx(ProviderID, type, new CacheMapx<String, Object>());
	}

	/**
	 * 增加此类的原因是：<br>
	 * 1，有多台机器用同一个用户登录时，所有机器都可以收到弹出消息<br>
	 * 2，在服务器较频繁重启的情况下，也能确保所有未弹出过的消息都有机会弹出
	 */
	static class PopMessageList {
		private ArrayList<PopMessage> list = new ArrayList<PopMessage>();

		public PopMessageList(String userName) {
			long current = System.currentTimeMillis();
			Q qb = new Q("select * from ZDMessage where ReadFlag=0 and PopFlag=0 and ToUser=? order by AddTime asc", userName);
			DataTable dt = qb.fetch();
			for (int i = 0; i < dt.getRowCount(); i++) {
				String html = MessageCache.getHtmlMessage(dt.getLong(i, "ID"), dt.getString(i, "Subject"), dt.getString(i, "Content"));
				list.add(new PopMessage(dt.getLong(i, "ID"), html, current, userName));
			}
		}

		public synchronized String getLastMessage() {
			if (list.size() == 0) {
				return null;
			} else {
				for (int i = list.size() - 1; i >= 0; i--) {
					PopMessage pm = list.get(i);// 最后一个
					if (System.currentTimeMillis() - pm.LastTime > 30 * 60 * 1000) {// 30分钟后清除掉
						list.remove(pm);
					}
					if (pm.SessionIDMap.containsKey(User.getSessionID())) {
						continue;
					}
					pm.SessionIDMap.put(User.getSessionID(), "1");
					if (!pm.PopedFlag) {// 第一次弹出，以后可能其他客户端还会来取
						Q qb = new Q("update ZDMessage set PopFlag=1 where ID=?", pm.ID);
						qb.executeNoQuery();
						pm.PopedFlag = true;
					}
					return pm.Message;
				}
			}
			return null;
		}
	}

	public static String getHtmlMessage(long id, String subject, String content) {
		StringFormat sf = new StringFormat(Lang.get("Platform.Message.GetAMessage") + "：<hr>?<hr>?<br><br>?");
		sf.add(subject);
		sf.add(content);
		sf.add("<p align='center' width='100%'><input type='button' class='inputButton' value='" + Lang.get("Platform.MessagePopButton")
				+ "'" + " onclick=\"Server.getOneValue('Message.updateReadFlag'," + id + ",function(){MsgPop.closeSelf();});\"></p>");
		return sf.toString();
	}

	static class PopMessage {
		public long ID;
		public String Message;
		public long LastTime;
		public boolean PopedFlag = false;
		public String ToUser;
		public Mapx<String, String> SessionIDMap = new Mapx<String, String>();

		public PopMessage(long id, String message, long lastTime, String toUser) {
			ID = id;
			Message = message;
			LastTime = lastTime;
			ToUser = toUser;
		}
	}
}
