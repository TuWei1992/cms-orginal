package com.zving.platform.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zving.framework.Config;
import com.zving.framework.Current;
import com.zving.framework.collection.CacheMapx;
import com.zving.framework.core.IURLHandler;
import com.zving.framework.ui.resource.UIResourceFile;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.ServletUtil;
import com.zving.framework.utility.StringUtil;

public class StyleResourceHandler implements IURLHandler {

	public static final String ID = "com.zving.platform.service.StyleResourceHandler";

	private String config; // style config

	private static CacheMapx<String, CachedResource> map = new CacheMapx<String, CachedResource>(2000);// 最多缓存2000个

	public static final String LastModifiedFormat = "EEE, dd MMM yyyy HH:mm:ss";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public boolean match(String url) {
		if (url.startsWith("/icons/")) {
			String ext = ServletUtil.getUrlExtension(url);
			if (ObjectUtil.empty(ext)) {
				return false;
			}
			if (ObjectUtil.in(ext, ".gif", ".jpg", ".png")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getExtendItemName() {
		return "Style Resource URL Processor";
	}

	@Override
	public boolean handle(String url, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (StringUtil.isEmpty(config)) {
			UIResourceFile rf = new UIResourceFile("/style/config.properties");
			config = rf.readText();
		}
		String skin = Current.getCookie("zcms_skin");
		if (StringUtil.isNotEmpty(skin)) {
			String resourceDir = getStyleDirectory(config, skin + ".iconfolder");
			if (StringUtil.isNotEmpty(resourceDir)) {
				url = "/" + resourceDir + "/" + url.substring(url.indexOf("/", 1) + 1);
			}
		}
		String fileName = url;
		if (fileName.indexOf("?") > 0) {
			fileName = fileName.substring(0, fileName.indexOf("?"));
		}
		if (fileName.indexOf("#") > 0) {
			fileName = fileName.substring(0, fileName.indexOf("#"));
		}
		if (fileName.startsWith("/")) {
			fileName = fileName.substring(1);
		}
		fileName = FileUtil.normalizePath(fileName);
		String ext = ServletUtil.getUrlExtension(url);
		response.setHeader("Content-Type", "image/" + ext.substring(1));

		CachedResource r = map.get(fileName);
		if (r != null && System.currentTimeMillis() - r.LastCheck < 3000) {
			String match = request.getHeader("If-None-Match");
			if (r.ETag.equals(match)) {
				response.setStatus(304);
				return true;// 直接从浏览器缓存中获取
			}
			response.getOutputStream().write(r.Data);
			return true;
		}
		String fullFileName = Config.getContextRealPath() + fileName;
		File f = new File(fullFileName);
		String etag;
		if (f.exists() && f.isFile()) {
			etag = getETag(f);
			String match = request.getHeader("If-None-Match");
			if (etag.equals(match)) {
				response.setStatus(304);
				return true;
			}
			response.setHeader("ETag", etag);
			write(response, f);
			return true;
		} else {
			UIResourceFile rf = new UIResourceFile(fileName);
			etag = getETag(rf);
			long lastModified = rf.lastModified();
			if (lastModified != 0) {
				String match = request.getHeader("If-None-Match");
				if (r != null && r.ETag.equals(match)) {
					response.setStatus(304);
					response.getOutputStream().write(0);
					response.flushBuffer();
					if (r != null) {
						r.LastCheck = System.currentTimeMillis();
					}
					return true;// 直接从浏览器缓存中获取
				}
				response.setHeader("ETag", etag);
				write(response, rf);
				response.flushBuffer();
				if (rf.length() <= 512000) {
					r = new CachedResource();
					r.LastCheck = System.currentTimeMillis();
					r.ETag = etag;
					r.LastModified = rf.lastModified();
					r.Data = rf.readByte();
					map.put(fileName, r);
				}
			} else {
				return false;
			}
		}

		return true;
	}

	private static void write(HttpServletResponse response, InputStream is) throws IOException {
		if (is != null) {
			try {
				OutputStream os = response.getOutputStream();
				int len = 0;
				byte[] bs = new byte[1024 * 100];
				while ((len = is.read(bs)) != -1) {
					os.write(bs, 0, len);
					// os.flush();
				}
			} finally {
				is.close();
			}
		}
	}

	private static void write(HttpServletResponse response, File f) throws IOException {
		write(response, new FileInputStream(f));
	}

	private static void write(HttpServletResponse response, UIResourceFile rf) throws IOException {
		write(response, rf.toStream());
	}

	static class CachedResource {
		public String ETag; // 文件的ETag
		public long LastCheck;// 最后检查时间
		public long LastModified;// 资源的最后修改时间
		public byte[] Data;// 如果不是文件，则缓存二进制数据
	}

	private String getStyleDirectory(String config, String id) {
		if (ObjectUtil.empty(config)) {
			return config;
		}
		String str = id + "=";
		if (config.startsWith(str)) {
			int end = config.indexOf("\n");
			if (end < 0) {
				end = config.length();
			}
			return StringUtil.javaEncode(config.substring(config.indexOf("=") + 1, end).trim());
		} else {
			str = "\n" + str;
			int i = config.indexOf(str);
			if (i < 0) {
				return null;
			}
			int end = config.indexOf("\n", i + str.length());
			if (end < 0) {
				end = config.length();
			}
			return StringUtil.javaEncode(config.substring(i + str.length(), end).trim());
		}
	}

	@Override
	public void init() {
	}

	@Override
	public void destroy() {
	}

	@Override
	public int getOrder() {
		return 9998;
	}

	private String getETag(File file) {
		return Integer.toHexString(file.hashCode()) + "-" + Long.toHexString(file.lastModified()) + "-" + Long.toHexString(file.length());
	}

	private String getETag(UIResourceFile file) {
		return Integer.toHexString(file.hashCode()) + "-" + Long.toHexString(file.lastModified()) + "-" + Long.toHexString(file.length());
	}
}
