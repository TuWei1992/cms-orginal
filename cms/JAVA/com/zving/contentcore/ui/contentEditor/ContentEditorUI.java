package com.zving.contentcore.ui.contentEditor;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.zving.contentcore.IContent;
import com.zving.contentcore.IContentType;
import com.zving.contentcore.bl.ContentBL;
import com.zving.contentcore.bl.ContentLogBL;
import com.zving.contentcore.bl.PrevAndNextBL;
import com.zving.contentcore.bl.SiteBL;
import com.zving.contentcore.code.ContentStatus;
import com.zving.contentcore.property.PropertyService;
import com.zving.contentcore.property.PropertyUtil;
import com.zving.contentcore.resource.ResourceRelaBL;
import com.zving.contentcore.resource.ResourceUtil;
import com.zving.contentcore.service.ContentTypeService;
import com.zving.contentcore.util.CatalogUtil;
import com.zving.contentcore.util.ContentMaxNo;
import com.zving.contentcore.util.ContentUtil;
import com.zving.contentcore.util.SiteUtil;
import com.zving.contentcore.util.ZImageUtil;
import com.zving.framework.Current;
import com.zving.framework.UIFacade;
import com.zving.framework.User;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.annotation.Verify;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.i18n.Lang;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.security.PrivCheck;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.Errorx;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.code.YesOrNo;
import com.zving.platform.service.EventTypeService;
import com.zving.platform.service.UserPreferencesService;
import com.zving.schema.ZCCatalog;
import com.zving.schema.ZCContent;
import com.zving.schema.ZCResources;
import com.zving.schema.ZCSite;
import com.zving.search.index.IndexUtil;

