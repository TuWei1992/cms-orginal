package com.zving.media.bl;

import com.zving.contentcore.code.CatalogResourceTypeCode;
import com.zving.contentcore.property.PropertyUtil;
import com.zving.contentcore.resource.ResourceBL;
import com.zving.contentcore.resource.ResourceUtil;
import com.zving.contentcore.util.InternalURLUtil;
import com.zving.contentcore.util.SiteUtil;
import com.zving.cxdata.config.CmsImageTimeSuffix;
import com.zving.framework.User;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.IOUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.util.NoUtil;
import com.zving.platform.util.OrderUtil;
import com.zving.schema.ZCContent;
import com.zving.schema.ZCImage;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImageBL {
	public static String getImageAbsolutePath(ZCImage image) {
		StringBuilder sb = new StringBuilder();
		sb.append(SiteUtil.getSiteRoot(image.getSiteID()));
		sb.append(image.getPath());
		FileUtil.mkdir(sb.toString());
		sb.append(image.getFileName());
		return sb.toString();
	}

	public static List<File> getThumbnails(ZCImage image) {
		List<File> list = new ArrayList();
		File dir = new File(SiteUtil.getSiteRoot(image.getSiteID()) + image.getPath());
		for (File f : dir.listFiles()) {
			if (f.getName().indexOf("_") > -1) {
				String prefix = f.getName().substring(0, f.getName().lastIndexOf("_"));
				if (prefix.equals(image.getFileName().substring(0, image.getFileName().lastIndexOf(".")))) {
					list.add(f);
				}
			}
		}
		return list;
	}

	public static String getImageSrc(DataRow dr, boolean isThumbnail) {
		return getImageSrc(dr, isThumbnail, 120, 120);
	}

	public static String getImageSrc(DataRow dr, boolean isThumbnail, int width, int height) {
		StringBuilder sb = new StringBuilder();
		sb.append(dr.getString("Path"));
		if ((isThumbnail) && ((dr.getInt("Width") > 120) || (dr.getInt("Height") > 120))) {
			sb.append(dr.getString("FileName").substring(0, dr.getString("FileName").lastIndexOf(".")));
			sb.append("_").append(width).append("x").append(height);
			sb.append(".");
			sb.append(FileUtil.getExtension(dr.getString("FileName")));
		} else {
			sb.append(dr.getString("FileName"));
		}
		if (CmsImageTimeSuffix.isCurrentTimeMillion()) {
			return sb.toString() + "?" + System.currentTimeMillis();
		} else if (CmsImageTimeSuffix.isFileModifyTime()){
			Long siteID = dr.getLong("SiteID");
			if (siteID != null) {
				File f = new File(SiteUtil.getSiteRoot(siteID) + sb);
				if (f.exists()) {
					return sb.toString() + "?" + f.lastModified();
				}
			}
			return sb.toString();
		} else {
			return sb.toString();
		}
		
	}

	public static String getImageThumbnailSrc(ZCImage image) {
		if (ObjectUtil.empty(image)) {
			return "";
		}
		return getImageSrc(image.toDataRow(), true);
	}

	public static void decodeImagePath(DataTable dt) {
		if (!dt.containsColumn("ThumbnailPath")) {
			dt.insertColumn("ThumbnailPath");
		}
		if (!dt.containsColumn("ImagePath")) {
			dt.insertColumn("ImagePath");
		}
		if (!dt.containsColumn("ThumbnailPath2")) {
			dt.insertColumn("ThumbnailPath2");
		}
		for (DataRow dr : dt) {
			dr.set("ThumbnailPath", getImageSrc(dr, true));
			dr.set("ThumbnailPath2", getImageSrc(dr, true, 500, 500));
			dr.set("ImagePath", getImageSrc(dr, false));
		}
	}

	public static void decodeImagePath(AbstractExecuteContext context, DataTable dt) {
		decodeImagePath(dt);
		if (!dt.containsColumn("LinkUrl")) {
			dt.insertColumn("LinkUrl");
		}
		for (DataRow dr : dt) {
			dr.set("ThumbnailPath", ResourceBL.getAbsoluteURL(dr.getString("ThumbnailPath"), context));
			dr.set("ThumbnailPath2", ResourceBL.getAbsoluteURL(dr.getString("ThumbnailPath2"), context));
			dr.set("ImagePath", ResourceBL.getAbsoluteURL(dr.getString("ImagePath"), context));
			dr.set("LinkUrl", InternalURLUtil.getActualURL(dr.getString("SourceURL"), context));
		}
	}

	public static Mapx<String, Object> dealCopyImage(ZCContent imgGroup, boolean addDetail) {
		Mapx<String, Object> values = new Mapx();
		ZCContent newImgGroup = new ZCContent();
		if ((ObjectUtil.empty(Integer.valueOf(imgGroup.getCopyType()))) || (1 >= imgGroup.getCopyType())) {
			values = imgGroup.toMapx();
		} else {
			newImgGroup.setID(imgGroup.getCopyID());
			newImgGroup.fill();
			values.putAll(newImgGroup.toMapx());
			values.putAll(PropertyUtil.parse(newImgGroup.getConfigProps()));
		}
		if (imgGroup.getCopyType() != 3) {
			values.put("ID", Long.valueOf(imgGroup.getID()));
		}
		values.put("CopyType", Integer.valueOf(imgGroup.getCopyType()));
		values.put("CopyID", Long.valueOf(imgGroup.getCopyID()));

		return values;
	}

	public static Mapx<String, Object> dealCopyImage(Mapx<String, Object> imageMap, boolean addDetail) {
		ZCContent imgGroup = new ZCContent();
		imgGroup.setValue(imageMap);
		return dealCopyImage(imgGroup, addDetail);
	}

	public static ZCImage createImageDAO(String fileName, long fileSize, ZCContent imgGroup) {
		String ext = FileUtil.getExtension(fileName);
		if (!CatalogResourceTypeCode.isImage(ResourceUtil.getResourceType(ext))) {
			return null;
		}
		ZCImage image = new ZCImage();
		image.setValue(imgGroup.toMapx());
		image.setID(NoUtil.getMaxID("ZCImageID"));
		image.setGroupID(imgGroup.getID());
		image.setSuffix(ext);

		String oldname = fileName.substring(0, fileName.lastIndexOf("."));
		image.setOldName(oldname);
		image.setName(fileName);
		image.setOldFileName(fileName);
		image.setFileName(fileName);
		image.setFileSize(IOUtil.byteCountToDisplaySize(fileSize));
		image.setWidth(0L);
		image.setHeight(0L);
		image.setOrderFlag(OrderUtil.getDefaultOrder());
		image.setHitCount(0L);
		image.setAddTime(new Date());
		image.setAddUser(User.getUserName());
		String dir = ResourceBL.getResourceRelativeDirectory(image.getSiteID(), "Image", image.getAddTime());
		image.setPath(dir);
		return image;
	}
}
