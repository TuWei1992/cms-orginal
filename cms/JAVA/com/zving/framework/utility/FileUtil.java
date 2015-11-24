package com.zving.framework.utility;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import com.zving.cxdata.config.CheckContentBeforeWriteFlag;
import com.zving.cxdata.config.LicensePath;
import com.zving.framework.Config;
import com.zving.framework.collection.Mapx;
import com.zving.framework.thirdparty.commons.ArrayUtils;

/**
 * 文件操作工具类
 * 
 * @author 王育春<br>
 * @date 2006-7-12<br>
 * @email wangyc@zving.com<br>
 */
public class FileUtil {
	/**
	 * 将文件路径规则化，去掉其中多余的/和\，去掉可能造成文件信息泄漏的../
	 */
	public static String normalizePath(String path) {
		path = path.replace('\\', '/');
		path = StringUtil.replaceEx(path, "../", "/");
		path = StringUtil.replaceEx(path, "./", "/");
		if (path.endsWith("..")) {
			path = path.substring(0, path.length() - 2);
		}
		path = path.replaceAll("/+", "/");
		return path;
	}

	public static File normalizeFile(File f) {
		String path = f.getAbsolutePath();
		path = normalizePath(path);
		return new File(path);
	}

	/**
	 * 得到文件名中的扩展名，不带圆点。
	 */
	public static String getExtension(String fileName) {
		int index = fileName.lastIndexOf(".");
		if (index < 0) {
			return null;
		}
		String ext = fileName.substring(index + 1);
		return ext.toLowerCase();
	}

	/**
	 * 以全局编码将指定内容写入指定文件
	 */
	public static boolean writeText(String fileName, String content) {
		if (CheckContentBeforeWriteFlag.checkFlag()) {
			File file = new File(fileName);
			if (file.exists()) {
				String txt = readText(file);
				if (txt != null && txt.equals(content)) {
					LogUtil.info("=======内容相同,不更新文件：" + fileName);
					return false;
				}
			}
		}
		return writeText(fileName, content, Config.getGlobalCharset());
	}
	
	/**
	 * 以全局编码将指定内容写入指定文件
	 */
	public static boolean writeText(String fileName, String content, boolean checkContent) {
		if (checkContent) {
			File file = new File(fileName);
			if (file.exists()) {
				String txt = readText(file);
				if (txt != null && txt.equals(content)) {
					LogUtil.info("内容相同,不更新文件：" + fileName);
					return false;
				}
			}
		}
		return writeText(fileName, content, Config.getGlobalCharset());
	}

	/**
	 * 以指定编码将指定内容写入指定文件
	 */
	public static boolean writeText(String fileName, String content, String encoding) {
		return writeText(fileName, content, encoding, false);
	}