@Alias("Content")
public class ContentEditorUI extends UIFacade {
	@Priv
	public void init() {
		long catalogID = $L("CatalogID");
		long contentID = $L("ContentID");

		$S("StatusName", "");
		$S("LogoSrc", "../platform/images/addpicture.png");
		if (contentID > 0L) {
			ZCContent dao = new ZCContent();
			dao.setID(contentID);
			if (!(dao.fill())) {
				return;
			}
			long topFlag = dao.getTopFlag();
			Date date = null;
			if (ObjectUtil.notEmpty(dao.getTopDate())) {
				date = dao.getTopDate();
			}
			int platformAttribute = dao.getPlatformAttribute();
			if ((dao.getCopyType() == 2) || (dao.getCopyType() == 3)) {
				long sourceID = dao.getCopyID();
				dao = new ZCContent();
				dao.setID(sourceID);
				if (!(dao.fill())) {
					return;
				}
				dao.setTopFlag(topFlag);
				if (date != null) {
					dao.setTopDate(date);
				}

				dao.setPlatformAttribute(platformAttribute);
				$S("CopyContentID", Long.valueOf(dao.getID()));
				$S("CopyCatalogID", Long.valueOf(dao.getCatalogID()));
			}
			IContentType ct = ContentTypeService.getContentType(dao.getContentTypeID());
			IContent content = ct.loadContent(dao, null);

			Mapx map = content.getValues();
			catalogID = dao.getCatalogID();
			map.put("Title", dao.getTitle());

			map.put("Attribute", ContentUtil.attributeToString(dao.getAttribute()));
			map.put("PlatformAttribute", ContentUtil.platformAttributeToString(dao.getPlatformAttribute()));
			map.put("StatusName", LangUtil.get(ContentStatus.getStatusName((int) dao.getStatus())));

			map.put("ConfigProps", StringUtil.replaceEx(dao.getConfigProps(), "\n", "__ZVING__"));
			if (ObjectUtil.notEmpty(dao.getLogoFile())) {
				if (dao.getLogoFile().toLowerCase().startsWith("http")) {
					map.put("LogoSrc", dao.getLogoFile());
				} else {
					map.put("LogoSrc", "../preview/" + SiteUtil.getDAO(dao.getSiteID()).getPath() + "/" + ZImageUtil.getThumbnailPath(dao.getLogoFile(), 120, 120) + "?t=" + System.currentTimeMillis());
					ZCResources res = ResourceUtil.getDAOByPath(dao.getLogoFile());
					if (ObjectUtil.notEmpty(res)) {
						map.put("ResourceID", Long.valueOf(res.getID()));
					}
				}

			}

			if (StringUtil.isNotNull(dao.getStaticFileName())) {
				map.put("StaticFileNameFlag", "Y");
			}

			Date lastModify = (dao.getModifyTime() == null) ? dao.getAddTime() : dao.getModifyTime();
			map.put("LastModify", DateUtil.toString(lastModify, "yyyy-MM-dd HH:mm:ss"));
			if (ObjectUtil.notEmpty(dao.getPublishDate())) {
				map.put("PublishDate", DateUtil.toString(dao.getPublishDate(), "yyyy-MM-dd HH:mm:ss"));
			}
			map.put("DownlineDate", DateUtil.toString(dao.getDownlineDate(), "yyyy-MM-dd HH:mm:ss"));
			map.put("Method", "UPDATE");

			if ((ObjectUtil.notEmpty(Long.valueOf(dao.getCopyID()))) && (80L != dao.getStatus())) {
				map.put("CopySourceCatalogName", ContentBL.getSourceCatalogName(dao.getSiteID(), dao.getCopyID()));
			}

			Mapx<Integer, List<String>> mapx = ContentBL.getCopyToCatalogNames(dao.getID());
			if ((mapx != null) && (mapx.size() > 0)) {
				for (Integer key : mapx.keySet()) {
					if (key.intValue() == 1)
						map.put("IndepentedCatalogNames", mapx.get(key));
					else if (key.intValue() == 2)
						map.put("MappedCatalogNames", mapx.get(key));
					else if (key.intValue() == 3) {
						map.put("LinkedCatalogNames", mapx.get(key));
					}
				}
			}
			if (YesOrNo.isYes(dao.getLinkFlag())) {
				map.put("CName", ContentUtil.getRedirectURLName(dao.getRedirectURL()));
			}

			if ((StringUtil.isNotEmpty(dao.getTag())) && (dao.getTag().length() > 2) && (dao.getTag().startsWith(",")) && (dao.getTag().endsWith(","))) {
				map.put("Tag", dao.getTag().substring(1, dao.getTag().length() - 1));
			}

			map.put("ContentType", dao.getContentTypeID());
			this.Response.putAll(map);

			$S("ContentStatus", Long.valueOf(dao.getStatus()));
			ExtendManager.invoke("com.zving.contentcore.BeforeContentInit", new Object[] { this.Response });

			String link = ContentUtil.getPublishedURL(SiteUtil.getURL(dao.getSiteID()), dao, "pc");
			$S("Link", link);
		} else {
			if (StringUtil.isNotNull($V("ContentType"))) {
				IContentType ct = ContentTypeService.getContentType($V("ContentType"));
				Mapx map = new Mapx();
				PropertyService.getInstance().addContentDefaultValues(map, ct.getExtendItemID());
				this.Response.putAll(map);
			}
			long siteID = CatalogUtil.getSiteID(catalogID);
			$S("CatalogID", Long.valueOf(catalogID));
			$S("ContentID", Long.valueOf(ContentMaxNo.getMaxID()));
			$S("Method", "ADD");

			$S("DownlineDate", "2099-12-31");
			$S("DownlineTime", "23:59:59");

			$S("Pages", new Integer(1));
			$S("ContentPages", "''");
			$S("ReferTarget", "");
			$S("ReferType", "1");
			$S("PlatformAttribute", SiteUtil.getPropertyValue(siteID, "PublishPlatform"));

			$S("Source", SiteUtil.getName(siteID));

			if (YesOrNo.isYes((String) User.getValue("AutoFillContentEditor"))) {
				$S("Editor", LangUtil.decode(User.getRealName()));
			}
		}
		ZCCatalog c = CatalogUtil.getDAO(catalogID);
		String contentType = c.getContentType();
		$S("ContentType", contentType);

		IContentType ct = ContentTypeService.getContentType(contentType);
		$S("DetailTemplateType", ct.getDetailTemplateTypeID());
		$S("CatalogPath", c.getPath());
		$S("SiteID", Long.valueOf(c.getSiteID()));
		$S("SitePath", SiteUtil.getDAO(c.getSiteID()).getPath());
		$S("CatalogName", c.getName());
		$S("InnerCode", c.getInnerCode());
		$S("PreviewPrefix", SiteUtil.getPreviewPrefix(c.getSiteID()));
		$S("UserName", User.getUserName());

		String showImageName = UserPreferencesService.getUerPreferences(User.getUserName()).getString("ShowImageName");
		$S("ShowImageName", showImageName);
		boolean hasCatalogEditPriv = PrivCheck.check("com.zving.cms.Catalog.Content.Add." + catalogID + "||" + "com.zving.cms.Catalog.Content.Edit" + "." + catalogID);
		$S("HasEditPriv", Boolean.valueOf(hasCatalogEditPriv));
	}

