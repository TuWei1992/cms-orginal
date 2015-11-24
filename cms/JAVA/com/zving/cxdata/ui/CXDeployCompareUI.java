package com.zving.cxdata.ui;

import java.io.File;

import com.zving.contentcore.util.SiteUtil;
import com.zving.deploy.IDeployType;
import com.zving.deploy.service.DeployTypeService;
import com.zving.deploy.util.conn.AbstractDeployConn;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.i18n.Lang;
import com.zving.framework.i18n.LangMapping;
import com.zving.framework.utility.Errorx;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.schema.ZCDeployConfig;

@Alias("CXDeployCompare")
public class CXDeployCompareUI extends UIFacade {
	@Priv
	public void deleteTarget() {
		long deployID = $L("ConfigID");
		String filePaths = $V("UploadFiles");
		if ((StringUtil.isEmpty(filePaths)) || (deployID == 0L)) {
			fail(Lang.get("Deploy.ErrorMsg1", new Object[0]));
			return;
		}
		ZCDeployConfig config = new ZCDeployConfig();
		config.setID(deployID);
		if (!config.fill()) {
			fail(LangMapping.get("Common.InvalidID", new Object[0]));
			return;
		}
		IDeployType dt = (IDeployType) DeployTypeService.getInstance().get(config.getMethod());
		AbstractDeployConn conn = dt.getConnection(config);
		conn.connect();
		if (!conn.isConnected()) {
			fail("创建链接失败：" + config.getName());
			return;
		}
		for (String path : StringUtil.splitEx(filePaths, ",")) {
			path = FileUtil.normalizePath(path);
			IDeployType deploy = (IDeployType) DeployTypeService.getInstance().get(config.getMethod());
			if (deploy == null) {
				Errorx.addError("Delete Failed: " + config.getMethod());
				break;
			}
			//String siteRoot = SiteUtil.getSiteRoot(config.getSiteID());
			//String targetPath = FileUtil.normalizePath(config.getTargetDir() + "/" + path.substring(siteRoot.length(), path.length()));
			if (!conn.delete(path)) {
				Errorx.addError("Delete Failed：FilePath=" + path);
			}
		}
		conn.disconnect();
		if (!Errorx.hasError()) {
			success("删除成功！");
		} else {
			fail(Errorx.getAllMessage());
		}
	}

}
