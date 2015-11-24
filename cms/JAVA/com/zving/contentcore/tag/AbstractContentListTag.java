package com.zving.contentcore.tag;

import com.zving.contentcore.bl.ContentBL;
import com.zving.contentcore.bl.PrevAndNextBL;
import com.zving.contentcore.code.DetailNameRule;
import com.zving.contentcore.resource.ResourceBL;
import com.zving.contentcore.service.TagCatalogConditionProviderService;
import com.zving.contentcore.service.TemplateTypeService;
import com.zving.contentcore.template.types.AbstractCatalogListTemplate;
import com.zving.contentcore.util.CatalogUtil;
import com.zving.contentcore.util.ContentUtil;
import com.zving.contentcore.util.PublishPlatformUtil;
import com.zving.framework.collection.CaseIgnoreMapx;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DBUtil;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.i18n.Lang;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.TemplateExecutor;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.NumberUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.util.CodeCache;
import com.zving.schema.ZCContent;
import com.zving.schema.ZDCode;
import com.zving.staticize.tag.AbstractListTag;
import com.zving.staticize.template.ITemplateType;
import com.zving.staticize.template.TemplateConfig;
import java.util.Date;
import java.util.List;

public abstract class AbstractContentListTag extends AbstractListTag {
	protected String type;
	protected String hasAttribute;
	protected String platformAttribute;
	protected String noAttribute;
	protected int minWeight;
	protected int maxWeight;
	protected String keyword;
	protected String orderby;
	protected long siteID;
	protected boolean loadContent;
	protected boolean loadExtend;
	protected boolean hasLogo;
	protected String catalogID;
	protected String catalogAlias;
	protected String catalog;
	protected long id;
	protected String name;
	protected String level;
	protected boolean isLoadCatalogCondition = true;
	protected String publishtime;

	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = super.getTagAttrs();
		list.add(new TagAttr("loadExtend", false, 1, "@{Article.ArticleTag.LoadExtendUsage}"));
		list.add(new TagAttr("loadContent", false, 1, "@{Article.ArticleTag.LoadContentUsage}"));
		list.add(new TagAttr("hasLogo", false, 1, "@{Article.ArticleTag.HasLogoUsage}"));
		list.add(new TagAttr("siteID", false, 7, "@{Contentcore.SiteID}"));
		list.add(new TagAttr("catalogID", false, 1, "@{Article.ArticleTag.CatalogIDUsage}"));
		list.add(new TagAttr("catalogAlias", false, 1, "@{Article.ArticleTag.CatalogAliasUsage}"));
		list.add(new TagAttr("catalog", false, 1, "@{Article.ArticleTag.CatalogUsage}"));
		list.add(new TagAttr("hasAttribute", false, 1, "@{Article.ArticleTag.HasAttributeUsage}"));
		list.add(new TagAttr("platformAttribute", false, 1, "@{Contentcore.PublishPlatformList}"));
		list.add(new TagAttr("noAttribute", false, 1, "@{Article.ArticleTag.NoAttributeUsage}"));
		list.add(new TagAttr("minWeight", false, 8, "@{Block.MinWeight}"));
		list.add(new TagAttr("maxWeight", false, 8, "@{Block.MaxWeight}"));
		list.add(new TagAttr("keyword", false, 1, "@{Article.ArticleTag.KeywordUsage}"));
		list.add(new TagAttr("orderby", false, 1, "@{Contentcore.ContentListTag.Orderby}"));
		list.add(new TagAttr("flags", false, 1, "@{Article.ArticleTagAttributeDis}"));

		list.add(new TagAttr("name", false, 1, "@{Contentcore.Basic.Title}"));
		list.add(new TagAttr("id", false, 1, "@{Contentcore.ContentListTag.ID}"));

