package com.zving.media.util;

import com.zving.contentcore.bl.HistoryBL;
import com.zving.contentcore.resource.ResourceBL;
import com.zving.contentcore.resource.ResourceManager;
import com.zving.contentcore.resource.ResourceRelaBL;
import com.zving.contentcore.resource.ResourceUtil;
import com.zving.contentcore.util.PublishPlatformUtil;
import com.zving.contentcore.util.SiteUtil;
import com.zving.contentcore.util.ZImageUtil;
import com.zving.framework.User;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.Transaction;
import com.zving.framework.i18n.Lang;
import com.zving.framework.media.ImageUtil;
import com.zving.framework.orm.DAOUtil;
import com.zving.framework.security.PrivCheck;
import com.zving.framework.thirdparty.commons.fileupload.FileItem;
import com.zving.framework.utility.Errorx;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.IOUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.media.bl.ImageBL;
import com.zving.platform.code.YesOrNo;
import com.zving.platform.util.NoUtil;
import com.zving.platform.util.OrderUtil;
import com.zving.schema.ZCContent;
import com.zving.schema.ZCImage;
import com.zving.schema.ZCResources;
import java.awt.Dimension;
import java.io.File;
import java.util.Date;

public class Image {
	private Mapx<String, Object> properties;
	private Transaction trans;
	public String historyFileName = "";
	public String reStoreFileName = "";

	public void insert() {
		FileItem fileItem = (FileItem) this.properties.get("FileItem");

		ZCContent imgGroup = new ZCContent();
		imgGroup.setID(this.properties.getLong("GroupID"));
		imgGroup.fill();
		if ((!PrivCheck.check("com.zving.cms.Catalog.Content.Add." + imgGroup.getCatalogID())) && (!PrivCheck.check("com.zving.cms.Catalog.Content.Edit." + imgGroup.getCatalogID()))) {
			Errorx.addError(Lang.get("Media.NoPriv", new Object[0]));
			return;
		}
		ZCImage image = new ZCImage();
		image.setID(NoUtil.getMaxID("ZCImageID"));
		image.setValue(this.properties);
		image.setCatalogID(imgGroup.getCatalogID());
		image.setCatalogInnerCode(imgGroup.getCatalogInnerCode());
		image.setSiteID(imgGroup.getSiteID());

		image.setOldName(fileItem.getName().substring(0, fileItem.getName().lastIndexOf(".")));

		String ext = fileItem.getName().substring(fileItem.getName().lastIndexOf(".") + 1).toLowerCase();
		image.setSuffix(ext);
		image.setOldFileName(fileItem.getName());

		image.setFileSize(IOUtil.byteCountToDisplaySize(fileItem.getSize()));
		image.setWidth(0L);
		image.setHeight(0L);
		image.setOrderFlag(OrderUtil.getDefaultOrder());
		image.setHitCount(0L);
		image.setAddTime(new Date());
		image.setAddUser(User.getUserName());
		image.setPath(ResourceBL.getResourceRelativeDirectory(image.getSiteID(), "Image", image.getAddTime()));

		ResourceManager rm = new ResourceManager(image.getAddUser(), this.trans);
		ZCResources resource = rm.add(fileItem.getName(), fileItem.getSize(), image.getCatalogID(), image.getSiteID());
		image.setFileName(resource.getName());
		ResourceBL.addResourceFile(fileItem, resource);

		String filePath = ImageBL.getImageAbsolutePath(image);
		if ("bmp".equalsIgnoreCase(ext)) {
			filePath = SiteUtil.getSiteRoot(resource.getSiteID()) + resource.getPath();
			String path = resource.getPath();
			image.setPath(path.substring(0, path.lastIndexOf("/") + 1));
			image.setSuffix(FileUtil.getExtension(path));
			image.setFileName(resource.getName());
		}
		try {
			Dimension dim = ImageUtil.getDimension(filePath);
			image.setWidth((long) dim.getWidth());
			image.setHeight((long) dim.getHeight());
		} catch (Exception e) {
			LogUtil.error("图片写入磁盘失败：Image.ID=" + image.getID());
			e.printStackTrace();
		}
		ResourceRelaBL.addRelationship(image.getID(), "Image", resource, this.trans);
		this.trans.add(image, 1);
	}

