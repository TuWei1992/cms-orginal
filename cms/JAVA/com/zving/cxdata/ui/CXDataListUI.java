package com.zving.cxdata.ui;

import com.zving.contentcore.ui.contentEditor.ContentEditorUI;
import com.zving.cxdata.bl.CXDataModelBL;
import com.zving.cxdata.property.CXDataModelProp;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.ui.control.DataListAction;
import com.zving.framework.utility.Errorx;
import com.zving.framework.utility.StringUtil;
import com.zving.schema.CXDataModel;

@Alias("CXDataList")
public class CXDataListUI extends UIFacade {
	/**
	 * 后台栏目列表页数据
	 * @param dla
	 */
	@Priv
	public void bindList(DataListAction dla) {
		try {
			String modelID = CXDataModelProp.getValue($L("CatalogID"));
			String conditions = $V("searchConditions");
			if (StringUtil.isNotEmpty(modelID)) {
				CXDataModel dm = new CXDataModel();
				dm.setID(Long.parseLong(modelID));
				if (dm.fill()) { 
					if (!(StringUtil.isEmpty(conditions) && CXDataModelBL.hasSearchCondition(dm.getID())) || StringUtil.isNotEmpty($V("searchFlag"))) {
						dla.bindData(CXDataModelBL.searchDataStr(dm, CXDataModelBL.modifyParam(dm, conditions)));
					}
				}
			}
		} catch (Exception e) {
			fail("数据获取出错：" + e.getMessage());
		}
		if (Errorx.hasError()) {
			fail(Errorx.getAllMessage());
		}
	}
	
	/**
	 * 栏目列表页初始化
	 */
	@Priv
	public void init() {
		new ContentEditorUI().init();
		String modelID = CXDataModelProp.getValue($L("CatalogID"));
		if (StringUtil.isNotEmpty(modelID)) {
			$S("Conditions", CXDataModelBL.parseModelFieldTag(null, Long.parseLong(modelID)));
		}
	}
}
