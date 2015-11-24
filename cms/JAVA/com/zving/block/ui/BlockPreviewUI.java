package com.zving.block.ui;

import com.meidusa.toolkit.common.util.StringUtil;
import com.zving.block.IBlockType;
import com.zving.block.service.BlockTypeService;
import com.zving.contentcore.item.PCPublishPlatform;
import com.zving.framework.Config;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.core.handler.ZAction;
import com.zving.schema.ZCBlock;

public class BlockPreviewUI extends UIFacade {
	@Priv
	@Alias("block/preview")
	public void preview(ZAction za) {
		long id = $L("ID");
		String platformID = $V("platformID");
		if (StringUtil.isEmpty(platformID)) {
			platformID = PCPublishPlatform.ID;
		}
		if (id == 0L) {
			return;
		}
		ZCBlock block = new ZCBlock();
		block.setID(id);
		if (!block.fill()) {
			return;
		}
		try {
			IBlockType blockType = (IBlockType) BlockTypeService.getInstance().get(block.getType());
			String html = blockType.getHtml(block, "Block", platformID, true);
			za.writeHTML("<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN' 'http://www.w3.org/TR/html4/loose.dtd'><html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'>\n<head><script type='text/javascript' src='" +

			Config.getContextPath() + "framework/main.js'></script>\n" + "</head><body>\n" + html + "</body></html>");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