	/**
	 * 以指定编码将指定内容写入指定文件，如果编码为UTF-8且bomFlag为true,则在文件头部加入3字节的BOM
	 */
	public static boolean writeText(String fileName, String content, String encoding, boolean bomFlag) {
		fileName = normalizePath(fileName);
		try {
			byte[] bs = content.getBytes(encoding);
			if (encoding.equalsIgnoreCase("UTF-8") && bomFlag) {
				bs = ArrayUtils.addAll(StringUtil.BOM, bs);
			}
			writeByte(fileName, bs);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 以二进制方式读取文件
	 */
	public static byte[] readByte(String fileName) {
		fileName = normalizePath(fileName);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(fileName);
			byte[] r = new byte[fis.available()];
			fis.read(r);
			return r;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * 以二进制方式读取文件
	 */
	public static byte[] readByte(File f) {
		f = normalizeFile(f);
		FileInputStream fis = null;
		try {

			fis = new FileInputStream(f);
			byte[] r = readByte(fis);
			return r;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * 读取指定流，并转换为二进制数组
	 */
	public static byte[] readByte(InputStream is) {
		byte[] buffer = new byte[8192];
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		while (true) {
			int bytesRead = -1;
			try {
				bytesRead = is.read(buffer);
			} catch (IOException e) {
				throw new RuntimeException("File.readByte() failed");
			}
			if (bytesRead == -1) {
				break;
			}
			try {
				os.write(buffer, 0, bytesRead);
			} catch (Exception e) {
				throw new RuntimeException("File.readByte() failed");
			}
		}
		return os.toByteArray();
	}

	/**
	 * 将二进制数组写入指定文件
	 */
	public static boolean writeByte(String fileName, byte[] b) {
		fileName = normalizePath(fileName);
		BufferedOutputStream os = null;
		try {
			os = new BufferedOutputStream(new FileOutputStream(fileName));
			os.write(b);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 将二进制数组写入指定文件
	 */
	public static boolean writeByte(File f, byte[] b) {
		f = normalizeFile(f);
		BufferedOutputStream os = null;
		try {
			os = new BufferedOutputStream(new FileOutputStream(f));
			os.write(b);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 以全局编码读取指定文件中的文本
	 */
	public static String readText(File f) {
		return readText(f, Config.getGlobalCharset());
	}

	/**
	 * 以指定编码读取指定文件中的文本
	 */
	public static String readText(File f, String encoding) {
		f = normalizeFile(f);
		InputStream is = null;
		try {
			is = new FileInputStream(f);
			String str = readText(is, encoding);
			return str;
		} catch (Exception e) {
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

	/**
	 * 以指定编码读取流中的文本
	 */
	public static String readText(InputStream is, String encoding) {
		try {
			byte[] bs = readByte(is);
			if (encoding.equalsIgnoreCase("utf-8")) {// 如果是UTF8则要判断有没有BOM
				if (StringUtil.hexEncode(ArrayUtils.subarray(bs, 0, 3)).equals("efbbbf")) {// BOM标志
					bs = ArrayUtils.subarray(bs, 3, bs.length);
				}
			}
			return new String(bs, encoding);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 以全局编码读取指定文件中的文本
	 */
	public static String readText(String fileName) {
		//CX控制license文件的读取位置
		if (fileName.endsWith("classes/license.dat")) {
			String licensePath = LicensePath.getReadValue();
			if (licensePath != null) {
				fileName = licensePath;
			}
		}
		return readText(fileName, Config.getGlobalCharset());
	}

	/**
	 * 以指定编码读取指定文件中的文本
	 */
	public static String readText(String fileName, String encoding) {
		fileName = normalizePath(fileName);
		InputStream is = null;
		try {
			is = new FileInputStream(fileName);
			String str = readText(is, encoding);
			return str;
		} catch (Exception e) {
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

	/**
	 * 以全局编码读取指定URL中的文本
	 */
	public static String readURLText(String urlPath) {
		return readURLText(urlPath, Config.getGlobalCharset());
	}

	/**
	 * 以指定编码读取指定URL中的文本
	 */
	public static String readURLText(String urlPath, String encoding) {
		BufferedReader in = null;
		try {
			URL url = new URL(urlPath);
			in = new BufferedReader(new InputStreamReader(url.openStream(), encoding));
			String line;
			StringBuilder sb = new StringBuilder();
			while ((line = in.readLine()) != null) {
				sb.append(line + "\n");
			}

			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * 删除文件，不管路径是文件还是文件夹，都删掉。<br>
	 * 删除文件夹时会自动删除子文件夹。
	 */
	public static boolean delete(String path) {
		File file = new File(path);
		return delete(file);
	}

	/**
	 * 删除文件，不管路径是文件还是文件夹，都删掉。<br>
	 * 删除文件夹时会自动删除子文件夹。
	 */
	public static boolean delete(File f) {
		f = normalizeFile(f);
		if (!f.exists()) {
			LogUtil.warn("File or directory not found" + f);
			return false;
		}
		if (f.isFile()) {
			return f.delete();
		} else {
			return FileUtil.deleteDir(f);
		}
	}

	/**
	 * 删除文件夹及其子文件夹
	 */
	private static boolean deleteDir(File dir) {
		dir = normalizeFile(dir);
		try {
			return deleteFromDir(dir) && dir.delete(); // 先删除完里面所有内容再删除空文件夹
		} catch (Exception e) {
			LogUtil.warn("Delete directory failed");
			// e.printStackTrace();
			return false;
		}
	}

	/**
	 * 创建文件夹
	 */
	public static boolean mkdir(String path) {
		path = normalizePath(path);
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return true;
	}

	/**
	 * 判断文件或文件夹是否存在
	 */
	public static boolean exists(String path) {
		path = normalizePath(path);
		File dir = new File(path);
		return dir.exists();
	}

	/**
	 * 删除文件夹里面的所有文件,但不删除自己本身
	 */
	public static boolean deleteFromDir(String dirPath) {
		dirPath = normalizePath(dirPath);
		File file = new File(dirPath);
		return deleteFromDir(file);
	}

	/**
	 * 删除文件夹里面的所有文件和子文件夹,但不删除自己本身
	 * 
	 * @param file
	 * @return
	 */
	public static boolean deleteFromDir(File dir) {
		dir = normalizeFile(dir);
		if (!dir.exists()) {
			LogUtil.warn("Directory not found：" + dir);
			return false;
		}
		if (!dir.isDirectory()) {
			LogUtil.warn(dir + " is not directory");
			return false;
		}
		File[] tempList = dir.listFiles();
		for (int i = 0; i < tempList.length; i++) {
			if (!delete(tempList[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 从指定位置复制文件到另一个文件夹，复制时不符合filter条件的不复制
	 */
	public static boolean copy(String oldPath, String newPath, FileFilter filter) {
		oldPath = normalizePath(oldPath);
		newPath = normalizePath(newPath);
		File oldFile = new File(oldPath);
		File[] oldFiles = oldFile.listFiles(filter);
		boolean flag = true;
		if (oldFiles != null) {
			for (int i = 0; i < oldFiles.length; i++) {
				if (!copy(oldFiles[i], newPath + "/" + oldFiles[i].getName())) {
					flag = false;
				}
			}
		}
		return flag;
	}

	/**
	 * 从指定位置复制文件到另一个文件夹
	 */
	public static boolean copy(String oldPath, String newPath) {
		File oldFile = new File(oldPath);
		return copy(oldFile, newPath);
	}

	public static boolean copy(File oldFile, String newPath) {
		oldFile = normalizeFile(oldFile);
		if (!oldFile.exists()) {
			LogUtil.warn("File not found:" + oldFile);
			return false;
		}
		if (StringUtil.isEmpty(newPath)) {
			LogUtil.info("Destintion path:" + newPath + " cannot be empty!");
			return false;
		}
		if (oldFile.isFile()) {
			return copyFile(oldFile, newPath);
		} else {
			// 判断newPath是否为oldFile的子路径，进行特殊处理
			if (normalizePath(newPath).startsWith(normalizePath(oldFile.getAbsolutePath()))) {
				return renameToSubDir(normalizePath(oldFile.getAbsolutePath()), newPath, false);
			} else {
				return copyDir(oldFile, newPath);
			}
		}
	}

	/**
	 * 将指定路径的文件复制到输出流中
	 * 
	 * @param filePath 要复制的文件路径
	 * @param outputStream 输出流
	 * @param close 是否关闭输出流
	 * @param buffer 缓冲区
	 * @return 复制字节长度
	 * @throws IOException
	 */
	public static long copy(String filePath, OutputStream outputStream, boolean close, byte[] buffer) throws IOException {
		FileInputStream inputStream = new FileInputStream(filePath);
		return copy(inputStream, outputStream, close, buffer);
	}

	/**
	 * 将指定的文件复制到输出流中
	 * 
	 * @param file 要复制的文件
	 * @param outputStream 输出流
	 * @param close 是否关闭输出流
	 * @param buffer 缓冲区
	 * @return 复制的字节长度
	 * @throws IOException
	 */
	public static long copy(File file, OutputStream outputStream, boolean close, byte[] buffer) throws IOException {
		FileInputStream inputStream = new FileInputStream(file);
		return copy(inputStream, outputStream, close, buffer);
	}

	/**
	 * 从输入流将内容写入输出流
	 * 
	 * @param inputStream 输入流
	 * @param outputStream 输出流
	 * @param close 写入完毕是否关闭输出流
	 * @param buffer 缓存字节数组
	 * @return 复制流的字节长度
	 * @throws IOException
	 */
	public static long copy(InputStream inputStream, OutputStream outputStream, boolean close, byte[] buffer) throws IOException {
		OutputStream out = outputStream;
		InputStream in = inputStream;
		try {
			long total = 0;
			for (;;) {
				int res = in.read(buffer);
				if (res == -1) {
					break;
				}
				if (res > 0) {
					total += res;
					if (out != null) {
						out.write(buffer, 0, res);
					}
				}
			}
			if (out != null) {
				if (close) {
					out.close();
				} else {
					out.flush();
				}
				out = null;
			}
			in.close();
			in = null;
			return total;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Throwable t) {
					/* Ignore me */
				}
			}
			if (close && out != null) {
				try {
					out.close();
				} catch (Throwable t) {
					/* Ignore me */
				}
			}
		}
	}

	/**
	 * 复制单个文件
	 */
	private static boolean copyFile(File oldFile, String newPath) {
		oldFile = normalizeFile(oldFile);
		newPath = normalizePath(newPath);

		if (!oldFile.exists()) { // 文件存在时
			LogUtil.warn("File not found:" + oldFile);
			return false;
		}
		if (!oldFile.isFile()) { // 文件存在时
			LogUtil.warn(oldFile + " is not file");
			return false;
		}
		if (oldFile.getName().equalsIgnoreCase("Thumbs.db")) {
			LogUtil.warn(oldFile + " is ignored");
			return true;
		}
		FileUtil.mkdir(newPath.substring(0, newPath.lastIndexOf("/")));
		File newFile = new File(newPath);
		// 如果新文件是一个目录，则创建新的File对象
		if (newFile.isDirectory()) {
			newFile = new File(newPath, oldFile.getName());
		}
		if (newFile.getAbsoluteFile().equals(oldFile.getAbsoluteFile())) {
			return true;
		}
		try {
			InputStream inStream = new FileInputStream(oldFile); // 读入原文件
			FileOutputStream fs = new FileOutputStream(newFile);
			byte[] buffer = new byte[1024];
			copy(inStream, fs, true, buffer);
		} catch (Exception e) {
			LogUtil.warn("Copy file " + oldFile.getPath() + " failed,cause:" + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 复制整个文件夹内容
	 */
	private static boolean copyDir(File oldDir, String newPath) {
		oldDir = normalizeFile(oldDir);
		newPath = normalizePath(newPath);
		if (!oldDir.exists()) { // 文件存在时
			LogUtil.info("File not found:" + oldDir);
			return false;
		}
		if (!oldDir.isDirectory()) { // 文件存在时
			LogUtil.info(oldDir + " is not directory");
			return false;
		}
		try {
			new File(newPath).mkdirs(); // 如果文件夹不存在 则建立新文件夹
			File[] files = oldDir.listFiles();
			File temp = null;
			for (File file : files) {
				temp = file;
				if (temp.isFile()) {
					if (!FileUtil.copyFile(temp, newPath + "/" + temp.getName())) {
						return false;
					}
				} else if (temp.isDirectory()) {// 如果是子文件夹
					if (!FileUtil.copyDir(temp, newPath + "/" + temp.getName())) {
						return false;
					}
				}
			}
			return true;
		} catch (Exception e) {
			LogUtil.info("Copy directory failed,cause:" + e.getMessage());
			// e.printStackTrace();
			return false;
		}
	}

	/**
	 * 移动文件到指定目录
	 */
	public static boolean move(String oldPath, String newPath) {
		if (StringUtil.isEmpty(newPath)) {
			LogUtil.info("Destintion path:" + newPath + " cannot be empty!");
			return false;
		}
		if (StringUtil.isEmpty(oldPath)) {
			LogUtil.info("Source path:" + oldPath + " cannot be empty!");
			return false;
		}
		oldPath = normalizePath(oldPath);
		newPath = normalizePath(newPath);

		// 判断newPath是否为oldFile的子路径，进行特殊处理
		if (newPath.startsWith(oldPath)) {
			return renameToSubDir(oldPath, newPath, true);
		} else {
			return copy(oldPath, newPath) && delete(oldPath);
		}
	}

	/**
	 * 该方法用于处理move和copy时目标路径为源路径的子路径问题
	 * 
	 * @param oldPath
	 * @param newPath
	 * @param deleteOldDir 如果是true则删除源文件夹，否则不删除
	 * @return
	 */
	private static boolean renameToSubDir(String oldPath, String newPath, boolean deleteOldDir) {
		File oldFile = new File(oldPath);
		File oldParent = oldFile.getParentFile();
		// 如果oldFile的父目录为分区根目录，如D:\的时候，不做处理，因为有些系统的文件或路径会出错
		if (oldParent == null) {
			String action = deleteOldDir ? "move" : "copy";
			LogUtil.info("Cannot " + action + " directory:" + oldPath + " to a subdirectory of itself " + newPath);
			return false;
		}
		// 生成临时文件夹
		String oldParentPath = normalizePath(oldParent.getAbsolutePath()) + "/";
		String tempPath = oldParentPath + System.currentTimeMillis();

		File tempFile = new File(tempPath);
		File newFile = new File(newPath);
		newFile.mkdirs();
		// 将目标路径重命名为临时文件夹的名字
		newFile.renameTo(tempFile);
		if (!copy(oldPath, tempPath)) {
			return false;
		}
		// 删除源文件
		if (deleteOldDir && !delete(oldPath)) {
			return false;
		}
		newFile.getParentFile().mkdirs();
		// 如果重命名失败可能由于磁盘文件系统不一样，这时需要复制
		if (!tempFile.renameTo(newFile)) {
			// 需要重新创建文件夹
			oldFile.mkdirs();
			return copy(tempPath, oldPath) && delete(tempPath);
		}
		return true;
	}

	/**
	 * 将可序列化对象序列化并写入指定文件
	 */
	public static void serialize(Serializable obj, String fileName) {// NO_UCD
		fileName = normalizePath(fileName);
		ObjectOutputStream s = null;
		try {
			FileOutputStream f = new FileOutputStream(fileName);
			s = new ObjectOutputStream(f);
			s.writeObject(obj);
			s.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (s != null) {
				try {
					s.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 将可序列化对象序列化并返回二进制数组
	 */
	public static byte[] serialize(Serializable obj) {
		ObjectOutputStream s = null;
		try {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			s = new ObjectOutputStream(b);
			s.writeObject(obj);
			s.flush();
			return b.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (s != null) {
				try {
					s.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 从指定文件中反序列化对象
	 */
	public static Object unserialize(String fileName) {// NO_UCD
		fileName = normalizePath(fileName);
		ObjectInputStream s = null;
		try {
			FileInputStream in = new FileInputStream(fileName);
			s = new ObjectInputStream(in);
			Object o = s.readObject();
			return o;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (s != null) {
				try {
					s.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 从二进制数组中反序列化对象
	 */
	public static Object unserialize(byte[] bs) {
		ObjectInputStream s = null;
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(bs);
			s = new ObjectInputStream(in);
			Object o = s.readObject();
			return o;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (s != null) {
				try {
					s.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 将一个Map高性能序列化,键值只能为字符串<br>
	 */
	public static byte[] mapToBytes(Map<?, ?> map) {
		if (map == null) {
			return null;
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			for (Entry<?, ?> entry : map.entrySet()) {
				Object k = entry.getKey();
				Object v = entry.getValue();
				if (k == null) {
					continue;
				}
				if (v == null) {
					bos.write(new byte[] { 0 });
				} else if (v instanceof String) {
					bos.write(new byte[] { 1 });
				} else if (v instanceof Long) {
					bos.write(new byte[] { 2 });
				} else if (v instanceof Integer) {
					bos.write(new byte[] { 3 });
				} else if (v instanceof Boolean) {
					bos.write(new byte[] { 4 });
				} else if (v instanceof Date) {
					bos.write(new byte[] { 5 });
				} else if (v instanceof Mapx) {
					bos.write(new byte[] { 6 });
				} else if (v instanceof Serializable) {
					bos.write(new byte[] { 7 });
				} else {
					throw new RuntimeException("Unknown datatype:" + v.getClass().getName());
				}
				byte[] bs = k.toString().getBytes();
				bos.write(NumberUtil.toBytes(bs.length));
				bos.write(bs);
				if (v == null) {
					continue;
				} else if (v instanceof String) {
					bs = v.toString().getBytes();
					bos.write(NumberUtil.toBytes(bs.length));
					bos.write(bs);
				} else if (v instanceof Long) {
					bos.write(NumberUtil.toBytes(((Long) v).longValue()));
				} else if (v instanceof Integer) {
					bos.write(NumberUtil.toBytes(((Integer) v).intValue()));
				} else if (v instanceof Boolean) {
					bos.write(((Boolean) v).booleanValue() ? 1 : 0);
				} else if (v instanceof Date) {
					bos.write(NumberUtil.toBytes(((Date) v).getTime()));
				} else if (v instanceof Mapx) {
					byte[] arr = mapToBytes((Mapx<?, ?>) v);
					bos.write(NumberUtil.toBytes(arr.length));
					bos.write(arr);
				} else if (v instanceof Serializable) {
					byte[] arr = serialize((Serializable) v);
					bos.write(NumberUtil.toBytes(arr.length));
					bos.write(arr);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bos.toByteArray();
	}

	/**
	 * 将一个二进制数组反序列化为Mapx
	 */
	public static Mapx<String, Object> bytesToMap(byte[] arr) {
		if (arr == null) {
			return null;
		}
		ByteArrayInputStream bis = new ByteArrayInputStream(arr);
		int b = -1;
		Mapx<String, Object> map = new Mapx<String, Object>();
		byte[] kbs = new byte[4];
		byte[] vbs = null;
		try {
			while ((b = bis.read()) != -1) {
				bis.read(kbs);
				int len = NumberUtil.toInt(kbs);
				vbs = new byte[len];
				bis.read(vbs);
				String k = new String(vbs);
				Object v = null;
				if (b == 1) {
					bis.read(kbs);
					len = NumberUtil.toInt(kbs);
					vbs = new byte[len];
					bis.read(vbs);
					v = new String(vbs);
				} else if (b == 2) {
					vbs = new byte[8];
					bis.read(vbs);
					v = new Long(NumberUtil.toLong(vbs));
				} else if (b == 3) {
					vbs = new byte[4];
					bis.read(vbs);
					v = new Integer(NumberUtil.toInt(vbs));
				} else if (b == 4) {
					int i = bis.read();
					v = new Boolean(i == 1 ? true : false);
				} else if (b == 5) {
					vbs = new byte[8];
					bis.read(vbs);
					v = new Date(NumberUtil.toLong(vbs));
				} else if (b == 6) {
					bis.read(kbs);
					len = NumberUtil.toInt(kbs);
					vbs = new byte[len];
					bis.read(vbs);
					v = bytesToMap(vbs);
				} else if (b == 7) {
					bis.read(kbs);
					len = NumberUtil.toInt(kbs);
					vbs = new byte[len];
					bis.read(vbs);
					v = unserialize(vbs);
				}
				map.put(k, v);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}
}
