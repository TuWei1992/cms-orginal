package com.zving.contentcore.bl;

import java.io.File;
import java.util.Date;
import java.util.List;

import com.zving.contentcore.ContentCorePlugin;
import com.zving.contentcore.ICatalogType;
import com.zving.contentcore.IContent;
import com.zving.contentcore.IContentType;
import com.zving.contentcore.ICoreURLModifier;
import com.zving.contentcore.IPublishPlatform;
import com.zving.contentcore.code.DetailNameRule;
import com.zving.contentcore.config.MaxListPublishPage;
import com.zving.contentcore.config.MaxListPublishPageWhenContentChange;
import com.zving.contentcore.item.PCPublishPlatform;
import com.zving.contentcore.property.PropertyUtil;
import com.zving.contentcore.property.impl.AlwaysGenerateFile;
import com.zving.contentcore.property.impl.LastDetailTemplateFlag;
import com.zving.contentcore.property.impl.PublishPlatform;
import com.zving.contentcore.service.ContentTypeService;
import com.zving.contentcore.service.CoreURLModifierService;
import com.zving.contentcore.service.PublishPlatformService;
import com.zving.contentcore.tag.CmsPagebarTag;
import com.zving.contentcore.task.PublishTask;
import com.zving.contentcore.task.PublishTheadPool;
import com.zving.contentcore.template.types.AbstractDetailTemplate;
import com.zving.contentcore.util.CatalogUtil;
import com.zving.contentcore.util.PublishPlatformUtil;
import com.zving.contentcore.util.SiteUtil;
import com.zving.contentcore.util.TemplateUtil;
import com.zving.cxdata.property.PlatformContentTemplate;
import com.zving.cxdata.property.PlatformContentTemplateFlag;
import com.zving.framework.cache.CacheManager;
import com.zving.framework.collection.CacheMapx;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.i18n.Lang;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.template.TemplateWriter;
import com.zving.framework.ui.control.LongTimeTask;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.Errorx;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.code.YesOrNo;
import com.zving.platform.service.EventTypeService;
import com.zving.schema.ZCCatalog;
import com.zving.schema.ZCContent;
import com.zving.schema.ZCDefaultTemplate;
import com.zving.schema.ZCPlatformProperty;
import com.zving.schema.ZCSite;
import com.zving.staticize.template.ITemplateType;
import com.zving.staticize.template.TemplateInstance;

public class PublishBL {
	private static Mapx<String, Long> lastMap = new CacheMapx(20000);
	private static final int INTERVAL = 5000;

	public static void publishSiteIndex(final ZCSite site) {
		Long lastTime = (Long) lastMap.get(site.getID() + "_Site");
		System.out.println(lastTime);
		System.out.println(System.currentTimeMillis() - 5000L);
		if ((lastTime != null) && (lastTime.longValue() > System.currentTimeMillis() - 5000L)) {
			return;
		}
		lastMap.put(site.getID() + "_Site", Long.valueOf(System.currentTimeMillis()));
		String ptStr = PublishPlatform.getValue(site.getConfigProps());
		String[] ptArr = StringUtil.splitEx(ptStr, ",");
		for (String ptID : ptArr) {
			final IPublishPlatform pt = (IPublishPlatform) PublishPlatformService.getInstance().get(ptID);
			if (pt != null) {
				PublishTask task = new PublishTask("Site", site.getID() + "," + ptID) {
					public void execute() {
						pt.publishSiteIndex(site);
					}
				};
				task.setDescription(site.getName());
				PublishTheadPool.getInstance().addTask(task);
			}
		}
	}

	public static void publishSiteIndex(ZCSite site, String platformID) {
		String siteRoot = PublishPlatformUtil.getPublishPlatformRoot(site.getID(), platformID);
		String fileName = siteRoot + "index" + PublishPlatformUtil.getSuffix(site.getID(), platformID);
		String indexTemplate = PublishPlatformUtil.getSiteIndexTemplate(site.getID(), platformID);
		if (ObjectUtil.empty(indexTemplate)) {
			message(Lang.get("Contentcore.NoSetSiteIndexTemplate", new Object[0]) + Lang.get("Contentcore.SiteName", new Object[0]) + ":" + site.getName(), platformID);
			return;
		}
		ITemplateType tt = ContentCorePlugin.getStaticizeContext().getTemplateType("SiteIndex");
		indexTemplate = siteRoot + indexTemplate;
		if (!FileUtil.exists(indexTemplate)) {
			message(Lang.get("Contentcore.TemplateNotFount", new Object[0]) + site.getName() + ":" + indexTemplate, platformID);
			return;
		}
		File f = new File(siteRoot);
		if (!f.exists()) {
			f.mkdirs();
		}
		try {
			AbstractExecuteContext context = tt.getContext(site.getID(), platformID, false);

			context = PublishPlatformBL.dealPlatformProperty(context, 0L, site.getID(), platformID);

			TemplateInstance tpl = ContentCorePlugin.getStaticizeContext().getTemplateManager().find(indexTemplate);
			tpl.setContext(context);

			TemplateWriter writer = new TemplateWriter();
			tpl.setWriter(writer);

			tpl.execute();
			FileUtil.writeText(fileName, writer.getResult());
			LogUtil.info("首页发布完成：" + site.getName());
		} catch (Exception e) {
			e.printStackTrace();
			PublishLogBL.addSiteIndexPublishLog(site, platformID, e.getMessage());
			Errorx.addError("Publish site index failed:" + e.getMessage());
		}
	}

