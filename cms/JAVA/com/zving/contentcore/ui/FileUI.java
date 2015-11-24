package com.zving.contentcore.ui;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.zving.adapter.YuiCompressUtil;
import com.zving.contentcore.bl.SiteBL;
import com.zving.contentcore.config.AllowEditFileType;
import com.zving.contentcore.config.AllowedUploadFileType;
import com.zving.contentcore.service.PublishPlatformService;
import com.zving.contentcore.template.TemplateService;
import com.zving.contentcore.util.PublishPlatformUtil;
import com.zving.contentcore.util.SiteUtil;
import com.zving.framework.Config;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.annotation.Verify;
import com.zving.framework.core.handler.ZAction;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.i18n.Lang;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.security.PrivCheck;
import com.zving.framework.thirdparty.commons.fileupload.FileItem;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.ui.control.TreeAction;
import com.zving.framework.ui.control.UploadAction;
import com.zving.framework.ui.control.grid.GridSort;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.IOUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.RarUtil;
import com.zving.framework.utility.ServletUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.framework.utility.ZipUtil;
import com.zving.platform.code.YesOrNo;
import com.zving.platform.util.PlatformUtil;
import com.zving.schema.ZCSite;
import com.zving.schema.ZCTemplate;

@Alias("Core.File")
public class FileUI extends UIFacade {
	@Priv("ContentCore.FileMenu")
	public void init() {
		long siteID = SiteBL.getCurrentSite();
		DataTable dt = PublishPlatformService.getUsedPublishPlatformDataTable(siteID);
		if ((dt != null) && (dt.getRowCount() > 0)) {
			$S("defaultPlatform", dt.get(0, "ID"));
		} else {
			$S("defaultPlatform", "pc");
		}
		if (ObjectUtil.notEmpty($V("SiteID"))) {
			siteID = Long.parseLong($V("SiteID"));
		}
		$S("SiteID", Long.valueOf(siteID));
	}

	@Priv("ContentCore.FileMenu")
	public void initList() {
		long siteID = SiteBL.getCurrentSite();
		if (ObjectUtil.notEmpty($V("SiteID"))) {
			siteID = $L("SiteID");
		}
		$S("SitePriv", Boolean.valueOf(PrivCheck.check("com.zving.cms.Site.File." + siteID)));
	}

	@Priv("ContentCore.FileMenu")
	public void bindTree(TreeAction ta) {
		long siteID = SiteBL.getCurrentSite();
		if (ObjectUtil.notEmpty($V("SiteID"))) {
			siteID = $L("SiteID");
		}
		ta.setRootText(SiteUtil.getName(siteID));
		if (!PrivCheck.check("com.zving.cms.Site.File." + siteID)) {
			ta.bindData(new DataTable());
			return;
		}
		String platform = $V("PlatformID");
		String siteRoot = PublishPlatformUtil.getPublishPlatformRoot(siteID, platform);
		if (StringUtil.isNull(siteRoot)) {
			return;
		}
		String path = siteRoot;
		String parentID = ServletUtil.getChineseParameter("ParentID");
		if (ObjectUtil.notEmpty(parentID)) {
			path = path + parentID;
		}
		path = FileUtil.normalizePath(path);
		DataTable dt = new DataTable();
		dt.insertColumn("ID");
		dt.insertColumn("ParentID");
		dt.insertColumn("Name");
		File dir = new File(path);
		if (!dir.exists()) {
			return;
		}
		addDir(siteRoot, dt, dir, 1);
		ta.bindData(dt);
	}