	public void save() {
		ZCImage image = new ZCImage();
		image.setID(this.properties.getLong("ID"));
		if (image.fill()) {
			if (!PrivCheck.check("com.zving.cms.Catalog.Content.Edit." + image.getCatalogID())) {
				Errorx.addError("权限校验失败");
				return;
			}
			ZCImage imageBak = (ZCImage) image.clone();
			String oldName = image.getFileName();
			image.setValue(this.properties);
			image.setFileName(oldName);
			image.setModifyTime(new Date());
			image.setModifyUser(User.getUserName());

			FileItem fileItem = (FileItem) this.properties.get("FileItem");
			if (fileItem != null) {
				String oldPath = image.getPath() + image.getFileName();

				image.setOldName(fileItem.getName().substring(0, fileItem.getName().lastIndexOf(".")));
				image.setOldFileName(this.properties.getString("Filename"));

				String ext = fileItem.getName().substring(fileItem.getName().lastIndexOf(".") + 1).toLowerCase();
				image.setSuffix(ext);

				String dirPath = SiteUtil.getSiteRoot(image.getSiteID()) + image.getPath();
				File historyFileFrom = new File(dirPath + image.getFileName());
				String oName = historyFileFrom.getName().substring(0, historyFileFrom.getName().lastIndexOf("."));
				String nName = oName + "_backup_" + System.currentTimeMillis() + historyFileFrom.getName().substring(historyFileFrom.getName().lastIndexOf(".")).toLowerCase();
				File historyFileTo = new File(dirPath + nName);
				historyFileFrom.renameTo(historyFileTo);

				this.historyFileName = historyFileTo.getName();
				image.setFileName(image.getFileName().substring(0, image.getFileName().lastIndexOf(".")) + "." + ext);
				image.setFileSize(IOUtil.byteCountToDisplaySize(fileItem.getSize()));
				image.setWidth(0L);
				image.setHeight(0L);

				imageBak.setFileName(nName);

				File newfile = new File(dirPath + image.getFileName());
				try {
					if (!FileUtil.exists(dirPath)) {
						FileUtil.mkdir(dirPath);
					}
					fileItem.write(newfile);
					Dimension dim = ImageUtil.getDimension(dirPath + image.getFileName());
					image.setWidth((long) dim.getWidth());
					image.setHeight((long) dim.getHeight());
				} catch (Exception ex) {
					try {
						fileItem.write(newfile);
					} catch (Exception ex1) {
						LogUtil.error("图片写入磁盘失败：Image.ID=" + image.getID());
						ex1.printStackTrace();
					}
				}
				if (!oldPath.equals(image.getPath() + image.getFileName())) {
					ZCResources r = ResourceUtil.getDAOByPath(image.getSiteID(), oldPath);
					if (r != null) {
						r.setPath(image.getPath() + image.getFileName());
						r.setLogoFile(r.getPath());
						this.trans.update(r);
					} else {
						ResourceManager rm = new ResourceManager(image.getAddUser(), this.trans);
						r = rm.add(fileItem.getName(), fileItem.getSize(), image.getCatalogID(), image.getSiteID());
					}
					ResourceRelaBL.delAllRelationship(image.getID(), DAOUtil.getTableCode(image), this.trans);
					ResourceRelaBL.addRelationship(image.getID(), DAOUtil.getTableCode(image), r, this.trans);
				}
				ZImageUtil.pressImageByCatalog(dirPath + image.getFileName(), image.getSiteID(), image.getCatalogID());
				ZImageUtil.saveThumbnail(newfile.getAbsolutePath());
				ZImageUtil.saveThumbnail(newfile.getAbsolutePath(), 500, 500, true);

				PublishPlatformUtil.createImagesForAll(image.getPath() + newfile.getName(), image.getSiteID());

				ZImageUtil.saveThumbnail(historyFileTo.getAbsolutePath());
				ZImageUtil.saveThumbnail(historyFileTo.getAbsolutePath(), 500, 500, true);

				PublishPlatformUtil.createImagesForAll(image.getPath() + historyFileTo.getName(), image.getSiteID());

				HistoryBL.addHistory(imageBak);
			} else if (YesOrNo.isYes(this.properties.getString("IsRestore"))) {
				image.setValue(this.properties);
				image.setModifyTime(new Date());
				image.setModifyUser(User.getUserName());

				String oldPath = image.getPath() + image.getFileName();

				String dirPath = SiteUtil.getSiteRoot(image.getSiteID()) + imageBak.getPath();
				File historyFileFrom = new File(dirPath + imageBak.getFileName());
				String oName = historyFileFrom.getName().substring(0, historyFileFrom.getName().lastIndexOf("."));
				String nName = oName + "_backup_" + System.currentTimeMillis() + historyFileFrom.getName().substring(historyFileFrom.getName().lastIndexOf(".")).toLowerCase();
				File historyFileTo = new File(dirPath + nName);
				historyFileFrom.renameTo(historyFileTo);
				this.historyFileName = historyFileTo.getName();

				File newFile = new File(dirPath + image.getFileName());
				String restoreFileName = image.getFileName().substring(0, image.getFileName().lastIndexOf("_backup_")) + "." + image.getSuffix();
				File newFileTo = new File(dirPath + restoreFileName);
				FileUtil.copy(newFile.getAbsoluteFile(), newFileTo.getAbsolutePath());
				image.setFileName(restoreFileName);
				imageBak.setFileName(nName);
				if (!oldPath.equals(image.getPath() + image.getFileName())) {
					ZCResources r = ResourceUtil.getDAOByPath(image.getSiteID(), oldPath);
					if (r != null) {
						r.setPath(image.getPath() + image.getFileName());
						r.setLogoFile(r.getPath());
						this.trans.update(r);
					} else {
						ResourceManager rm = new ResourceManager(image.getAddUser(), this.trans);
						r = rm.add(image.getPath() + image.getFileName(), newFileTo.getTotalSpace(), image.getCatalogID(), image.getSiteID());
					}
					ResourceRelaBL.delAllRelationship(image.getID(), DAOUtil.getTableCode(image), this.trans);
					ResourceRelaBL.addRelationship(image.getID(), DAOUtil.getTableCode(image), r, this.trans);
				}
				ZImageUtil.pressImageByCatalog(FileUtil.normalizePath(dirPath + image.getFileName()), image.getSiteID(), image.getCatalogID());
				ZImageUtil.saveThumbnail(FileUtil.normalizePath(newFileTo.getAbsolutePath()));
				ZImageUtil.saveThumbnail(FileUtil.normalizePath(newFileTo.getAbsolutePath()), 500, 500, true);

				PublishPlatformUtil.createImagesForAll(image.getPath() + newFileTo.getName(), image.getSiteID());

				ZImageUtil.saveThumbnail(FileUtil.normalizePath(historyFileTo.getAbsolutePath()));
				ZImageUtil.saveThumbnail(FileUtil.normalizePath(historyFileTo.getAbsolutePath()), 500, 500, true);

				PublishPlatformUtil.createImagesForAll(image.getPath() + historyFileTo.getName(), image.getSiteID());
				HistoryBL.addHistory(imageBak);
			}
			this.trans.update(image);
		}
	}