	@Priv("com.zving.cms.Catalog.Content.Add.${CatalogID}||com.zving.cms.Catalog.Content.Edit.${CatalogID}")
	@Verify
	public void save() {
		long contentID = $L("ContentID");
		if ((contentID == 0L) && (!("ADD".equals($V("Method"))))) {
			fail(Lang.get("Common.InvalidID", new Object[0]));
			return;
		}

		String _tags = $V("Tag");
		if (StringUtil.isNotEmpty(_tags)) {
			String[] tags = _tags.split(",|\\s");
			HashSet set = new HashSet();
			for (String tag : tags) {
				set.add(tag);
			}
			this.Request.put("Tag", StringUtil.join(set, ","));
		}
		long catalogID = $L("CatalogID");
		int count = ContentBL.sameTitleCount($V("Title"), catalogID, SiteBL.getCurrentSite(), contentID);
		if (count > 0) {
			ZCSite site = SiteUtil.getDAO(SiteBL.getCurrentSite());
			String titleCheckType = PropertyUtil.getValue(site.getConfigProps(), "ContentTitleCheckType");
			if ("Catalog".equals(titleCheckType))
				fail(Lang.get("Contentcore.Title.Notice", new Object[0]));
			else {
				fail(Lang.get("Contentcore.Title.SiteNotice", new Object[0]));
			}
			return;
		}
		
		//same StaticFileName test
		String staticFileName = $V("StaticFileName");
		if (StringUtil.isNotEmpty(staticFileName)) {
			 Q q = new Q("select count(1) from ZCContent where StaticFileName=? and CatalogID=?", new Object[] { staticFileName, catalogID });
			 if (contentID > 0L) {
				 q.and().ne("ID", Long.valueOf(contentID));
			 }
			 if (q.executeInt() > 0) {
				 fail("该栏目下已有其他内容设置“自定义静态文件名称”为：" + staticFileName);
				 return;
			 }
		}
		ZCCatalog catalog = CatalogUtil.getDAO(catalogID);
		IContentType ct = ContentTypeService.getContentType(catalog.getContentType());
		IContent content = null;

		this.Request.put("ID", Long.valueOf(contentID));
		if ("ADD".equals($V("Method"))) {
			content = ct.newContent();
			content.setFields(this.Request);
			content.getProperties().putAll(this.Request);
			content.setOperator(User.getUserName());
			content.setTransaction(Current.getTransaction());
			contentID = content.insert();
		} else {
			ZCContent c = new ZCContent();
			c.setID(contentID);
			content = contentSave(true, ct, c);

			if (content == null) {
				fail("Content ID not exists:" + contentID);
				return;
			}
			c = content.getContentDAO();

			PrevAndNextBL.computePrevAndNextID(c);

			Q copyQuery = new Q("select ID from ZCcontent where ContentTypeID=? and (CopyType=? or CopyType=?)", new Object[] {
					content.getContentTypeID(), Integer.valueOf(2),
					Integer.valueOf(3) });
			long copyID = c.getCopyID();

			if ((copyID > 0L) && (c.getCopyType() > 1))
				copyQuery.and().eq("CopyID", Long.valueOf(copyID));
			else {
				copyQuery.and().eq("CopyID", Long.valueOf(contentID));
			}
			copyQuery.and().ne("ID", Long.valueOf(contentID));
			Object[] IDs = copyQuery.executeDataTable().getColumnValues("ID");
			for (Object id : IDs) {
				ZCContent con = new ZCContent();
				con.setID(String.valueOf(id));
				contentSave(true, ct, con);
			}

			if ((c.getCopyType() == 2) || (c.getCopyType() == 3)) {
				Long sourceID = Long.valueOf(c.getCopyID());
				c = new ZCContent();
				c.setID(sourceID.longValue());
				contentSave(true, ct, c);
			}

		}

		ExtendManager.invoke("com.zving.contentcore.BeforeContentSave", new Object[] {
				Current.getTransaction(), content });
		if (Errorx.hasError()) {
			fail(Errorx.getAllMessage());
			if ("ADD".equals($V("Method")))
				ContentLogBL.fail(content, "add", Errorx.getAllMessage());
			else {
				ContentLogBL.fail(content, "edit", Errorx.getAllMessage());
			}
			return;
		}
		if (Current.getTransaction().commit()) {
			ExtendManager.invoke("com.zving.contentcore.AfterContentSave", new Object[] { content });
			EventTypeService.put("com.zving.cms.content.Save", content);
			success(Lang.get("Common.SaveSuccess", new Object[0]));
			if (!("ADD".equals($V("Method")))) {
				ContentLogBL.success(content, "edit", "@{Common.ExecuteSuccess}");
			} else if (contentID == 0L)
				ContentLogBL.fail(content, "add", "@{Common.ExecuteFailed}");
			else {
				ContentLogBL.success(content, "add", "@{Common.ExecuteSuccess}");
			}

			$S("Keyword", content.getContentDAO().getKeyword());
			$S("ContentID", Long.valueOf(contentID));
		} else {
			String actionDetail = Current.getTransaction().getExceptionMessage();
			Transaction tran = new Transaction();
			if ("ADD".equals($V("Method")))
				ContentLogBL.fail(content, "add", actionDetail);
			else {
				ContentLogBL.fail(content, "edit", actionDetail);
			}
			tran.commit();

			fail(Lang.get("Common.ExecuteFailed", new Object[0]) + ":" + Current.getTransaction().getExceptionMessage());
		}
	}

