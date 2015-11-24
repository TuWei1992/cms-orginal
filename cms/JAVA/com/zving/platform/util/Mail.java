package com.zving.platform.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;

import com.zving.framework.Config;
import com.zving.framework.collection.Mapx;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 邮件处理类
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-3-18
 */
public class Mail {

	public final static String SUCCESS = "success"; // 发送成功

	public final static String FAILED_SEND = "failed_send"; // 发送失败

	public final static String FAILED_WRONG = "failed_wrong"; // 传值错误

	public final static String FAILED_WRONG_HOST = "failed_wrong_host"; // 邮件服务器主机发生异常

	public final static String FAILED_WRONG_NOATTACH = "failed_wrong_noattach"; // 附件不存在

	public final static String FAILED_EMPTY_TOUSER = "failed_empty_user"; // 邮件接收者为空

	public final static String FAILED_EMPTY_CONTENT = "failed_empty_content"; // 邮件内容为空

	public final static String FAILED_EMPTY_URL = "failed_empty_url"; // URL为空

	public final static Mapx<String, String> RESULT_MAP = new Mapx<String, String>();

	static {
		RESULT_MAP.put(SUCCESS + "", "发送成功");
		RESULT_MAP.put(FAILED_SEND + "", "发送失败");
		RESULT_MAP.put(FAILED_WRONG + "", "传值错误");
		RESULT_MAP.put(FAILED_WRONG_HOST + "", "邮件服务器主机地址有误");
		RESULT_MAP.put(FAILED_WRONG_NOATTACH + "", "附件不存在");
		RESULT_MAP.put(FAILED_EMPTY_TOUSER + "", "邮件接收者为空");
		RESULT_MAP.put(FAILED_EMPTY_CONTENT + "", "邮件内容为空");
		RESULT_MAP.put(FAILED_EMPTY_URL + "", "URL为空");
	}

	public static String sendSimpleEmail(Mapx<String, String> map) {
		if (map == null) {
			return FAILED_WRONG;
		}
		String host = map.getString("mail.host");
		String port = map.getString("mail.port");
		String userName = map.getString("mail.username");
		String password = map.getString("mail.password");
		if (StringUtil.isEmpty(map.getString("ToUser"))) {
			return FAILED_EMPTY_TOUSER;
		} else if (StringUtil.isEmpty(map.getString("Content"))) {
			return FAILED_EMPTY_CONTENT;
		}

		String realName = map.getString("RealName");
		if (StringUtil.isEmpty(realName)) {
			realName = map.getString("ToUser");
		}

		String subject = map.getString("Subject");
		if (StringUtil.isEmpty(subject)) {
			subject = "来自" + realName + "的系统邮件";
		}

		SimpleEmail email = new SimpleEmail();
		try {
			email.setAuthentication(userName, password);
			email.setHostName(host);
			email.setSmtpPort(Integer.parseInt(port));
			email.addTo(map.getString("ToUser"), realName);
			email.setFrom(userName);
			email.setSubject(subject);
			email.setContent(map.getString("Content"), "text/html;charset=" + Config.getGlobalCharset());
			email.send();
		} catch (EmailException e) {
			e.printStackTrace();
			return FAILED_SEND;
		}
		return SUCCESS;
	}

	public static String sendHtmlMail(Mapx<String, String> map) {// NO_UCD 最好重构一下
		if (map == null) {
			return FAILED_WRONG;
		}
		String host = map.getString("mail.host");
		String port = map.getString("mail.port");
		String userName = map.getString("mail.username");
		String password = map.getString("mail.password");
		if (StringUtil.isEmpty(map.getString("ToUser"))) {
			return FAILED_EMPTY_TOUSER;
		} else if (StringUtil.isEmpty(map.getString("URL"))) {
			return FAILED_EMPTY_URL;
		}

		String realName = map.getString("RealName");
		if (StringUtil.isEmpty(realName)) {
			realName = map.getString("ToUser");
		}

		String subject = map.getString("Subject");
		if (StringUtil.isEmpty(subject)) {
			subject = "来自" + realName + "的系统邮件";
		}

		String htmlContent = FileUtil.readURLText(map.getString("URL"));
		HtmlEmail email = new HtmlEmail();
		try {
			email.setAuthentication(userName, password);
			email.addTo(map.getString("ToUser"), realName);
			email.setHostName(host);
			email.setSmtpPort(Integer.parseInt(port));
			email.setFrom(userName);
			email.setSubject(subject);
			email.setHtmlMsg(htmlContent);
			email.send();
		} catch (EmailException e) {
			return FAILED_SEND;
		}
		return SUCCESS;
	}

