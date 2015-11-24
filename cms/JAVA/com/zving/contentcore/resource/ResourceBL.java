package com.zving.contentcore.resource;

import com.zving.contentcore.ICoreURLModifier;
import com.zving.contentcore.bl.PublishPlatformBL;
import com.zving.contentcore.code.CatalogResourceTypeCode;
import com.zving.contentcore.item.PCPublishPlatform;
import com.zving.contentcore.property.impl.ResourceDirectoryRule;
import com.zving.contentcore.service.CoreURLModifierService;
import com.zving.contentcore.service.PublishPlatformService;
import com.zving.contentcore.util.PublishPlatformUtil;
import com.zving.contentcore.util.SiteUtil;
import com.zving.contentcore.util.ZImageUtil;
import com.zving.framework.media.BmpUtil;
import com.zving.framework.media.ImageJDKUtil;
import com.zving.framework.media.VideoUtil;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.thirdparty.commons.fileupload.FileItem;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.util.NoUtil;
import com.zving.schema.ZCResources;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;

public class ResourceBL {
	public static final String RelativeDirectory = "upload/resources/";
	public static final String RESOURCESIDKEY = "ZCResourcesID";

	public static long getResourcesMaxID() {
		return NoUtil.getMaxID("ZCResourcesID");
	}

	public static String generateResourceFileName(ZCResources dao) {
		return dao.getID()+"";
	}

	public static String getResourceRelativeDirectory(long siteID, String resourceType, Date addTime) {
		StringBuilder sb = new StringBuilder();
		sb.append("upload/resources/");
		sb.append(resourceType.toLowerCase()).append("/");
		String rule = SiteUtil.getPropertyValue(siteID, "ResourceDirectoryRule");
		if (ResourceDirectoryRule.Rule_Year.equals(rule)) {
			sb.append(DateUtil.toString(addTime, "yyyy/"));
		} else if (ResourceDirectoryRule.Rule_Year_Month.equals(rule)) {
			sb.append(DateUtil.toString(addTime, "yyyy/MM/"));
		} else {
			sb.append(DateUtil.toString(addTime, "yyyy/MM/dd/"));
		}
		return sb.toString();
	}

	public static void copyResourceFile(File file, ZCResources resource) {
		String siteRoot = SiteUtil.getSiteRoot(resource.getSiteID());

		String targetPath = siteRoot + resource.getPath();
		FileUtil.copy(file, targetPath);

		dealSpecialResourceType(resource);
	}

	public static void addResourceFile(FileItem fileItem, ZCResources resource) {
		String filePath = SiteUtil.getSiteRoot(resource.getSiteID()) + resource.getPath();
		try {
			FileUtil.mkdir(filePath.substring(0, filePath.lastIndexOf("/") + 1));
			fileItem.write(new File(filePath));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		specialImgConvertJpg(resource);

		dealSpecialResourceType(resource);
	}

	public static void specialImgConvertJpg(ZCResources resource) {
		if ("bmp".equalsIgnoreCase(resource.getSuffix())) {
			String sResourcePath = resource.getPath().substring(0, resource.getPath().lastIndexOf(".")) + ".jpg";
			String filePath = SiteUtil.getSiteRoot(resource.getSiteID()) + resource.getPath();
			String sFilePath = filePath.substring(0, filePath.lastIndexOf(".")) + ".jpg";
			BufferedImage img = BmpUtil.read(filePath);
			img = ImageJDKUtil.scaleFixed(img, img.getWidth(), img.getHeight(), true);
			try {
				ImageJDKUtil.writeImageFile(sFilePath, img);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			resource.setLogoFile(sResourcePath);
			resource.setPath(sResourcePath);
			resource.setName(sFilePath.substring(sFilePath.lastIndexOf("/") + 1));
			resource.setSuffix("jpg");
			FileUtil.delete(filePath);
		}
	}

	public static void addResourceFile(byte[] bytes, ZCResources resource) {
		String filePath = SiteUtil.getSiteRoot(resource.getSiteID()) + resource.getPath();
		try {
			FileUtil.mkdir(filePath.substring(0, filePath.lastIndexOf("/") + 1));
			FileUtil.writeByte(new File(filePath), bytes);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		dealSpecialResourceType(resource);
	}

	public static void delResourceFile(ZCResources resource) {
		String siteRoot = SiteUtil.getSiteRoot(resource.getSiteID());
		String path = siteRoot + resource.getPath();
		if (!FileUtil.delete(path)) {
			LogUtil.warn("Delete resource file failed：" + path);
			return;
		}
		if (CatalogResourceTypeCode.isImage(resource.getSuffix())) {
			final String prefix = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("_") + 1);
			File f = new File(path.substring(0, path.lastIndexOf("/")));
			String[] thumbnails = f.list(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.startsWith(prefix);
				}
			});
			for (String thumbnail : thumbnails) {
				if (!FileUtil.delete(thumbnail)) {
					LogUtil.warn("Delete resource's thumbnail failed：" + thumbnail);
				}
			}
		}
	}