	private IContent contentSave(boolean isCopy, IContentType ct, ZCContent dao) {
		IContent content = null;
		if (!(dao.fill())) {
			return null;
		}

		content = ct.loadContent(dao, null);
		if (isCopy) {
			this.Request.put("CatalogID", Long.valueOf(dao.getCatalogID()));
			this.Request.put("ID", Long.valueOf(content.getID()));
			this.Request.put("SiteID", Long.valueOf(content.getSiteID()));
		}
		String oldLogoFilePath = content.getLogoFile();
		if ((StringUtil.isNotNull(oldLogoFilePath)) && (oldLogoFilePath.startsWith("/"))) {
			oldLogoFilePath = oldLogoFilePath.substring(1);
		}
		long resourceID = $L("ResourceID");
		if ((StringUtil.isNotNull(oldLogoFilePath)) && (StringUtil.isNull(this.Request.getString("LogoFile")))) {
			ZCResources r = ResourceUtil.getDAOByPath(oldLogoFilePath);
			if (r != null)
				ResourceRelaBL.delRelationship(content.getID(), content.getContentTypeID(), r.getID(), Current.getTransaction());
		} else if ((StringUtil.isNotNull(oldLogoFilePath)) && (StringUtil.isNotNull(this.Request.getString("LogoFile"))) && (!(oldLogoFilePath.equals(this.Request.getString("LogoFile")))) && (resourceID != 0L)) {
			ZCResources r = ResourceUtil.getDAOByPath(oldLogoFilePath);
			ResourceRelaBL.delRelationship(content.getID(), content.getContentTypeID(), r.getID(), Current.getTransaction());
			ResourceRelaBL.addRelationship(content.getID(), content.getContentTypeID(), resourceID, Current.getTransaction());
		} else if ((StringUtil.isNull(oldLogoFilePath)) && (StringUtil.isNotNull(this.Request.getString("LogoFile"))) && (resourceID != 0L)) {
			ResourceRelaBL.addRelationship(content.getID(), content.getContentTypeID(), resourceID, Current.getTransaction());
		}
		content.setFields(this.Request);
		Mapx props = content.getProperties();
		props.putAll(this.Request);
		if (this.Request.containsKey("Title")) {
			props.put("Attribute", this.Request.getString("Attribute"));
			props.put("PlatformAttribute", this.Request.getString("PlatformAttribute"));
		}
		content.setOperator(User.getUserName());

		content.setTransaction(Current.getTransaction());
		content.save();
		return content;
	}