	public String sendMailWithAttach(Mapx<String, String> map) {// NO_UCD 最好重构一下
		if (map == null) {
			return FAILED_WRONG;
		}
		Properties props = new Properties();
		String host = map.getString("mail.host");
		String port = map.getString("mail.port");
		String userName = map.getString("mail.username");
		String password = map.getString("mail.password");

		// 获取收件人信息
		if (StringUtil.isEmpty(map.getString("ToUser"))) {
			return FAILED_EMPTY_TOUSER;
		}

		String realName = map.getString("RealName");
		if (StringUtil.isEmpty(realName)) {
			realName = map.getString("ToUser");
		}

		String subject = map.getString("Subject");
		if (StringUtil.isEmpty(subject)) {
			subject = "来自" + realName + "的系统邮件";
		}

		String content = map.getString("Content");
		if (StringUtil.isEmpty(content)) {
			content = "您好！";
		}

		String attachPath = map.getString("AttachPath");
		String attachName = map.getString("AttachName");
		String toUser = map.getString("ToUser");

		javax.mail.Session mailSession; // 邮件会话对象
		javax.mail.internet.MimeMessage mimeMsg; // MIME邮件对象
		props = java.lang.System.getProperties(); // 获得系统属性对象
		props.put("mail.smtp.host", host); // 设置SMTP主机
		props.put("mail.smtp.port", port); // 设置SMTP端口
		props.put("mail.smtp.auth", "true"); // 是否到服务器用户名和密码验证

		// 到服务器验证发送的用户名和密码是否正确
		MailAutherticator myEmailAuther = new MailAutherticator(userName, password);

		// 设置邮件会话
		mailSession = javax.mail.Session.getInstance(props, myEmailAuther);

		try {
			// 设置传输协议
			Transport transport = mailSession.getTransport("smtp");

			// 设置from、to等信息
			mimeMsg = new javax.mail.internet.MimeMessage(mailSession);//

			if (StringUtil.isNotEmpty(userName)) {
				InternetAddress sentFrom = new InternetAddress(userName);
				mimeMsg.setFrom(sentFrom); // 设置发送人地址
			}

			InternetAddress[] sendTo = new InternetAddress[1];
			sendTo[0] = new InternetAddress(toUser);
			mimeMsg.setRecipients(RecipientType.TO, sendTo);
			mimeMsg.setSubject(subject, Config.getGlobalCharset());

			MimeBodyPart messageBodyPart1 = new MimeBodyPart();
			messageBodyPart1.setContent(content, "text/html;charset=" + Config.getGlobalCharset());

			Multipart multipart = new MimeMultipart();// 附件传输格式
			multipart.addBodyPart(messageBodyPart1);

			String[] attachs = StringUtil.splitEx(attachPath, ",");
			String[] attachNames = StringUtil.splitEx(attachName, ",");
			boolean wrongFlag = false;
			for (int i = 0; i < attachs.length; i++) {
				if (!new File(attachs[i]).exists()) {
					wrongFlag = true;
				}
				MimeBodyPart messageBodyPart = new MimeBodyPart();
				// 选择出每一个附件名
				String filename = attachs[i];
				String displayname = attachNames[i];
				// 得到数据源
				FileDataSource fds = new FileDataSource(filename);
				// 得到附件本身并至入BodyPart
				messageBodyPart.setDataHandler(new DataHandler(fds));
				// 得到文件名同样至入BodyPart
				messageBodyPart.setFileName(MimeUtility.encodeText(displayname));
				multipart.addBodyPart(messageBodyPart);
			}

			if (wrongFlag) {
				return FAILED_WRONG_NOATTACH;
			}

			mimeMsg.setContent(multipart);
			// 设置信件头的发送日期
			mimeMsg.setSentDate(new Date());
			mimeMsg.saveChanges();
			// 发送邮件
			Transport.send(mimeMsg);
			transport.close();
		} catch (NoSuchProviderException e) {
			return FAILED_WRONG_HOST;
		} catch (AddressException e) {
			return FAILED_WRONG_HOST;
		} catch (MessagingException e) {
			return FAILED_SEND;
		} catch (UnsupportedEncodingException e) {
			return FAILED_SEND;
		}

		return SUCCESS;
	}

	public static class MailAutherticator extends Authenticator {
		private String m_username = null;
		private String m_userpass = null;

		public void setUsername(String username) {
			m_username = username;
		}

		public void setUserpass(String userpass) {
			m_userpass = userpass;
		}

		public MailAutherticator(String username, String userpass) {
			super();
			setUsername(username);
			setUserpass(userpass);
		}

		@Override
		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(m_username, m_userpass);
		}
	}
}