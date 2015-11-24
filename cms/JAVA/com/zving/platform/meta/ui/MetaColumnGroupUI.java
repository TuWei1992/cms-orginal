package com.zving.platform.meta.ui;

import com.zving.framework.Current;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.cache.CacheManager;
import com.zving.framework.data.Q;
import com.zving.framework.i18n.Lang;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.utility.ObjectUtil;
import com.zving.platform.meta.bl.MetaModelColumnBL;
import com.zving.platform.privilege.MetadataPriv;
import com.zving.schema.ZDMetaColumnGroup;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2012-1-13
 */
@Alias("MetaColumnGroup")
public class MetaColumnGroupUI extends UIFacade {
	@Priv
	public void bindGrid(DataGridAction dga) {
		long id = $L("ID");
		Q q = new Q().select("*").from("ZDMetaColumnGroup").where("ModelID", id);
		dga.bindData(q);
	}

	@Priv
	public void init() {
		long id = $L("ID");
		if (id != 0) {
			ZDMetaColumnGroup mcg = new ZDMetaColumnGroup();
			mcg.setID(id);
			mcg.fill();
			Response.putAll(mcg.toMapx());
		}
	}

	@Priv(MetadataPriv.Add + "||" + MetadataPriv.Save)
	public void save() {
		long modelID = $L("ModelID");
		long groupID = $L("ID");
		if (MetaModelColumnBL.isColumnGroupCodeExists($V("Code"), groupID)) {
			fail(Lang.get("Platform.CodeExists"));
			return;
		}
		MetaModelColumnBL.saveColumnGroup(Request, Current.getTransaction());
		if (Current.getTransaction().commit()) {
			success(Lang.get("Common.ExecuteSuccess"));
			CacheManager.remove("Platform", "MetaModel", modelID);
		} else {
			fail(Lang.get("Common.ExecuteFailed") + ":" + Current.getTransaction().getExceptionMessage());
		}

	}

	@Priv(MetadataPriv.Delete)
	public void delete() {
		String ids = $V("IDs");
		if (ObjectUtil.empty(ids)) {
			fail(Lang.get("Common.InvalidID"));
			return;
		}
		DAOSet<ZDMetaColumnGroup> set = MetaModelColumnBL.deleteColumnGroup(ids, Current.getTransaction());
		if (Current.getTransaction().commit()) {
			CacheManager.remove("Platform", "MetaModel", set.get(0).getModelID());
			success(Lang.get("Common.DeleteSuccess"));
		} else {
			fail(Lang.get("Common.DeleteFailed"));
		}
	}
}