	public static void publishCatalogIndexAndList(ZCCatalog catalog, boolean afterContentChange, String platformID) {
		Long lastTime = (Long) lastMap.get(catalog.getID() + "_Catalog" + platformID);
		if ((lastTime != null) && (lastTime.longValue() > System.currentTimeMillis() - 5000L)) {
			return;
		}
		lastMap.put(catalog.getID() + "_Catalog" + platformID, Long.valueOf(System.currentTimeMillis()));
		if (StringUtil.isEmpty(platformID)) {
			platformID = "pc";
		}
		if (catalog.getVisibleFlag().equals("N")) {
			return;
		}
		if (!YesOrNo.isYes(catalog.getGenerateFlag())) {
			return;
		}
		String indexTemplate = "";
		String listTemplate = "";
		if ("pc".equals(platformID)) {
			indexTemplate = catalog.getIndexTemplate();
			listTemplate = catalog.getListTemplate();
		} else {
			ZCPlatformProperty t = PublishPlatformUtil.getPlatformProperty(catalog.getSiteID(), platformID, catalog.getID());
			if (t != null) {
				indexTemplate = t.getIndexTemplate();
				listTemplate = t.getListTemplate();
			}
		}
		boolean noIndexTemplate = ObjectUtil.empty(indexTemplate);
		if (ObjectUtil.empty(listTemplate)) {
			ZCDefaultTemplate t = SiteUtil.getDefaultTemplate(catalog.getSiteID(), platformID, catalog.getContentType());
			if (t != null) {
				listTemplate = t.getListTemplate();
			}
		}
		if ((noIndexTemplate) && (ObjectUtil.empty(listTemplate))) {
			message(Lang.get("Contentcore.NoSetIndexOrListTemplate", new Object[0]) + Lang.get("Contentcore.Catalog.Name", new Object[0]) + ":" + catalog.getName(), platformID);
			return;
		}
		String siteRoot = PublishPlatformUtil.getPublishPlatformRoot(catalog.getSiteID(), platformID);
		if ((!noIndexTemplate) && (!FileUtil.exists(siteRoot + indexTemplate))) {
			message(Lang.get("Contentcore.TemplateNotFount", new Object[0]) + catalog.getName() + ":" + siteRoot + indexTemplate, platformID);
			return;
		}
		IContentType contentType = ContentTypeService.getContentType(catalog.getContentType(), catalog.getSiteID());
		if (ObjectUtil.empty(contentType)) {
			message("Content type '" + contentType + "' has not registed!", platformID);
			return;
		}
		ITemplateType tt = ContentCorePlugin.getStaticizeContext().getTemplateType(contentType.getListTemplateTypeID());

		String path = CatalogUtil.getFullPath(catalog.getID());
		File f = new File(siteRoot + path);
		if (!f.exists()) {
			f.mkdirs();
		}
		AbstractExecuteContext context = tt.getContext(catalog.getID(), platformID, false);
		String fileName = siteRoot + path + "index" + PublishPlatformUtil.getSuffix(catalog.getSiteID(), platformID);

		context = PublishPlatformBL.dealPlatformProperty(context, catalog.getID(), catalog.getSiteID(), platformID);

		String staticFileType = PublishPlatformBL.getStaticFileType(catalog.getSiteID(), platformID);
		context.addDataVariable(CmsPagebarTag.FirstFileNameVar, noIndexTemplate ? "index" + staticFileType : "list" + staticFileType);
		context.addDataVariable(CmsPagebarTag.OtherFileNameVar, noIndexTemplate ? "index_${PageIndex}" + staticFileType
				: "list_${PageIndex}" + staticFileType);
		try {
			if (!noIndexTemplate) {
				indexTemplate = siteRoot + indexTemplate;
				TemplateInstance tpl = ContentCorePlugin.getStaticizeContext().getTemplateManager().find(indexTemplate);
				tpl.setContext(context);

				TemplateWriter writer = new TemplateWriter();
				tpl.setWriter(writer);
				tpl.execute();
				FileUtil.writeText(fileName, writer.getResult());
				fileName = siteRoot + path + "list" + PublishPlatformUtil.getSuffix(catalog.getSiteID(), platformID);
			}
			if ((StringUtil.isNotEmpty(listTemplate)) && (FileUtil.exists(siteRoot + listTemplate))) {
				listTemplate = siteRoot + listTemplate;
				TemplateInstance tpl = ContentCorePlugin.getStaticizeContext().getTemplateManager().find(listTemplate);
				tpl.setContext(context);

				TemplateWriter writer = new TemplateWriter();
				tpl.setWriter(writer);
				tpl.execute();
				FileUtil.writeText(fileName, writer.getResult());

				String pageFileNamePrefix = fileName.substring(0, fileName.lastIndexOf(".")) + "_";
				while ((catalog.getListMaxPage() <= 0L) || (tpl.getPageIndex() < catalog.getListMaxPage())) {
					if (tpl.getPageIndex() >= 2147483647) {
						break;
					}
					if ((afterContentChange) && (tpl.getPageIndex() >= MaxListPublishPageWhenContentChange.getValue())) {
						break;
					}
					if (!tpl.hasNextPage()) {
						break;
					}
					tpl.setPageIndex(tpl.getPageIndex() + 1);
					if ((catalog.getListMaxPage() <= 0L) && (MaxListPublishPage.getValue() > 0) && (tpl.getPageIndex() > MaxListPublishPage.getValue())) {
						break;
					}
					tpl.setContext(context);
					writer = new TemplateWriter();
					tpl.setWriter(writer);
					tpl.execute();
					String pageFileName = pageFileNamePrefix + (tpl.getPageIndex() + 1) + PublishPlatformUtil.getSuffix(catalog.getSiteID(), platformID);
					FileUtil.writeText(pageFileName, writer.getResult());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			PublishLogBL.addCatalogPublishLog(catalog, platformID, e.getMessage());
			Errorx.addError("Publish catalog index/list failed:" + e.getMessage());
		}
	}

	public static void publishContent(IContent c) {
		if (!c.isDataLoaded()) {
			Errorx.addError("Content data not loaded!");
			return;
		}
		ZCContent dao = c.getContentDAO();
		if (ObjectUtil.empty(dao.getPublishDate())) {
			dao.setPublishDate(DateUtil.getCurrentDateTime());
		} else if (new Date().before(c.getPublishDate())) {
			Errorx.addError(Lang.get("Content.CanNotPublishBeforePublishTime", new Object[0]) + "," + Lang.get("Contentcore.ContentType", new Object[0]) + "=" + LangUtil.get(ContentTypeService.getContentType(c.getContentTypeID()).getExtendItemName()) + ":" + c.getTitle());
			return;
		}
		String flag = LastDetailTemplateFlag.generateFlag(c);
		String oldFlag = LastDetailTemplateFlag.getValue(dao.getConfigProps());
		if (!ObjectUtil.equal(flag, oldFlag)) {
			String props = PropertyUtil.setValue(dao.getConfigProps(), "LastTemplateFlag", flag);
			dao.setConfigProps(props);
			c.setProp("_TemplateChangedFlag", "Y");
		}
		long status = dao.getStatus();
		if (status != 30L) {
			dao.setModifyTime(new Date());
			dao.setStatus(30L);
			if (!dao.update()) {
				Errorx.addError(Lang.get("Content.UpdatePublishStatusFailed", new Object[0]));
				return;
			}
			if (status != 30L) {
				EventTypeService.put("com.zving.cms.content.Publish", dao);
			}
		} else if (!ObjectUtil.equal(flag, oldFlag)) {
			dao.update();
		}
		ZCCatalog catalog = CatalogUtil.getDAO(c.getCatalogID());
		if (!YesOrNo.isYes(catalog.getGenerateFlag())) {
			return;
		}
		ZCSite site = SiteUtil.getDAO(dao.getSiteID());
		if (site == null) {
			LogUtil.warn("ContentBL.publish() failed:Content " + dao.getID() + "'s SiteID not found:" + dao.getSiteID());
			return;
		}
		for (IPublishPlatform pt : PublishPlatformService.getUsedPublishPlatform(dao)) {
			c.setProp("_PageIndex", Integer.valueOf(0));
			pt.publishContent(c);
		}
	}

	public static void publishContent(IContent c, String platformID) {
		if (StringUtil.isEmpty(platformID)) {
			platformID = "pc";
		}
		int pageIndex = c.getProperties().getInt("_PageIndex");
		long catalogID = c.getCatalogID();
		ZCContent dao = c.getContentDAO();
		ZCCatalog catalog = CatalogUtil.getDAO(catalogID);
		String templateFile = PublishPlatformUtil.getCatalogDetailTemplate(catalogID, platformID);
		if (PCPublishPlatform.ID.equals(platformID)) {
			if (ObjectUtil.equal(c.getField("TemplateFlag"), "Y")) {
				templateFile = dao.getTemplate();
			}
		} else {
			if (PlatformContentTemplateFlag.templateFlag(dao.getConfigProps(), platformID)) {
				templateFile = PlatformContentTemplate.getTemplate(dao.getConfigProps(), platformID);
			}
		}
		
		if (ObjectUtil.empty(templateFile)) {
			// 没有设置详细页模板不提示
			//message(Lang.get("Contentcore.NoDetailTemplateSet", new Object[0]) + "标题" + ":" + c.getTitle(), platformID);
			return;
		}
		
		if (ObjectUtil.empty(templateFile)) {
			message(Lang.get("Contentcore.NoDetailTemplateSet", new Object[0]) + Lang.get("Contentcore.Catalog.Name", new Object[0]) + ":" + catalog.getName(), platformID);
			return;
		}
		String siteRoot = PublishPlatformUtil.getPublishPlatformRoot(catalog.getSiteID(), platformID);
		String fullTemplateFile = siteRoot + templateFile;
		if (!FileUtil.exists(fullTemplateFile)) {
			message(Lang.get("Contentcore.TemplateNotFount", new Object[0]) + catalog.getName() + ">" + c.getTitle() + ":" + siteRoot + templateFile, platformID);
			return;
		}
		String path = DetailNameRule.getContentPath(dao, pageIndex, platformID);
		File f = new File(siteRoot + path);
		if (!f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		if (AlwaysGenerateFile.getValue(catalog.getConfigProps())) {
			long contentLastModified = dao.getModifyTime() == null ? dao.getAddTime().getTime() : dao.getModifyTime().getTime();
			File detailTemplateFile = new File(fullTemplateFile);
			boolean templateChangedFlag = YesOrNo.isYes((String) c.getProp("_TemplateChangedFlag"));
			if ((!templateChangedFlag) && (f.exists()) && (TemplateUtil.checkShtml(catalog.getSiteID()))) {
				boolean publishPointFlag = false;
				for (ICoreURLModifier um : CoreURLModifierService.getInstance().getAll()) {
					if (!um.pass(catalog)) {
						publishPointFlag = true;
						break;
					}
				}
				if ((!publishPointFlag) && (f.lastModified() > contentLastModified) && (f.lastModified() > detailTemplateFile.lastModified())) {
					return;
				}
			}
		}
		try {
			IContentType ct = ContentTypeService.getContentType(c.getContentTypeID(), c.getSiteID());
			ITemplateType tt = ContentCorePlugin.getStaticizeContext().getTemplateType(ct.getDetailTemplateTypeID());
			AbstractExecuteContext context = null;
			if (tt instanceof AbstractDetailTemplate) {
				AbstractDetailTemplate adt = (AbstractDetailTemplate) tt;
				context = adt.getContext(c, platformID, false);
				adt.beforeConentPublish(dao, context);
			} else {
				context = tt.getContext(c.getID(), platformID, false);
			}
			context.addDataVariable("PlatformID", platformID);

			context = PublishPlatformBL.dealPlatformProperty(context, catalog.getID(), catalog.getSiteID(), platformID);
			TemplateInstance tpl = ContentCorePlugin.getStaticizeContext().getTemplateManager().find(fullTemplateFile);
			tpl.setContext(context);

			TemplateWriter writer = new TemplateWriter();
			tpl.setWriter(writer);
			tpl.execute();
			FileUtil.writeText(f.getAbsolutePath(), writer.getResult());

			int pagesize = context.evalInt("Content.ContentPageSize");
			if (pagesize == 1) {
				path = f.getAbsolutePath();
				String prefix = path.substring(0, path.lastIndexOf(".")) + "_";
				String suffix = path.substring(path.lastIndexOf("."));
				for (int i = 2; i < 100; i++) {
					File pageFile = new File(prefix + i + suffix);
					if (!pageFile.exists()) {
						break;
					}
					FileUtil.delete(pageFile);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			PublishLogBL.addContentPublishLog(c, platformID, e.getMessage());
			Errorx.addError("Publish content failed:" + e.getMessage());
		}
	}

	public static int publishCatalogContents(ZCCatalog catalog, int status, LongTimeTask task) {
		IContentType contentType = ContentTypeService.getContentType(catalog.getContentType(), catalog.getSiteID());
		if (ObjectUtil.empty(contentType)) {
			LogUtil.warn("Content type '" + catalog.getContentType() + "' has not registed!");
			return 0;
		}
		Q wherePart = new Q("where CatalogID=?", new Object[] { Long.valueOf(catalog.getID()) });
		if (status < 0) {
			wherePart.append(" and Status<>?", new Object[] { Integer.valueOf(10) });
		} else if (20 == status) {
			wherePart.append(" and Status=? and (PublishDate<? or PublishDate is null)", new Object[] {
					Integer.valueOf(20), new Date() });
		} else {
			wherePart.append(" and Status=?", new Object[] { Integer.valueOf(status) });
		}
		int total = contentType.getCount(wherePart);
		int size = 100;
		for (int i = 0; i * size < total; i++) {
			List<IContent> contents = contentType.loadContents(size, status == 30 ? i : 0, wherePart);
			PrevAndNextBL.dealContentList(contents, status, i);
			Mapx<Long, Long> oldStatusMap = new Mapx();
			int j = 0;
			for (IContent content : contents) {
				Transaction trans = new Transaction();
				content.setTransaction(trans);
				oldStatusMap.put(Long.valueOf(content.getID()), Long.valueOf(content.getStatus()));
				content.publish();
				if (trans.commit()) {
					if (((Long) oldStatusMap.get(Long.valueOf(content.getID()))).longValue() != 30L) {
						ContentBL.createContentIndex(content);
						ExtendManager.invoke("com.zving.contentcore.AfterContentPublish", new Object[] { content });
					}
				} else {
					Errorx.addError(trans.getExceptionMessage());
				}
				if (task != null) {
					task.setPercent((i * size + j) * 100 / total);
					task.setCurrentInfo(catalog.getName() + "(" + (i * size + j + 1) + "/" + total + ")：" + content.getTitle());
				}
				j++;
			}
		}
		return total;
	}

	public static void publishContentRelative(IContent content) {
		IContentType ct = ContentTypeService.getContentType(content.getContentTypeID());
		if (ct != null) {
			int pageSize = 100;
			Q wherePart = new Q("where CopyType>? and CopyID=?", new Object[] {
					Integer.valueOf(1), Long.valueOf(content.getID()) });
			int count = ct.getCount(wherePart);
			for (int i = 0; i * pageSize < count; i++) {
				List<IContent> copyList = ct.loadContents(pageSize, i, wherePart);
				if ((copyList == null) || (copyList.size() == 0)) {
					break;
				}
				for (IContent copyContent : copyList) {
					copyContent.publish();
					publishContentRelative(copyContent);
				}
				if (Errorx.hasError()) {
					LogUtil.warn("发布引用内容失败：" + Errorx.getAllMessage());
				} else {
					LogUtil.info("发布引用内容成功");
				}
			}
		}
		long catalogID = content.getCatalogID();
		long siteID = content.getSiteID();

		ZCCatalog catalog = CatalogUtil.getDAO(catalogID);
		if (catalog != null) {
			while (true) {
				ICatalogType catalogType = CatalogUtil.getCatalogType(catalogID);
				catalogType.publish(catalog, true);
				if ((catalog.getParentID() == 0L) || (!CacheManager.contains("ContentCoreCache", "Catalog", Long.valueOf(catalog.getParentID())))) {
					break;
				}
				catalog = CatalogUtil.getDAO(catalog.getParentID());
				if (ObjectUtil.empty(catalog)) {
					break;
				}
				catalogID = catalog.getID();
			}
		}
		publishSiteIndex(SiteUtil.getDAO(siteID));
	}

	public static void message(String msg, String platFormID) {
		if (Errorx.printString().contains(platFormID + " " + msg)) {
			return;
		}
		LogUtil.warn(platFormID + " " + msg);
		Errorx.addError(platFormID + " " + msg);
	}
}

/*
 * Location:
 * D:\project\cms\UI\WEB-INF\plugins\lib\com.zving.contentcore.plugin.jar
 * Qualified Name: com.zving.contentcore.bl.PublishBL JD-Core Version: 0.7.0.1
 */