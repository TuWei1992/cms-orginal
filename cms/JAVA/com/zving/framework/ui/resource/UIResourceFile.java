package com.zving.framework.ui.resource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.zving.framework.Config;
import com.zving.framework.utility.FileUtil;

/**
 * 表示一个UI资源，可能处于.ui.jar中，也可能直接在应用目录下
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-10-15
 */
public class UIResourceFile {
	private File file = null;
	private JarResourceEntry re = null;
	private String path;

	public UIResourceFile(String path) {
		path = FileUtil.normalizePath(path);
		if (path.startsWith(Config.getContextRealPath())) {
			path = path.substring(Config.getContextRealPath().length());
		}
		this.path = path;
		File f = new File(Config.getContextRealPath() + path);
		if (f.exists()) {
			file = f;
			return;
		} else {
			re = JarResourceRoot.getFile(path);
		}
	}

	public boolean isFile() {
		return !isDirectory();
	}

	public boolean isDirectory() {
		if (file != null && file.isDirectory()) {
			return true;
		}
		if (re != null && re.isDirectory()) {
			return true;
		}
		return false;
	}

	public boolean isFromJar() {
		return re != null;
	}

	public String getFullPath() {
		return path;
	}

	public String getName() {
		if (path.indexOf("/") >= 0) {
			return path.substring(path.lastIndexOf("/") + 1);
		}
		return path;
	}

	public List<UIResourceFile> listFiles() {
		if (isFile()) {
			return null;
		}
		List<UIResourceFile> list = new ArrayList<UIResourceFile>();
		if (re != null) {
			for (JarResourceEntry re2 : re.listFiles()) {
				list.add(new UIResourceFile(re2.getFullPath()));
			}
		}
		if (file != null) {// 目录下的文件优先使用
			for (File f : file.listFiles()) {
				list.add(new UIResourceFile(f.getAbsolutePath()));
			}
		}
		return list;
	}

	public List<UIResourceFile> listSubFiles() {
		if (isFile()) {
			return null;
		}
		List<UIResourceFile> list = new ArrayList<UIResourceFile>();
		if (re != null) {
			for (JarResourceEntry re2 : re.listFiles()) {
				UIResourceFile file = new UIResourceFile(re2.getFullPath());
				if (file.isDirectory()) {
					list.addAll(file.listSubFiles());
				} else {
					list.add(file);
				}
			}
		}
		if (file != null) {// 目录下的文件优先使用
			for (File f : file.listFiles()) {
				list.add(new UIResourceFile(f.getAbsolutePath()));
			}
		}
		return list;
	}

	public boolean exists() {
		return file != null || re != null;
	}

	public long lastModified() {
		if (file != null) {
			return file.lastModified();
		}
		if (re != null) {
			return re.getLastModified();
		}
		return 0;
	}

	public InputStream toStream() throws IOException {
		if (file != null) {
			return new FileInputStream(file);
		} else if (re != null) {
			return re.toStream();
		} else {
			return new ByteArrayInputStream(new byte[0]);
		}
	}

	public byte[] readByte() {
		InputStream is = null;
		try {
			is = toStream();
			return FileUtil.readByte(is);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public long length() {
		if (file != null) {
			return file.length();
		}
		if (re != null) {
			return re.length();
		}
		return 0;
	}

	public String readText() {
		return readText(Config.getGlobalCharset());
	}

	public String readText(String encoding) {
		try {
			return new String(readByte(), encoding);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
}