	@Priv
	public void verifySameTitle() {
		long catalogID = $L("CatalogID");
		long contentID = $L("ContentID");
		int count = ContentBL.sameTitleCount($V("Title"), catalogID, SiteBL.getCurrentSite(), contentID);
		if (count > 0) {
			ZCSite site = SiteUtil.getDAO(SiteBL.getCurrentSite());
			String titleCheckType = PropertyUtil.getValue(site.getConfigProps(), "ContentTitleCheckType");
			if ("Catalog".equals(titleCheckType))
				$S("TitleCheckType", "Catalog");
			else {
				$S("TitleCheckType", "Site");
			}
		}
		this.Response.setStatus(count);
	}

	@Priv
	public void bindRelativeGrid(DataGridAction dga) {
		String relaIDs = $V("RelativeContent");
		if (StringUtil.isEmpty(relaIDs)) {
			if (!("true".equals($V("Delete")))) {
				dga.bindData(new DataTable());
				return;
			}
			relaIDs = new Q("select RelativeContent from ZCContent where ID=?", new Object[] { Long.valueOf($L("ContentID")) }).executeString();
			if (StringUtil.isEmpty(relaIDs)) {
				dga.bindData(new DataTable());
				return;
			}
		}

		Q qb = new Q().select(new String[] { "CatalogID", "Title", "ID",
				"Status", "Author", "PublishDate" }).from(new String[] { "ZCContent" }).where().in("ID", relaIDs).and().eq("Status", Integer.valueOf(30));
		qb.append(dga.getSortString(), new Object[0]);
		DataTable dt = qb.fetch();

		String[] ids = relaIDs.split("\\,");
		DataTable result = new DataTable(dt.getDataColumns(), null);
		for (String id : ids) {
			for (int j = 0; j < dt.getRowCount(); ++j) {
				if (dt.getString(j, "ID").equals(id)) {
					result.insertRow(dt.getDataRow(j));
					break;
				}
			}
		}
		result.insertColumn("CatalogName");
		for (DataRow dr : result) {
			ZCCatalog catalog = CatalogUtil.getDAO(dr.getLong("CatalogID"));
			if (ObjectUtil.notEmpty(catalog)) {
				dr.set("CatalogName", catalog.getName());
			}
		}
		dga.bindData(result);
	}

	@Priv
	public void getAutoRelaIDs() {
		long contentID = $L("ContentID");
		long catalogID = $L("CatalogID");
		long siteID = $L("SiteID");
		String keyword = $V("Keyword");
		if (StringUtil.isEmpty(keyword)) {
			String Title = $V("Title");
			keyword = StringUtil.join(IndexUtil.getKeyword(Title, ""), " ");
		}
		$S("IDs", ContentBL.getAutoRelaIDs(siteID, catalogID, contentID, keyword));
	}

	@Priv
	public void initRelativeContentDialog() {
		long contentID = $L("ContentID");
		if (contentID != 0L) {
			ZCContent content = new ZCContent();
			content.setID(contentID);
			content.fill();
			this.Request.put("RelativeContent", content.getRelativeContent());
			this.Response.putAll(content.toMapx());
		}
	}

	@Priv("com.zving.cms.Catalog.Content.Add.${CatalogID}||com.zving.cms.Catalog.Content.Edit.${CatalogID}")
	public void saveRelaContent() {
		long id = $L("ID");
		String relaIDs = $V("RelaIDs");
		if (id == 0L) {
			fail(Lang.get("Common.InvalidID", new Object[0]));
			return;
		}
		if (ObjectUtil.empty(relaIDs)) {
			relaIDs = "";
		}
		ZCContent content = new ZCContent();
		content.setID(id);
		content.fill();
		content.setRelativeContent(relaIDs);
		content.setModifyTime(new Date());
		content.setModifyUser(User.getUserName());
		if (content.update()) {
			$S("RelativeContent", relaIDs);
			success(Lang.get("Common.ExecuteSuccess", new Object[0]));
		} else {
			fail(Lang.get("Common.ExecuteFailed", new Object[0]));
		}
	}
}