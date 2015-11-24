package com.zving.stat.tag;

import com.zving.contentcore.util.CatalogUtil;
import com.zving.contentcore.util.SiteUtil;
import com.zving.framework.Config;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.TemplateWriter;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.utility.DateUtil;
import com.zving.platform.bl.SystemInfoBL;
import com.zving.platform.code.YesOrNo;
import com.zving.schema.ZCCatalog;
import com.zving.schema.ZCSite;
import com.zving.stat.property.AutoStatFlag;
import com.zving.stat.property.ThirdPartyStatCode;
import com.zving.stat.property.UseThirdPartyStat;
import com.zving.staticize.tag.SimpleTag;
import java.util.Date;
import java.util.List;

public class CmsStatTag extends SimpleTag {
	public List<TagAttr> getTagAttrs() {
		return null;
	}

	public String getExtendItemName() {
		return "@{Stat.StatTagName}";
	}

	public int onTagStart() throws TemplateRuntimeException {
		long siteID = this.context.evalLong("Site.ID");
		if (siteID != 0L) {
			ZCSite site = SiteUtil.getDAO(siteID);
			if (site != null) {
				String configProps = site.getConfigProps();
				StringBuilder statScript = new StringBuilder();
				if (YesOrNo.isYes(AutoStatFlag.getValue(configProps))) {
					String prefix = this.context.eval("FrontAppContext");

					StringBuilder destURL = new StringBuilder();
					destURL.append("SiteID=" + siteID);
					long catalogID = this.context.evalLong("Catalog.ID");
					if (catalogID != 0L) {
						ZCCatalog catalog = CatalogUtil.getDAO(catalogID);
						if (catalog != null) {
							destURL.append("&CatalogInnerCode=" + catalog.getInnerCode());
							destURL.append("&Type=" + this.context.eval("Content.ContentTypeID"));
						}
					}
					long contentID = this.context.evalLong("Content.ID");
					if (contentID != 0L) {
						destURL.append("&LeafID=" + contentID);
					}
					destURL.append("&Dest=" + prefix + "stat/dealer");
					statScript.append("\n<script src=\"" + prefix + "stat/front/stat.js\" type=\"text/javascript\"></script>\n");
					statScript.append("<script>\n");
					statScript.append("if(window._zcms_stat)_zcms_stat(\"" + destURL + "\");\n");
					statScript.append("</script>\n");
				}
				if (YesOrNo.isYes(UseThirdPartyStat.getValue(configProps))) {
					statScript.append(ThirdPartyStatCode.getValue(configProps) + "\n");
				}
				this.pageContext.getOut().write(statScript);
			}
		}
		String comment = "<!-- App=" + Config.getAppCode() + "(" + LangUtil.get(Config.getAppName()) + ") " + SystemInfoBL.getAppVersion() + " -->\n";
		this.pageContext.getOut().write(comment);
		return 0;
	}

	public String getPrefix() {
		return "cms";
	}

	public String getTagName() {
		return "stat";
	}

	public String getPluginID() {
		return "com.zving.stat";
	}

	public String getDescription() {
		return "@{Stat.StatTagDescription}";
	}
}
