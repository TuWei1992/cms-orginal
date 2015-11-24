package com.zving.block.type;

import java.io.File;
import java.io.FileNotFoundException;

import com.zving.block.IBlockType;
import com.zving.block.util.BlockUtil;
import com.zving.contentcore.ContentCorePlugin;
import com.zving.contentcore.util.SiteUtil;
import com.zving.framework.i18n.Lang;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.template.TemplateWriter;
import com.zving.framework.utility.Errorx;
import com.zving.framework.utility.StringUtil;
import com.zving.schema.ZCBlock;
import com.zving.staticize.template.ITemplateType;
import com.zving.staticize.template.TemplateInstance;

public class TemplateBlock implements IBlockType {
	public static final String TypeID = "Template";

	public String getExtendItemID() {
		return "Template";
	}

	public String getExtendItemName() {
		return "@{Contentcore.TemplateGenerator}";
	}

	public String getEditURL() {
		return "block/templateBlock.zhtml";
	}

	public String getHtml(ZCBlock schema, String templateTypeID,
			String platformID, boolean preview) {
		try {
			ITemplateType tt = ContentCorePlugin.getStaticizeContext().getTemplateType(templateTypeID);
			AbstractExecuteContext context = tt.getContext(schema.getID(), platformID, preview);
			
			String blockTemplate = schema.getTemplate();
			TemplateWriter writer = new TemplateWriter();
			if (StringUtil.isNotNull(blockTemplate)) {
				blockTemplate = SiteUtil.getSiteRoot(schema.getSiteID(), platformID) + blockTemplate;
				if (!new File(blockTemplate).exists()) {
					writer.close();
					return Lang.get("Staticize.TemplateNotExists", new Object[] { blockTemplate });
				}
				TemplateInstance tpl = ContentCorePlugin.getStaticizeContext().getTemplateManager().find(blockTemplate);
				tpl.setContext(context);
				tpl.setWriter(writer);
				tpl.execute();
			}
			String html = writer.getResult();
			if (context.isPreview()) {
				return html;
			}
			return BlockUtil.processResource(schema.getSiteID(), platformID, html);
		} catch (Exception e) {
			e.printStackTrace();
			Errorx.addError(e.getMessage());
			return e.getMessage();
		}
	}
}
