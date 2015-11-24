package com.zving.platform.update;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import com.zving.framework.Config;
import com.zving.framework.collection.Mapx;
import com.zving.framework.i18n.Lang;
import com.zving.framework.ui.control.LongTimeTask;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.IOUtil;
import com.zving.framework.utility.PropertiesUtil;
import com.zving.framework.utility.ZipUtil;
import com.zving.platform.update.UpdateServer.PluginUpdateRecord;

/**
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-8-17
 */
public class UpdateDownloader {
	LongTimeTask ltt;
	String server;

	public UpdateDownloader(LongTimeTask ltt, String server) {
		this.ltt = ltt;
		this.server = server;
	}

	public void download() throws Exception {
		UpdateServer us = new UpdateServer(server);
		us.loadContent();
		File current = new File(Config.getPluginPath() + "/update/current/");
		if (!current.exists()) {
			current.mkdirs();
		}
		File statusFile = new File(Config.getPluginPath() + "update/update.status");
		Mapx<String, String> map = statusFile.exists() ? PropertiesUtil.read(statusFile) : new Mapx<String, String>();
		int i = 0;
		for (PluginUpdateRecord upr : us.getPluginUpdateRecords()) {
			if (!upr.NeedUpdate) {
				continue;
			}
			String str = server + "/plugins/" + upr.ID + ".zip";
			URL url = new URL(str);
			InputStream is = url.openStream();
			try {
				ltt.setCurrentInfo(Lang.get("Platform.PluginCurrentDownloading") + upr.ID);
				ltt.setPercent(i * 100 / us.getPluginUpdateRecords().size());
				byte[] bs = IOUtil.getBytesFromStream(is);
				String fileName = current.getAbsolutePath() + "/" + upr.ID + ".zip";
				FileUtil.writeByte(fileName, bs);
				ZipUtil.unzip(fileName, current.getAbsolutePath());
				FileUtil.delete(fileName);
				map.put(upr.ID, upr.LastUpdateTime + "");
			} finally {
				if (is != null) {
					is.close();
				}
			}
			i++;
		}
		PropertiesUtil.write(statusFile, map);
	}
}
