package com.zving.platform.handler;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.captcha.Captcha;
import nl.captcha.noise.CurvedLineNoiseProducer;
import nl.captcha.servlet.CaptchaServletUtil;
import nl.captcha.text.renderer.DefaultWordRenderer;
import nl.captcha.text.renderer.WordRenderer;

import com.zving.framework.Current;
import com.zving.framework.core.IURLHandler;
import com.zving.framework.utility.NumberUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 显示验证码图片的URL
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-15
 */
public class AuthCodeURLHandler implements IURLHandler {
	public static final String ID = "com.zving.platform.pub.AuthCodeURLProcessor";
	public static final String DefaultAuthKey = "_ZVING_AUTHCODE";
	public static final String WidthKey = "width";
	public static final String HeightKey = "height";
	private static final int DefaultWidth = 80;
	private static final int DefaultHeight = 28;
	private static final List<Color> Colors = new ArrayList<Color>();

	static {
		Colors.add(new Color(0x28, 0x4a, 0x53));
		Colors.add(new Color(0x5f, 0x81, 0x34));
	}

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "AuthCode Generator";
	}

	@Override
	public boolean match(String url) {
		int i = url.indexOf("?");
		if (i > 0) {
			url = url.substring(0, i);
		}
		return url.equals("/authCode.zhtml");
	}

	@Override
	public boolean handle(String url, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		int w = DefaultWidth;
		int h = DefaultHeight;
		if (NumberUtil.isInt(request.getParameter(WidthKey))) {
			w = Integer.valueOf(request.getParameter(WidthKey));
		}
		if (NumberUtil.isInt(request.getParameter(HeightKey))) {
			h = Integer.valueOf(request.getParameter(HeightKey));
		}
		List<Font> fonts = new ArrayList<Font>();
		fonts.add(new Font("Serif", 2, h - 3));
		// fonts.add(new Font("Geneva", 2, h - 3));
		// fonts.add(new Font("Courier", 2, h - 3));
		// fonts.add(new Font("Arial", 2, h - 3));

		Color c = Colors.get(NumberUtil.getRandomInt(Colors.size()));
		WordRenderer wordRenderer = new DefaultWordRenderer(c, fonts);
		Captcha captcha = new Captcha.Builder(w + 10, h + 10).addText(wordRenderer).addNoise(new CurvedLineNoiseProducer(c, 1)).build();

		long time = System.currentTimeMillis();
		String key = StringUtil.md5Hex(time + captcha.getAnswer()) + "_" + time;
		response.addCookie(new Cookie(DefaultAuthKey, key));

		// 以下是因为未查明的原因导致captcha生成的图片位置不正确而作出的位置修正
		BufferedImage image = new BufferedImage(w, h, captcha.getImage().getType());
		image.getGraphics().drawImage(captcha.getImage(), 0, 0, w, h, 23, 15, w + 10, h + 10, null);
		CaptchaServletUtil.writeImage(response, image);
		return true;
	}

	/**
	 * 校验验证码。注意：会从Current.getRequest()中读取Cookie信息
	 */
	public static boolean verify(String authCode) {
		String key = Current.getCookie(DefaultAuthKey);
		if (ObjectUtil.empty(authCode) || ObjectUtil.empty(key) || key.indexOf('_') <= 0) {
			return false;
		}
		int i = key.indexOf('_');
		String time = key.substring(i + 1);
		long c = Long.parseLong(time);
		if (System.currentTimeMillis() - c > 15 * 60 * 1000) {// 15分钟有效
			return false;
		}
		key = key.substring(0, i);
		authCode = StringUtil.md5Hex(time + authCode);
		return authCode.equals(key);
	}

	@Override
	public void init() {
	}

	@Override
	public int getOrder() {
		return 9997;
	}

	@Override
	public void destroy() {
	}

}