	public void cuttingSave() throws Exception {
		ZCImage image = new ZCImage();
		image.setID(this.properties.getLong("ID"));
		image.fill();
		if ((!PrivCheck.check("com.zving.cms.Catalog.Content.Add." + image.getCatalogID())) || (!PrivCheck.check("com.zving.cms.Catalog.Content.Edit." + image.getCatalogID()))) {
			Errorx.addError("权限校验失败");
			return;
		}
		image.setModifyTime(new Date());
		image.setModifyUser(User.getUserName());

		String absolutePath = ImageBL.getImageAbsolutePath(image);
		image.setFileSize(IOUtil.byteCountToDisplaySize(new File(absolutePath).length()));
		Dimension dim = ImageUtil.getDimension(absolutePath);
		image.setWidth((long) dim.getWidth());
		image.setHeight((long) dim.getHeight());

		ZImageUtil.saveThumbnail(absolutePath);

		ZImageUtil.saveThumbnail(absolutePath, 500, 500, true);

		PublishPlatformUtil.createImagesForAll(image.getPath() + image.getFileName(), image.getSiteID());
		this.trans.update(image);
	}

	public void setProperties(Mapx<String, Object> properties) {
		this.properties = properties;
	}

	public Mapx<String, Object> getProperties() {
		if (this.properties == null) {
			this.properties = new Mapx();
		}
		return this.properties;
	}

	public void setProperty(String name, Object value) {
		if (this.properties == null) {
			this.properties = new Mapx();
		}
		this.properties.put(name, value);
	}

	public void setTransaction(Transaction trans) {
		this.trans = trans;
	}

	public Transaction getTransaction() {
		return this.trans;
	}
}
