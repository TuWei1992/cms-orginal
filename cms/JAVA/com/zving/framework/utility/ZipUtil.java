package com.zving.framework.utility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import com.zving.framework.collection.Mapx;
import com.zving.framework.ui.control.LongTimeTask;
import com.zving.preloader.zip.ZipEntry;
import com.zving.preloader.zip.ZipFile;
import com.zving.preloader.zip.ZipOutputStream;

/**
 * ZIP压缩工具类，也可以解压gzip文件
 * 
 * @Author 王育春
 * @Date 2006-11-29
 * @Mail wyuch@midding.com
 */
public class ZipUtil {

	/**
	 * 以ZIP方式压缩二进制数组
	 */
	public static byte[] zip(byte[] bs) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Deflater def = new Deflater();
		DeflaterOutputStream dos = new DeflaterOutputStream(bos, def);
		try {
			dos.write(bs);
			dos.finish();
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] r = bos.toByteArray();
		return r;
	}

	/**
	 * 将一批文件(文件夹)压缩到一个流里，压缩后的路径以base参数为起点。
	 */
	public static void zipBatch(String base, String[] srcFiles, OutputStream destStream) throws Exception {
		File[] files = new File[srcFiles.length];
		for (int i = 0; i < srcFiles.length; i++) {
			files[i] = new File(srcFiles[i]);
		}
		zipBatch(base, files, destStream);
	}

	/**
	 * 将一批文件(文件夹)压缩到一个ZIP文件里，压缩后的路径以base参数为起点。
	 */
	public static void zipBatch(String base, File[] srcFiles, OutputStream destStream) throws Exception {
		zipBatch(base, srcFiles, destStream, true);
	}

	public static void zipBatch(String base, File[] srcFiles, OutputStream destStream, boolean filterSVN) throws Exception {
		ZipOutputStream zos = new ZipOutputStream(destStream);
		try {
			base = FileUtil.normalizePath(base);
			if (!base.endsWith("/")) {
				base += "/";
			}
			for (int k = 0; k < srcFiles.length; k++) {
				if (!srcFiles[k].exists()) {
					continue;
				}
				List<File> fileList = getSubFiles(srcFiles[k], filterSVN);
				ZipEntry ze = null;
				byte[] buf = new byte[1024];
				int readLen = 0;
				for (int i = 0; i < fileList.size(); i++) {
					File f = fileList.get(i);
					// 创建一个ZipEntry，并设置Name和其它的一些属性
					if (filterSVN) {
						if (f.getName().equals(".svn") || f.getName().equals(".temp")) {
							continue;
						}
					}
					// 排除目录重复新建
					if (f.isDirectory()) {
						continue;
					}
					String name = f.getAbsolutePath();
					name = FileUtil.normalizePath(name);
					if (!name.startsWith(base)) {
						return;
					}
					name = name.substring(base.length());
					ze = new ZipEntry(name);
					ze.setSize(f.length());
					ze.setTime(f.lastModified());
					LogUtil.info("Compressing:" + f.getPath());
					// 将ZipEntry加到zos中，再写入实际的文件内容
					if (f.isFile()) {
						ze.setUnixMode(644);
						zos.putNextEntry(ze);
						InputStream is = new BufferedInputStream(new FileInputStream(f));
						while ((readLen = is.read(buf, 0, 1024)) != -1) {
							zos.write(buf, 0, readLen);
						}
						is.close();
					} else if (f.isDirectory()) {
						ze.setUnixMode(755);
						zos.putNextEntry(ze);
					}
				}
			}
		} finally {
			zos.close();
		}
	}

	/**
	 * 将一批文件压缩到一个流里，压缩后的路径以各文件（文件夹）自身为起点。
	 */
	public static void zipBatch(String[] srcFiles, OutputStream destStream) throws Exception {
		File[] files = new File[srcFiles.length];
		for (int i = 0; i < srcFiles.length; i++) {
			files[i] = new File(srcFiles[i]);
		}
		zipBatch(files, destStream);
	}

	/**
	 * 以ZIP方式压缩文件
	 */
	public static void zip(String srcFile, String destFile, LongTimeTask lt) throws Exception {
		OutputStream os = new FileOutputStream(destFile);
		zip(new File(srcFile), os, lt);
		os.flush();
		os.close();
	}

	/**
	 * 以ZIP方式压缩文件或目录
	 */
	public static void zip(String srcFile, String destFile) throws Exception {
		OutputStream os = new FileOutputStream(destFile);
		zip(new File(srcFile), os);
		os.flush();
		os.close();
	}

	/**
	 * 以ZIP方式压缩整个文件或目录
	 */
	public static void zip(File srcFile, OutputStream destStream) throws Exception {
		zip(srcFile, destStream, null);
	}

	/**
	 * 以ZIP方式压缩文件或目录，并输出到指定流
	 */
	public static void zip(File srcFile, OutputStream destStream, LongTimeTask ltt) throws Exception {
		zipBatch(srcFile.listFiles(), destStream, true);
	}

	/**
	 * 将一批文件（文件夹）压缩到一个ZIP文件里，压缩后的路径以各文件（文件夹）自身为起点。
	 */
	public static void zipBatch(File[] srcFiles, OutputStream destStream) throws Exception {
		zipBatch(srcFiles, destStream, true);
	}

	public static void zipBatch(File[] srcFiles, OutputStream destStream, boolean filterSVN) throws Exception {
		ZipOutputStream zos = new ZipOutputStream(destStream);
		for (int k = 0; k < srcFiles.length; k++) {
			if (!srcFiles[k].exists()) {
				continue;
			}
			List<File> fileList = getSubFiles(srcFiles[k], filterSVN);
			ZipEntry ze = null;
			byte[] buf = new byte[1024];
			int readLen = 0;
			for (int i = 0; i < fileList.size(); i++) {
				File f = fileList.get(i);
				// 创建一个ZipEntry，并设置Name和其它的一些属性
				ze = new ZipEntry(getAbsFileName(srcFiles[k], f));
				ze.setSize(f.length());
				ze.setTime(f.lastModified());
				LogUtil.info("Compressing:" + f.getPath());
				// 将ZipEntry加到zos中，再写入实际的文件内容
				if (f.isFile()) {
					ze.setUnixMode(644);// 解决linux乱码
					zos.putNextEntry(ze);
					InputStream is = new BufferedInputStream(new FileInputStream(f));
					while ((readLen = is.read(buf, 0, 1024)) != -1) {
						zos.write(buf, 0, readLen);
					}
					is.close();
				} else if (f.isDirectory()) {
					ze.setUnixMode(755);// 解决linux乱码
					zos.putNextEntry(ze);
				}
			}
		}
		zos.close();
	}

	/**
	 * 将二进制数组解压缩
	 */
	public static byte[] unzip(byte[] bs) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ByteArrayInputStream bis = new ByteArrayInputStream(bs);
		bos = new ByteArrayOutputStream();
		Inflater inf = new Inflater();
		InflaterInputStream dis = new InflaterInputStream(bis, inf);
		byte[] buf = new byte[1024];
		int c;
		try {
			while ((c = dis.read(buf)) != -1) {
				bos.write(buf, 0, c);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] r = bos.toByteArray();
		return r;
	}

	/**
	 * GZIP解压缩
	 */
	public static byte[] ungzip(byte[] bs) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ByteArrayInputStream bis = new ByteArrayInputStream(bs);
		bos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int c;
		try {
			GZIPInputStream gis = new GZIPInputStream(bis);
			while ((c = gis.read(buf)) != -1) {
				bos.write(buf, 0, c);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] r = bos.toByteArray();
		return r;
	}

	/**
	 * 文件解压缩
	 */
	public static void unzip(String srcFileName, String destPath) throws Exception {
		ZipFile zipFile = new ZipFile(srcFileName);
		Enumeration<?> e = zipFile.getEntries();
		ZipEntry zipEntry = null;
		new File(destPath).mkdirs();
		while (e.hasMoreElements()) {
			zipEntry = (ZipEntry) e.nextElement();
			LogUtil.info("Uncompressing:" + zipEntry.getName());
			if (zipEntry.isDirectory()) {
				new File(destPath + File.separator + zipEntry.getName()).mkdirs();
			} else {
				File f = new File(destPath + File.separator + zipEntry.getName());
				f.getParentFile().mkdirs();
				InputStream in = zipFile.getInputStream(zipEntry);
				OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
				byte[] buf = new byte[1024];
				int c;
				while ((c = in.read(buf)) != -1) {
					out.write(buf, 0, c);
				}
				out.close();
				in.close();
			}
		}
		zipFile.close();
	}

	/**
	 * 获得ZIP文件内的文件清单，键值为文件大小
	 */
	public static Mapx<String, Long> getFileListInZip(String zipFileName) throws Exception {
		ZipFile zipFile = new ZipFile(zipFileName, "GBK");// 解决歌华乱码问题
		Enumeration<?> e = zipFile.getEntries();
		Mapx<String, Long> map = new Mapx<String, Long>();
		while (e.hasMoreElements()) {
			ZipEntry zipEntry = (ZipEntry) e.nextElement();
			if (!zipEntry.isDirectory()) {
				map.put(zipEntry.getName(), zipEntry.getSize());
			}
		}
		zipFile.close();
		return map;
	}

	/**
	 * 从zip文件中读取一个文件
	 * 
	 * @param zipFileName
	 * @param fileName
	 * @return
	 */
	public static byte[] readFileInZip(String zipFileName, String fileName) {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(zipFileName, "GBK");
			Enumeration<?> e = zipFile.getEntries();
			ZipEntry zipEntry = null;
			while (e.hasMoreElements()) {
				zipEntry = (ZipEntry) e.nextElement();
				if (!zipEntry.isDirectory() && zipEntry.getName().equals(fileName)) {
					InputStream in = zipFile.getInputStream(zipEntry);
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					byte[] buf = new byte[1024];
					int c;
					while ((c = in.read(buf)) != -1) {
						out.write(buf, 0, c);
					}
					out.close();
					in.close();
					return out.toByteArray();
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (zipFile != null) {
				try {
					zipFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * 递归获取一个文件夹下的所有文件及子文件
	 */
	static List<File> getSubFiles(File baseDir, boolean filterSVN) {
		List<File> arr = new ArrayList<File>();
		arr.add(baseDir);
		if (baseDir.isDirectory()) {
			File[] tmp = baseDir.listFiles();
			for (File element : tmp) {
				if (element.getName().equals(".svn") || element.getName().equals(".temp")) {
					continue;
				}
				arr.addAll(getSubFiles(element, filterSVN));
			}
		}
		return arr;
	}

	/**
	 * 获取一个文件相对于某个目录的相对路径
	 */
	static String getAbsFileName(File baseDir, File realFileName) {
		File real = realFileName;
		File base = baseDir;
		String ret = real.getName();
		if (real.isDirectory()) {
			ret += "/";
		}
		while (true) {
			if (real == base) {
				break;
			}
			real = real.getParentFile();
			if (real == null) {
				break;
			}
			if (real.equals(base)) {
				ret = real.getName() + "/" + ret;
				break;
			} else {
				ret = real.getName() + "/" + ret;
			}
		}
		return ret;
	}

}