	@Priv("ContentCore.FileMenu.Add")
	private void addDir(String prefix, DataTable dt, File dir, int level) {
		if (dir.isFile()) {
			return;
		}
		String parentID = FileUtil.normalizePath(dir.getAbsolutePath());
		if (!parentID.endsWith("/")) {
			parentID = parentID + "/";
		}
		parentID = parentID.substring(prefix.length());
		File[] fs = dir.listFiles(new FileFilter() {
			public boolean accept(File f) {
				if ((f.isFile()) || (f.isHidden()) || (f.getName().equals(".svn")) || (f.getName().equals(".temp")) || (f.getName().equals(".zdeploy"))) {
					return false;
				}
				return true;
			}
		});
		fs = (File[]) ObjectUtil.sort(fs, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return f1.getName().compareTo(f2.getName());
			}
		});
		for (File f : fs) {
			String id = parentID + f.getName().trim() + "/";
			dt.insertRow(new Object[] { id, parentID, f.getName().trim() });
			if (level == 2) {
				break;
			}
			addDir(prefix, dt, f, level + 1);
		}
	}

	@Priv
	public void initDialog() {
		String path = $V("Path");
		if (StringUtil.isNotEmpty(path)) {
			long siteID = SiteBL.getCurrentSite();
			if (ObjectUtil.notEmpty($V("SiteID"))) {
				siteID = Long.parseLong($V("SiteID"));
			}
			String platform = $V("PlatFormID");
			String prefix = PublishPlatformUtil.getPublishPlatformRoot(siteID, platform);
			String fileName = FileUtil.normalizePath(prefix + path);
			File f = new File(fileName);
			if (!f.exists()) {
				f.mkdirs();
			}
		}
	}

	@Priv
	public void bindGrid(DataGridAction dga) {
		String path = $V("Path");
		if (path == null) {
			path = "";
		}
		final String selectType = $V("SelectType");
		final String extension = $V("Extension") == null?null:"," + $V("Extension") + ",";

		long siteID = SiteBL.getCurrentSite();
		if (ObjectUtil.notEmpty($V("SiteID"))) {
			siteID = Long.parseLong($V("SiteID"));
		}
		if (!PrivCheck.check("com.zving.cms.Site.File." + siteID)) {
			return;
		}
		String alias = SiteUtil.getSiteRoot(siteID);
		if (!FileUtil.exists(alias)) {
			ZCSite site = SiteUtil.getDAO(siteID);
			SiteBL.initDirectory(site);
		}
		String prefix = PublishPlatformUtil.getPublishPlatformRoot(siteID, $V("PlatformID"));
		path = FileUtil.normalizePath(prefix + path);
		DataTable dt = new DataTable();
		dt.insertColumn("Icon");
		dt.insertColumn("Name");
		dt.insertColumn("Type");
		dt.insertColumn("DirFlag");
		dt.insertColumn("Size");
		dt.insertColumn("LastModified");
		File dir = new File(path);
		if (!dir.exists()) {
			return;
		}
		ZCTemplate t = new ZCTemplate();
		String[] colums = { "ID", "SiteID", "PlatformID", "FileName",
				"TemplateType", "Name", "FileSize", "ModifyTime" };
		t.setOperateColumns(colums);
		Q qb = new Q(" where SiteID=?", new Object[] { Long.valueOf(siteID) });
		qb.append(" and PlatformID=?", new Object[] { $V("PlatformID") });

		final DAOSet<ZCTemplate> set = t.query(qb);
		final String name = $V("Name");
		File[] arr = dir.listFiles(new FileFilter() {
			public boolean accept(final File f) {
				if ((StringUtil.isNotEmpty(name)) && (f.getName().indexOf(name) < 0)) {
					return false;
				}
				if (f.isFile()) {
					if ("Path".equals(selectType)) {
						return false;
					}
					String ext = FileUtil.getExtension(f.getName());
					if ((ObjectUtil.notEmpty(extension)) && ((ObjectUtil.empty(ext)) || (extension.indexOf(ext) < 0))) {
						return false;
					}
				}
				if ((f.getName().equals(".svn")) || (f.getName().equals(".temp")) || (f.getName().equals(".deploy")) || (f.getName().equals(".deploytemp"))) {
					return false;
				}
				if (f.getName().endsWith(".template.html")) {
					new Thread() {
						public void run() {
							try {
								TemplateService.scan(f, set, FileUI.this.$V("PlatformID"));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}.start();
				}
				return true;
			}
		});
		String sort = GridSort.getSortString(dga);
		if (sort.length() > 10) {
			sort = sort.substring(10);
		}
		if (sort.indexOf(',') > 0) {
			sort = sort.substring(0, sort.indexOf(','));
		}
		String field = null;
		String direction = "ASC";
		if (sort.indexOf(' ') > 0) {
			field = sort.substring(0, sort.indexOf(' '));
			direction = sort.substring(sort.indexOf(' ') + 1);
		} else {
			field = sort;
		}
		if (StringUtil.isNull(field)) {
			field = "Name";
		}
		final String sortField = field;
		final String sortDirection = direction;
		arr = (File[]) ObjectUtil.sort(arr, new Comparator<File>() {
			public int compare(File f1, File f2) {
				int flag = 0;
				if (sortField.equalsIgnoreCase("Name")) {
					flag = f1.getName().compareTo(f2.getName());
				} else if (sortField.equalsIgnoreCase("Size")) {
					long l1 = f1.isFile() ? f1.length() : 0L;
					long l2 = f2.isFile() ? f2.length() : 0L;
					flag = l1 > l2 ? 1 : -1;
				} else if (sortField.equalsIgnoreCase("Type")) {
					String ext1 = "";
					String ext2 = "";
					if ((f1.isFile()) && (f1.getName().indexOf('.') > 0)) {
						ext1 = f1.getName().substring(f1.getName().indexOf('.'));
					}
					if ((f2.isFile()) && (f2.getName().indexOf('.') > 0)) {
						ext2 = f2.getName().substring(f2.getName().indexOf('.'));
					}
					flag = ext1.compareTo(ext2);
				} else if (sortField.equalsIgnoreCase("LastTime")) {
					flag = f1.lastModified() > f2.lastModified() ? 1 : -1;
				}
				return sortDirection.equalsIgnoreCase("ASC") ? flag : -flag;
			}
		});
		if (!field.equalsIgnoreCase("LastTime")) {
			arr = (File[]) ObjectUtil.sort(arr, new Comparator<File>() {
				public int compare(File f1, File f2) {
					if ((f1.isDirectory()) && (!f2.isDirectory())) {
						return -1;
					}
					if ((f2.isDirectory()) && (!f1.isDirectory())) {
						return 1;
					}
					return 0;
				}
			});
		}
		int i = 0;
		int start = dga.getPageIndex() * dga.getPageSize();
		int end = (dga.getPageIndex() + 1) * dga.getPageSize();
		dga.setTotal(arr.length);
		for (File f : arr) {
			i++;
			if (i > start) {
				if (i > end) {
					break;
				}
				String name2 = f.getName();
				if (f.isDirectory()) {
					name2 = name2 + "/";
				}
				Date d = new Date(f.lastModified());
				long length = f.length() / 1024L;
				if (f.length() % 1024L != 0L) {
					length += 1L;
				}
				DecimalFormat df = new DecimalFormat("#,###");
				String size = df.format(length) + " KB";
				if (f.isDirectory()) {
					size = "";
				}
				dt.insertRow(new Object[] { PlatformUtil.getFileIcon(name2),
						f.getName(), getType(name2),
						f.isDirectory() ? "Y" : "N", size,
						DateUtil.toDateTimeString(d) });
			}
		}
		dga.bindData(dt);
	}

	private static String getType(String name) {
		if (name.endsWith("/")) {
			return Lang.get("Contentcore.Block.Directory", new Object[0]);
		}
		if (name.indexOf(".") < 0) {
			return Lang.get("Contentcore.UnknowFile", new Object[0]);
		}
		String ext = ServletUtil.getUrlExtension(name).toLowerCase();
		if (ObjectUtil.in(new Object[] { ext, ".gif", ".jpg", ".png", ".bmp",
				".psd", ".ai", ".jpeg", ".tif", ".tiff" })) {
			return ext.substring(1).toUpperCase() + Lang.get("Contentcore.ImageFile", new Object[0]);
		}
		if (ObjectUtil.in(new Object[] { ext, ".avi", ".mpg", ".flv", ".mpeg",
				".rm", ".rmvb", ".mov", ".wmv", ".wmp", ".mp4" })) {
			return ext.substring(1).toUpperCase() + Lang.get("Contentcore.VideoFile", new Object[0]);
		}
		if (ObjectUtil.in(new Object[] { ext, ".mp3", ".wma", ".wav" })) {
			return ext.substring(1).toUpperCase() + Lang.get("Contentcore.AudioFile", new Object[0]);
		}
		if (ObjectUtil.in(new Object[] { ext, ".zip", ".rar", ".tar", ".gz",
				".z", ".iso", ".cab", ".jar" })) {
			return ext.substring(1).toUpperCase() + Lang.get("Contentcore.CompressedFile", new Object[0]);
		}
		if (ObjectUtil.in(new Object[] { ext, ".doc", ".docx" })) {
			return Lang.get("Contentcore.WordDocument", new Object[0]);
		}
		if (ObjectUtil.in(new Object[] { ext, ".xls", ".xlsx" })) {
			return Lang.get("Contentcore.ExcelSpreadsheet", new Object[0]);
		}
		if (ObjectUtil.in(new Object[] { ext, ".ppt", ".pptx" })) {
			return Lang.get("Contentcore.PowerpointPresentation", new Object[0]);
		}
		if (ObjectUtil.in(new Object[] { ext, ".pdf" })) {
			return Lang.get("Contentcore.PDFDocument", new Object[0]);
		}
		if (ObjectUtil.in(new Object[] { ext, ".fla", ".swf" })) {
			return Lang.get("Contentcore.FlashFile", new Object[0]);
		}
		if (ext.equalsIgnoreCase(".js")) {
			return Lang.get("Contentcore.JavascriptFile", new Object[0]);
		}
		if (ext.equalsIgnoreCase(".css")) {
			return Lang.get("Contentcore.CSSFile", new Object[0]);
		}
		if (name.endsWith(".template.html")) {
			return Lang.get("Contentcore.TemplateFile", new Object[0]);
		}
		return ext.substring(1).toUpperCase() + Lang.get("Contentcore.File", new Object[0]);
	}

	@Priv
	public void addFile() {
		String name = $V("Name").trim();
		String path = $V("Path");
		String type = $V("Type");
		if (path == null) {
			path = "";
		}
		if (ObjectUtil.empty(name)) {
			return;
		}
		long siteID = SiteBL.getCurrentSite();
		if (ObjectUtil.notEmpty($V("SiteID"))) {
			siteID = Long.parseLong($V("SiteID"));
		}
		String prefix = PublishPlatformUtil.getPublishPlatformRoot(siteID, $V("PlatformID"));
		String fileName = FileUtil.normalizePath(prefix + path + name);
		File f = new File(fileName);
		if (f.exists()) {
			fail(Lang.get("Contentcore.CreateFailureTargetNameExists", new Object[0]));
			return;
		}
		if ("File".equals(type)) {
			String ext = FileUtil.getExtension(name);
			if ((StringUtil.isEmpty(ext)) || (!AllowEditFileType.isAllow(ext.toLowerCase()))) {
				fail(Lang.get("Contentcore.NotSupport", new Object[0]) + ext + Lang.get("Contentcore.File", new Object[0]));
				return;
			}
			try {
				f.createNewFile();
				TemplateService.scan(f, $V("PlatformID"));
			} catch (IOException e) {
				e.printStackTrace();
				fail(e.getMessage());
				return;
			}
		} else {
			f.mkdir();
		}
		success(Lang.get("Common.ExecuteSuccess", new Object[0]));
	}

	@Priv
	public void rename() {
		String oldName = $V("OldName");
		String newName = $V("Name").trim();
		String path = $V("Path");
		if (path == null) {
			path = "";
		}
		if (ObjectUtil.empty(oldName)) {
			return;
		}
		long siteID = SiteBL.getCurrentSite();
		if (ObjectUtil.notEmpty($V("SiteID"))) {
			siteID = Long.parseLong($V("SiteID"));
		}
		String prefix = PublishPlatformUtil.getPublishPlatformRoot(siteID, $V("PlatformID"));
		String oldFileName = FileUtil.normalizePath(prefix + path + oldName);
		String newFileName = FileUtil.normalizePath(prefix + path + newName);
		File oldFile = new File(oldFileName);
		File newFile = new File(newFileName);
		if ((oldFile.isFile()) && (!AllowedUploadFileType.isAllow(newFileName))) {
			fail(Lang.get("Contentcore.ExtensionNotAllowed", new Object[0]));
			return;
		}
		if (newFile.exists()) {
			fail(Lang.get("Contentcore.RenameFailsNameExists", new Object[0]));
			return;
		}
		oldFile.renameTo(newFile);
		TemplateService.rename(oldFile, newFile, $V("PlatformID"));
	}

	@Priv("ContentCore.FileMenu.Delete")
	public void delete() {
		String names = $V("Names");
		String path = $V("Path");
		if (path == null) {
			path = "";
		}
		if (ObjectUtil.empty(names)) {
			return;
		}
		long siteID = SiteBL.getCurrentSite();
		if (ObjectUtil.notEmpty($V("SiteID"))) {
			siteID = Long.parseLong($V("SiteID"));
		}
		String prefix = PublishPlatformUtil.getPublishPlatformRoot(siteID, $V("PlatformID"));
		String[] arr = names.split("\\,");
		for (String name : arr) {
			String fileName = FileUtil.normalizePath(prefix + path + name);
			File f = new File(fileName);
			if ((f.exists()) && (!FileUtil.delete(f))) {
				fail(Lang.get("Common.DeleteFailed", new Object[0]) + ":" + f.getName());
				return;
			}
			TemplateService.scan(f, $V("PlatformID"));
		}
		success(Lang.get("Common.DeleteSuccess", new Object[0]));
	}

	@Priv("ContentCore.FileMenu")
	public void initEdit() {
		String name = ServletUtil.getChineseParameter("Name");
		String path = $V("Path");
		if (path == null) {
			path = "";
		}
		if (ObjectUtil.empty(name)) {
			return;
		}
		String ext = FileUtil.getExtension(name).toLowerCase();
		if (!AllowEditFileType.isAllow(ext)) {
			fail(Lang.get("Contentcore.NotSupportFileType", new Object[0]));
			return;
		}
		long siteID = SiteBL.getCurrentSite();
		if (ObjectUtil.notEmpty($V("SiteID"))) {
			siteID = Long.parseLong($V("SiteID"));
		}
		String prefix = PublishPlatformUtil.getPublishPlatformRoot(siteID, $V("PlatformID"));
		String fileName = FileUtil.normalizePath(prefix + path + name);
		byte[] bs = FileUtil.readByte(fileName);
		try {
			if (StringUtil.isUTF8(bs)) {
				$S("Content", StringUtil.htmlEncode(new String(bs, "UTF-8")));
			} else {
				$S("Content", StringUtil.htmlEncode(new String(bs, "GBK")));
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Priv("ContentCore.FileMenu")
	public void initImageView() {
		String name = ServletUtil.getChineseParameter("Name");
		String path = $V("Path");
		if (path == null) {
			path = "";
		}
		if (ObjectUtil.empty(name)) {
			return;
		}
		long siteID = SiteBL.getCurrentSite();
		if (ObjectUtil.notEmpty($V("SiteID"))) {
			siteID = Long.parseLong($V("SiteID"));
		}
		String alias = PublishPlatformUtil.getPublishPlatformPath(siteID, $V("PlatformID"));
		String prefix = "preview/" + alias + "/";
		String fileName = Config.getContextPath() + FileUtil.normalizePath(new StringBuilder(String.valueOf(prefix)).append(path).append(name).toString());
		$S("FullPath", fileName + "?t=" + System.currentTimeMillis());
	}

	@Verify(ignoreAll = true)
	@Priv("ContentCore.FileMenu.Edit")
	public void save() {
		String name = $V("Name");
		String path = $V("Path");
		String content = $V("Content");
		if (path == null) {
			path = "";
		}
		if (ObjectUtil.empty(name)) {
			return;
		}
		String ext = FileUtil.getExtension(name).toLowerCase();
		if (!AllowEditFileType.isAllow(ext)) {
			fail(Lang.get("Contentcore.NotSupportFileType", new Object[0]));
			return;
		}
		long siteID = SiteBL.getCurrentSite();
		if (ObjectUtil.notEmpty($V("SiteID"))) {
			siteID = Long.parseLong($V("SiteID"));
		}
		String prefix = PublishPlatformUtil.getPublishPlatformRoot(siteID, $V("PlatformID"));
		String fileName = FileUtil.normalizePath(prefix + path + name);
		try {
			if (Config.getGlobalCharset().equalsIgnoreCase("UTF-8")) {
				byte[] utfspacebyte = { -62, -96 };
				String UTFSpace = new String(utfspacebyte, "UTF-8");
				content = content.replace(UTFSpace, " ");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		FileUtil.writeText(fileName, content);
		TemplateService.scan(new File(fileName), $V("PlatformID"));
		success(Lang.get("Common.SaveSuccess", new Object[0]));
	}

	@Priv("ContentCore.FileMenu.Upload")
	public void upload(UploadAction ua) {
		String path = $V("Path");
		if (path == null) {
			path = "";
		}
		String platformID = $V("PlatformID");
		if (StringUtil.isEmpty(platformID)) {
			platformID = "pc";
		}
		long siteID = SiteBL.getCurrentSite();
		if (ObjectUtil.notEmpty($V("SiteID"))) {
			siteID = Long.parseLong($V("SiteID"));
		}
		path = PublishPlatformUtil.getPublishPlatformRoot(siteID, platformID) + path;
		path = FileUtil.normalizePath(path + "/");
		String autoUnzip = $V("AutoUnzip");
		List<String> fileNames = new ArrayList<String>();
		for (FileItem file : ua.getAllFiles()) {
			String ext = FileUtil.getExtension(file.getName());
			if (!AllowedUploadFileType.isAllow(ext)) {
				fail(Lang.get("Contentcore.ProhibitUploadFile") + ": " + ext);
				return;
			}
			if ((ObjectUtil.in(new Object[] { ext, "rar", "zip" })) && (YesOrNo.isYes(autoUnzip))) {
				String tempPath = path + ".temp/" + System.currentTimeMillis();
				int i = 0;
				while (new File(tempPath).exists()) {
					tempPath = path + ".temp/" + (System.currentTimeMillis() + ++i);
				}
				new File(tempPath).mkdirs();
				String zipFile = tempPath + "/_." + ext;
				try {
					file.write(new File(zipFile));
				} catch (Exception e) {
					e.printStackTrace();
					fail(Lang.get("Contentcore.UploadFailureMessage", new Object[0]) + ":" + StringUtil.htmlEncode(e.getMessage()));
					return;
				}
				try {
					if (ext.equals("zip")) {
						ZipUtil.unzip(zipFile, tempPath);
					} else {
						RarUtil.unrar(zipFile, tempPath);
					}
					FileUtil.delete(zipFile);
					filter(new File(tempPath));
					
					//自动压缩css/js
					YuiCompressUtil.compress("", tempPath);
					
					FileUtil.copy(tempPath, path);
					TemplateService.scan(new File(path, platformID), platformID);
				} catch (Exception e) {
					e.printStackTrace();
					fail(Lang.get("Contentcore.UploadFailureMessage", new Object[0]) + ":" + StringUtil.htmlEncode(e.getMessage()));
					return;
				} finally {
					if (new File(zipFile).exists()) {
						FileUtil.delete(zipFile);
					}
					FileUtil.delete(tempPath);
				}
			} else {
				try {
					File f = new File(path + file.getName());
					file.write(f);
					
					//自动压缩css/js
					YuiCompressUtil.compress(path, file.getName());
					
					TemplateService.checkTemplateCharset(f);
					TemplateService.scan(f, $V("PlatformID"));
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
					return;
				}
			}
		}
		success(Lang.get("Contentcore.UploadSuccess", new Object[0]));
	}

	private static void filter(File p) {
		for (File f : p.listFiles()) {
			if (f.isFile()) {
				if (!AllowedUploadFileType.isAllow(f.getAbsolutePath())) {
					f.delete();
					continue;
				}
				if (f.getName().endsWith(".template.html")) {
					TemplateService.checkTemplateCharset(f);
				}
			}
			if (f.isDirectory()) {
				filter(f);
			}
		}
	}

	@Priv("ContentCore.FileMenu.Export")
	@Alias(value = "contentcore/file/export", alone = true)
	public void export(ZAction za) {
		String files = ServletUtil.getChineseParameter("Files");
		String base = ServletUtil.getChineseParameter("Base");
		try {
			String[] arr = StringUtil.splitEx(files, ",");
			for (int i = 0; i < arr.length; i++) {
				String f = arr[i];
				if (ObjectUtil.notEmpty(f)) {
					long siteID = SiteBL.getCurrentSite();
					if (ObjectUtil.notEmpty($V("SiteID"))) {
						siteID = Long.parseLong($V("SiteID"));
					}
					f = PublishPlatformUtil.getPublishPlatformRoot(siteID, $V("PlatformID")) + base + "/" + f;
					f = FileUtil.normalizePath(f);
				}
				arr[i] = f;
			}
			za.setBinaryMode(true);
			String fileName = "FileExport_" + DateUtil.toString(new Date(), "yyyyMMdd-HHmmss") + ".zip";
			IOUtil.setDownloadFileName(za.getRequest(), za.getResponse(), fileName);
			ZipUtil.zipBatch(arr, za.getResponse().getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Priv
	public void initAllowTypes() {
		$S("AllowedTypes", StringUtil.join(AllowedUploadFileType.getTypes()));
	}
}
