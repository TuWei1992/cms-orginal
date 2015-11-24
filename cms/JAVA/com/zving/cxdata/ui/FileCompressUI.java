package com.zving.cxdata.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.zving.adapter.YuiCompressUtil;
import com.zving.contentcore.bl.SiteBL;
import com.zving.contentcore.util.PublishPlatformUtil;
import com.zving.cxdata.UCMConfig;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.i18n.Lang;
import com.zving.framework.utility.Errorx;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;


@Alias("FileCompress")
public class FileCompressUI extends UIFacade {
	
	@Priv()
	public void compressCSSScript() {
		String names = $V("Names");
	    String path = $V("Path");
	    if (path == null) {
	      path = "";
	    }
	    if (ObjectUtil.empty(names)) {
	      return;
	    }
	    long siteID = SiteBL.getCurrentSite();
	    if (ObjectUtil.notEmpty($V("SiteID"))) {
	      siteID = Long.parseLong($V("SiteID"));
	    }
	    String prefix = PublishPlatformUtil.getPublishPlatformRoot(siteID, $V("PlatformID"));
	    String[] arr = names.split("\\,");
	    YuiCompressUtil.compress(prefix + path, arr);
	    if (Errorx.hasError()) {
	    	fail("压缩出错文件：\n" + Errorx.printString());
	    } else {
	    	success(Lang.get("压缩完成！"));
	    }
	}
}