	public static void dealSpecialResourceType(ZCResources resource) {
		String filePath = SiteUtil.getSiteRoot(resource.getSiteID()) + resource.getPath();
		if (CatalogResourceTypeCode.isImage(resource.getResourceType())) {
			ZImageUtil.pressImageByCatalog(filePath, resource.getSiteID(), resource.getCatalogID());

			ZImageUtil.saveThumbnail(filePath, 120, 120, true);
			ZImageUtil.saveThumbnail(filePath, 500, 500, true);

			PublishPlatformUtil.createImagesForAll(resource.getPath(), resource.getSiteID());

			String logoFile = ResourceUtil.getResourceLogoFile(resource);
			if (StringUtil.isNotEmpty(logoFile)) {
				resource.setLogoFile(logoFile);
			}
		} else if (CatalogResourceTypeCode.isVideo(resource.getResourceType())) {
			String siteRoot = SiteUtil.getSiteRoot(resource.getSiteID());
			VideoUtil.captureImage(siteRoot + resource.getPath(), siteRoot + resource.getLogoFile());
			if ("mp4".equalsIgnoreCase(resource.getSuffix())) {
				ResourceUtil.resetVideoCodec(siteRoot + resource.getPath());
			}
		}
	}
	
	/**
	 * 非PC平台下的原图如果不存在则从PC平台复制一份
	 * @param path
	 * @param context
	 */
	public static void copyPlatformImages(String path, AbstractExecuteContext context) {
		String platformID = context.eval("PlatformID");
		if (StringUtil.isNotEmpty(platformID) && !platformID.equals(PCPublishPlatform.ID)) {
			
			if (path.indexOf('?') > 0) {
				path = path.substring(0, path.lastIndexOf('?'));
			}
			if (path.indexOf('#') > 0) {
				path = path.substring(0, path.lastIndexOf('#'));
			}
			int i1 = path.lastIndexOf('/');
			int i2 = path.lastIndexOf('_');
			int i3 = path.lastIndexOf('x');
			int i4 = path.lastIndexOf('.');
			if (!((i2 < 0) || (i3 < 0) || (i4 < 0) || (i1 > i2) || (i1 > i3) || (i2 > i3) || (i3 > i4))) {
				path = path.substring(0, i2) + path.substring(i4);
			}

			if (StringUtil.isNotEmpty(path) && CatalogResourceTypeCode.isImage(ResourceUtil.getResourceType(FileUtil.getExtension(path)))) {
				long siteID = context.evalLong("Site.ID");
				File pf = new File(SiteUtil.getSiteRoot(siteID, platformID) + path);
				if (!pf.exists()) {
					File pcFile = new File(SiteUtil.getSiteRoot(siteID) + path);
					if (pcFile.exists()) {
						LogUtil.info(String.format("InitCopyImg: platform=%s, img=%s", platformID, path));
						PublishPlatformUtil.createImages(path, siteID, PublishPlatformService.getInstance().get(platformID));
					}
				}
			}
		}
	}

	public static String getAbsoluteURL(String path, AbstractExecuteContext context) {
		if ((ObjectUtil.empty(path)) || (path.indexOf(":/") > 0)) {
			return path;
		}
		//zq 2015/10/29  没有启动H5平台前上传的图片在启用H5平台后没有同步复制
		copyPlatformImages(path, context);
		
		String prefix = context.eval("Prefix");//context.eval("Site.ID") context.evalLong("Site.ID")
		if (context.isPreview()) {
			return prefix + path;
		}
		for (ICoreURLModifier um : CoreURLModifierService.getInstance().getAll()) {
			if (!um.passResource(context, path)) {
				return um.modifyResource(context, path);
			}
		}
		return prefix + path;
	}

	public static String dealPath(String bodyText, AbstractExecuteContext context) {
		HtmlResourceProcessor brp = new HtmlResourceProcessor(context, bodyText);
		return brp.process();
	}
}