		list.add(new TagAttr("publishtime", false, 1, "@{Contentcore.ExportSpecificContent}"));
		TagAttr type = new TagAttr("type", false, 1, "@{Common.Type}");
		list.add(type);
		CaseIgnoreMapx<String, String> typeMap = new CaseIgnoreMapx();
		typeMap.put("Recent", "@{Article.ArticleTag.Type.RecentUsage}");
		typeMap.put("Hot", "@{Article.ArticleTag.Type.HotUsage}");
		type.setOptions(typeMap);
		TagAttr level = new TagAttr("level", false, 1, "@{Contentcore.CatalogTag.LevelUsage}");
		list.add(level);
		level.setOptions(CatalogUtil.LEVEL_OPTIONS);
		return list;
	}

	public abstract String getItemName();

	public abstract Q loadContentQueryBuilder();

	public abstract String getContentType();

	public abstract void invokeTagSqlExtend(AbstractExecuteContext paramAbstractExecuteContext, Q paramQ);

	public abstract void dealExtend();

	public abstract void dealContent();

	public abstract void dealLink(DataRow paramDataRow);

	public void prepareData() {
		this.item = getItemName();
		if (this.siteID == 0L) {
			this.siteID = this.context.evalLong("Site.ID");
		}
		this.catalogID = StringUtil.join(TagCatalogConditionProviderService.getCatalogID(this.context, this.catalogID, this.catalogAlias, this.catalog));
		Q q = null;
		if (this.loadContent) {
			q = loadContentQueryBuilder();
			if (q == null) {
				this.loadContent = false;
			}
		}
		if (!this.loadContent) {
			q = new Q("select a.* from ZCContent a where a.SiteID=?", new Object[] { Long.valueOf(this.siteID) });
		}
		if (this.minWeight > 0) {
			q.and().ge("a.Weight", Integer.valueOf(this.minWeight));
		}
		if (this.maxWeight > 0) {
			q.and().le("a.Weight", Integer.valueOf(this.maxWeight));
		}
		if (this.hasLogo) {
			q.and().ne("a.LogoFile", "");
		}
		q.and().eq("a.ContentTypeID", getContentType());
		q.and().eq("a.Status", Integer.valueOf(30));
		if (ObjectUtil.notEmpty(this.keyword)) {
			q.and().like("a.keyword", this.keyword);
		}
		if (ObjectUtil.notEmpty(this.condition)) {
			q.append(" and ", new Object[0]).append(this.condition, new Object[0]);
		}

		if (ObjectUtil.notEmpty(this.hasAttribute)) {
			String[] attr = this.hasAttribute.split("\\,");
			int attrTotal = 0;
			for (String string : attr) {
				string = StringUtil.capitalize(string);
				ZDCode code = CodeCache.get("ContentAttribute", string);
				if (code != null) {
					attrTotal += Integer.parseInt(code.getMemo());
				}
			}
			q.append(" and " + DBUtil.sqlBitAndFunction("a.attribute", attrTotal) + "=" + attrTotal, new Object[0]);
		}

		if (ObjectUtil.notEmpty(this.platformAttribute)) {
			String[] attr = this.platformAttribute.split("\\,");
			int attrTotal = 0;
			for (String string : attr) {
				if (!"pc".equals(string)) {
					string = StringUtil.capitalize(string);
				}
				int code = ContentUtil.platformAttributeToInt(string);
				if (code != 0) {
					attrTotal += code;
				}
			}
			if (attrTotal > 0) {
				q.append(" and " + DBUtil.sqlBitAndFunction("a.PlatformAttribute", attrTotal) + "=" + attrTotal, new Object[0]);
			}
		}
		if (ObjectUtil.notEmpty(this.noAttribute)) {
			String[] attr = this.noAttribute.split("\\,");
			int attrTotal = 0;

			for (String string : attr) {
				string = StringUtil.capitalize(string);
				ZDCode code = CodeCache.get("ContentAttribute", string);
				if ((ObjectUtil.notEmpty(code)) && (NumberUtil.isInt(code.getMemo()))) {
					attrTotal += Integer.parseInt(code.getMemo());
					q.append(" and " + DBUtil.sqlBitAndFunction("a.attribute", attrTotal) + "<>" + Integer.parseInt(code.getMemo()), new Object[0]);
				}
			}
		}
		if (ObjectUtil.notEmpty(this.condition)) {
			q.append(" and ", new Object[0]).append(this.condition, new Object[0]);
		}
		if (ObjectUtil.notEmpty(this.name)) {
			q.append(" and a.Title=?", new Object[] { this.name });
		}
		if (this.id != 0L) {
			q.append(" and a.ID=?", new Object[] { Long.valueOf(this.id) });
		}
		if (this.isLoadCatalogCondition) {
			TagCatalogConditionProviderService.prepareCatalog(q, this);
		}
		if (StringUtil.isNotEmpty(this.publishtime)) {
			Date beforeTime = null;
			Date afterTime = null;
			String[] time = StringUtil.splitEx(this.publishtime, ",");
			if (time.length > 1) {
				beforeTime = DateUtil.parseDateTime(time[0]);
				afterTime = DateUtil.addDay(DateUtil.parseDateTime(time[1]), 1);
			} else {
				afterTime = DateUtil.parseDateTime(DateUtil.getCurrentDateTime());
				int count = -Integer.valueOf(time[0].substring(0, 1)).intValue();
				if (time[0].endsWith("h")) {
					beforeTime = DateUtil.addHour(afterTime, count);
				} else if (time[0].endsWith("d")) {
					beforeTime = DateUtil.addDay(afterTime, count);
				} else if (time[0].endsWith("m")) {
					beforeTime = DateUtil.addMonth(afterTime, count);
				} else if (time[0].endsWith("y")) {
					beforeTime = DateUtil.addYear(afterTime, count);
				}
			}
			if (ObjectUtil.notEmpty(beforeTime)) {
				q.and().ge("PublishDate", beforeTime);
			}
			if (ObjectUtil.notEmpty(afterTime)) {
				q.and().lt("PublishDate", afterTime);
			}
		}
		invokeTagSqlExtend(this.context, q);
		if (ObjectUtil.empty(this.orderby)) {
			if ("Recent".equalsIgnoreCase(this.type)) {
				q.orderby("a.PublishDate desc");
			} else if ("Hot".equalsIgnoreCase(this.type)) {
				q.orderby("a.Hitcount desc");
			} else {
				q.orderby("a.OrderFlag desc");
			}
		} else {
			q.orderby(this.orderby);
		}
		if (this.page) {
			this.pageTotal = DBUtil.getCount(q);
			this.context.setPageTotal(this.pageTotal);
			this.data = q.fetch(this.context.getPageSize(), this.context.getPageIndex());
		} else {
			this.count = (this.count <= 0 ? 20 : this.count);
			this.data = q.fetch(this.count, 0);
		}
		if (this.data == null) {
			return;
		}
		dealPrevAndNext();
		dealContent();

		prepareLink();
		if (this.loadExtend) {
			dealExtend();
		}
	}

	private void dealPrevAndNext() {
		TemplateConfig config = (TemplateConfig) this.context.getExecutor().getAttributes().get(TemplateConfig.class.getName());
		if (config == null) {
			return;
		}
		String type = config.getType();
		ITemplateType tt = (ITemplateType) TemplateTypeService.getInstance().get(type);
		if (!(tt instanceof AbstractCatalogListTemplate)) {
			return;
		}
		PrevAndNextBL.checkContentList(this.data, this.context.getPageIndex());
	}

	private void prepareLink() {
		this.data.insertColumn("Link");
		if (!this.data.containsColumn("Path")) {
			this.data.insertColumn("Path");
		}
		this.data.insertColumn("_RowNo");
		this.data.insertColumn("AttributeStr");

		String platformID = PublishPlatformUtil.getPlatformID(this.context);
		for (int i = 0; i < this.data.getRowCount(); i++) {
			DataRow dr = this.data.get(i);
			dr.set("AttributeStr", getAttributeNames(this.data.getInt(i, "Attribute")));
			dr.set("_RowNo", Integer.valueOf(i + 1));
			dealLink(dr);
			ZCContent c = new ZCContent();
			c.setValue(dr);
			String link = ContentUtil.getContentLink(c, this.context);
			dr.set("Link", link);

			String logoFile = dr.getString("LogoFile");
			if ((StringUtil.isNotNull(logoFile)) && (!logoFile.contains(":/"))) {
				logoFile = ContentBL.getLogoFilePath(logoFile, platformID, this.siteID);
				logoFile = ResourceBL.getAbsoluteURL(logoFile, this.context);
				dr.set("LogoFile", logoFile);
			}
			if (!this.data.containsColumn("Path")) {
				dr.set("Path", DetailNameRule.getContentPath(c, platformID));
			}
		}
	}

	private String getAttributeNames(int attribute) {
		String attributeNames = "";
		if (attribute == 0) {
			return attributeNames;
		}
		String attributestrs = ContentUtil.attributeToString(attribute);
		Mapx<String, ZDCode> map = CodeCache.getMapx("ContentAttribute");
		Mapx<String, String> m = new Mapx();
		for (ZDCode code : map.values()) {
			m.put(code.getCodeValue(), code.getCodeName());
		}
		String[] attributestrarr = attributestrs.split(",");
		for (String attributestr : attributestrarr) {
			attributeNames = attributeNames + Lang.get(m.getString(attributestr), new Object[0]) + ",";
		}
		attributeNames = attributeNames.substring(0, attributeNames.length() - 1);
		return attributeNames;
	}

	public int getPageTotal() {
		return this.pageTotal;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getHasAttribute() {
		return this.hasAttribute;
	}

	public void setHasAttribute(String hasAttribute) {
		this.hasAttribute = hasAttribute;
	}

	public String getPlatformAttribute() {
		return this.platformAttribute;
	}

	public void setPlatformAttribute(String platformAttribute) {
		this.platformAttribute = platformAttribute;
	}

	public String getNoAttribute() {
		return this.noAttribute;
	}

	public void setNoAttribute(String noAttribute) {
		this.noAttribute = noAttribute;
	}

	public int getMinWeight() {
		return this.minWeight;
	}

	public void setMinWeight(int minWeight) {
		this.minWeight = minWeight;
	}

	public int getMaxWeight() {
		return this.maxWeight;
	}

	public void setMaxWeight(int maxWeight) {
		this.maxWeight = maxWeight;
	}

	public String getKeyword() {
		return this.keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getOrderby() {
		return this.orderby;
	}

	public void setOrderby(String orderby) {
		this.orderby = orderby;
	}

	public long getSiteID() {
		return this.siteID;
	}

	public void setSiteID(long siteID) {
		this.siteID = siteID;
	}

	public boolean isLoadContent() {
		return this.loadContent;
	}

	public void setLoadContent(boolean loadContent) {
		this.loadContent = loadContent;
	}

	public boolean isLoadExtend() {
		return this.loadExtend;
	}

	public void setLoadExtend(boolean loadExtend) {
		this.loadExtend = loadExtend;
	}

	public boolean isHasLogo() {
		return this.hasLogo;
	}

	public void setHasLogo(boolean hasLogo) {
		this.hasLogo = hasLogo;
	}

	public String getCatalogID() {
		return this.catalogID;
	}

	public void setCatalogID(String catalogID) {
		this.catalogID = catalogID;
	}

	public String getCatalogAlias() {
		return this.catalogAlias;
	}

	public void setCatalogAlias(String catalogAlias) {
		this.catalogAlias = catalogAlias;
	}

	public String getCatalog() {
		return this.catalog;
	}

	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLevel() {
		return this.level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getPublishtime() {
		return this.publishtime;
	}

	public void setPublishtime(String publishtime) {
		this.publishtime = publishtime;
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
