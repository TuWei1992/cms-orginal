package com.zving.framework.utility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zving.framework.Config;

/**
 * IO工具类
 * 
 * @date 2010-5-25 <br>
 * @author 王育春 <br>
 * @email wangyc@zving.com <br>
 */
public class IOUtil {

	public static byte[] getBytesFromStream(InputStream is) throws IOException {
		return getBytesFromStream(is, Integer.MAX_VALUE);
	}

	public static byte[] getBytesFromStream(InputStream is, int max) throws IOException {
		byte[] buffer = new byte[1024];
		int read = -1;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] data = null;
		try {
			while ((read = is.read(buffer)) != -1) {
				if (bos.size() > max) {
					throw new IOException("InputStream length is out of range,max=" + max);
				}
				if (read > 0) {
					byte[] chunk = null;
					if (read == 1024) {
						chunk = buffer;
					} else {
						chunk = new byte[read];
						System.arraycopy(buffer, 0, chunk, 0, read);
					}
					bos.write(chunk);
				}
			}
			data = bos.toByteArray();
		} finally {
			if (bos != null) {
				bos.close();
				bos = null;
			}
		}
		return data;
	}

	public static void download(HttpServletRequest request, HttpServletResponse response, String fileName, InputStream is) {
		try {
			setDownloadFileName(request, response, fileName);
			if (is == null) {
				return;
			}
			OutputStream os = response.getOutputStream();
			byte[] buffer = new byte[1024];
			int read = -1;
			try {
				while ((read = is.read(buffer)) != -1) {
					if (read > 0) {
						byte[] chunk = null;
						if (read == 1024) {
							chunk = buffer;
						} else {
							chunk = new byte[read];
							System.arraycopy(buffer, 0, chunk, 0, read);
						}
						os.write(chunk);
						os.flush();
					}
				}
			} finally {
				is.close();
			}
			os.flush();
			os.close();
		} catch (IOException e) {
			LogUtil.warn("IOUtil.download:IO ends by user!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 设置下载文件名,通过设置http-header命名得文件下载时的文件名按照fileName参数呈现。
	 * 
	 * @param request
	 * @param response
	 * @param fileName
	 */
	public static void setDownloadFileName(HttpServletRequest request, HttpServletResponse response, String fileName) {
		try {
			response.reset();
			response.setContentType("application/octet-stream");
			String userAgent = request.getHeader("User-Agent");
			if (StringUtil.isNotEmpty(userAgent)
					&& (userAgent.toLowerCase().indexOf("msie") >= 0 || userAgent.toLowerCase().indexOf("trident") >= 0)) {
				fileName = new String(fileName.getBytes("GBK"), "ISO-8859-1");
			} else {
				fileName = new String(fileName.getBytes(Config.getGlobalCharset()), "ISO-8859-1");
			}
			response.addHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static final long ONE_KB = 1024;
	public static final long ONE_MB = ONE_KB * ONE_KB;
	public static final long ONE_GB = ONE_KB * ONE_MB;

	/**
	 * 字节数可读性转换
	 */
	public static String byteCountToDisplaySize(long size) {
		String displaySize;
		if (size / ONE_GB > 0) {
			displaySize = String.valueOf(size / ONE_GB) + " GB";
		} else if (size / ONE_MB > 0) {
			displaySize = String.valueOf(size / ONE_MB) + " MB";
		} else if (size / ONE_KB > 0) {
			displaySize = String.valueOf(size / ONE_KB) + " KB";
		} else {
			displaySize = String.valueOf(size);
		}
		return displaySize;
	}
}
