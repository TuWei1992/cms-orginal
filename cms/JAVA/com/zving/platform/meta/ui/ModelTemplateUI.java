package com.zving.platform.meta.ui;

import com.zving.framework.Current;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.annotation.Verify;
import com.zving.framework.core.handler.ZAction;
import com.zving.framework.i18n.Lang;
import com.zving.framework.ui.tag.ListAction;
import com.zving.framework.utility.ObjectUtil;
import com.zving.platform.meta.bl.MetaModelTemplateBL;
import com.zving.platform.privilege.MetadataPriv;

/**
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-2-15
 */
@Alias("ModelTemplate")
public class ModelTemplateUI extends UIFacade {

	@Priv(MetadataPriv.MenuID)
	public void initTemplate() {
		long modelID = $L("ID");
		if (modelID == 0) {
			return;
		}
	}

	@Priv(MetadataPriv.MenuID)
	@Alias(value = "model/template/preview", alone = true)
	public void preview(ZAction za) {
		long modelID = $L("ID");
		String tmplTypeID = $V("MMTemplateTypeID");
		if (modelID == 0 || ObjectUtil.empty(tmplTypeID)) {
			return;
		}
		String html = MetaModelTemplateBL.getPreviewHtml(modelID, tmplTypeID);
		za.writeHTML(html);
	}

	@Priv(MetadataPriv.Save)
	@Verify(ignoreAll = true)
	public void save() {
		long modelID = $L("ID");
		if (modelID == 0) {
			fail(Lang.get("Common.InvalidID"));
			return;
		}
		MetaModelTemplateBL.save(Request, Current.getTransaction());
		if (Current.getTransaction().commit()) {
			success(Lang.get("Common.ExecuteSuccess"));
		} else {
			fail(Lang.get("Common.ExecuteFailed"));
		}
	}

	@Priv
	public void tmplTypeBind(ListAction la) {
		long id = $L("ID");
		if (id == 0) {
			return;
		}
		la.bindData(MetaModelTemplateBL.getTemplateTypes(id));
	}
}
