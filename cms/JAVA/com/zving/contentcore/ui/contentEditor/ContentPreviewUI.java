package com.zving.contentcore.ui.contentEditor;

import com.zving.contentcore.ContentCorePlugin;
import com.zving.contentcore.IContent;
import com.zving.contentcore.IContentType;
import com.zving.contentcore.item.PCPublishPlatform;
import com.zving.contentcore.service.ContentTypeService;
import com.zving.contentcore.service.TemplateTypeService;
import com.zving.contentcore.template.types.AbstractDetailTemplate;
import com.zving.contentcore.util.InternalURLUtil;
import com.zving.contentcore.util.PublishPlatformUtil;
import com.zving.cxdata.property.PlatformContentTemplate;
import com.zving.cxdata.property.PlatformContentTemplateFlag;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.core.handler.ZAction;
import com.zving.framework.i18n.Lang;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.template.TemplateWriter;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.code.YesOrNo;
import com.zving.schema.ZCContent;
import com.zving.staticize.template.ITemplateType;
import com.zving.staticize.template.TemplateInstance;

public class ContentPreviewUI extends UIFacade {
	private void generate(ZAction za, boolean preview) {
		long id = $L("ID");
		if (id == 0L) {
			return;
		}
		ZCContent dao = new ZCContent();
		dao.setID(id);
		if (!dao.fill()) {
			za.writeHTML(Lang.get("Contentcore.ContentNoteExist", new Object[0]));
			return;
		}
		String platformID = za.getRequest().getParameter("platformID");
		if (StringUtil.isEmpty(platformID)) {
			platformID = "pc";
		}
		IContentType ct = ContentTypeService.getContentType(dao.getContentTypeID());
		IContent content = ct.loadContent(dao, null);
		content.setProp("_PageIndex", Integer.valueOf($I("PageIndex")));
		ITemplateType tt = (ITemplateType) TemplateTypeService.getInstance().get(ct.getDetailTemplateTypeID());
		if (tt == null) {
			za.writeHTML(Lang.get("Contentcore.NoDetailTemplateSet", new Object[0]));
			return;
		}
		try {
			AbstractExecuteContext context = null;
			if ((tt instanceof AbstractDetailTemplate)) {
				AbstractDetailTemplate adt = (AbstractDetailTemplate) tt;
				context = adt.getContext(content, platformID, preview);
				adt.beforeConentPublish(dao, context);
			} else {
				context = tt.getContext(content.getID(), platformID, preview);
			}
			if (YesOrNo.isYes(context.eval("Content.LinkFlag"))) {
				String url = InternalURLUtil.getActualURL(context.eval("Article.RedirectURL"), context);
				za.redirect(url);
				return;
			}
			context.addDataVariable("Request", this.Request);
			long catalogID = context.evalLong("Catalog.ID");

			String detailTemplate = PublishPlatformUtil.getCatalogDetailTemplate(catalogID, platformID);
			
			if (PCPublishPlatform.ID.equals(platformID)) {
				if (ObjectUtil.equal(dao.getTemplateFlag(), "Y")) {
					detailTemplate = dao.getTemplate();
				}
			} else {
				if (PlatformContentTemplateFlag.templateFlag(dao.getConfigProps(), platformID)) {
					detailTemplate = PlatformContentTemplate.getTemplate(dao.getConfigProps(), platformID);
				}
			}
			
			if (ObjectUtil.empty(detailTemplate)) {
				za.writeHTML(Lang.get("Contentcore.NoDetailTemplateSet", new Object[0]) + "标题" + ":" + context.eval("Content.Title"));
				return;
			}
			context.addDataVariable("ContentPageIndex", Integer.valueOf($I("PageIndex")));
			context.addDataVariable("PlatformID", platformID);

			String suffix = PublishPlatformUtil.getSuffix(dao.getSiteID(), platformID);
			if ((".xml".equalsIgnoreCase(suffix)) || (".json".equalsIgnoreCase(suffix))) {
				za.setContentType("text/plain");
			} else {
				za.setContentType("text/html");
			}
			detailTemplate = PublishPlatformUtil.getPublishPlatformRoot(dao.getSiteID(), platformID) + detailTemplate;
			TemplateInstance tpl = ContentCorePlugin.getStaticizeContext().getTemplateManager().find(detailTemplate);
			tpl.setContext(context);
			if (context.isPreview()) {
				TemplateWriter writer = new TemplateWriter();
				tpl.setWriter(writer);
				tpl.execute();

				String result = writer.getResult();
				if (StringUtil.isNotEmpty(result)) {
					result = result.trim();
				}
				za.writeHTML(result);
			} else {
				tpl.setWriter(za.getResponse().getWriter());
				tpl.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
			za.writeHTML(StringUtil.htmlEncode(e.getMessage()));
		}
	}

	@Priv(login = false)
	@Alias("content/show")
	public void interactive(ZAction za) {
		generate(za, false);
	}

	@Priv
	@Alias("content/preview")
	public void preview(ZAction za) {
		generate(za, true);
	}

	@Priv(login = false)
	@Alias(alone = true, value = "content/preview/flag")
	public void init(ZAction za) {
		za.writeHTML("Flag='ee6b3bde58583e8bdafe4bbb6e78988e69d83e68980e69c89'");
	}
}
