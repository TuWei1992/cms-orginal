package com.zving.deploy.bl;

import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.jce.provider.JDKX509CertificateFactory;

import com.zving.contentcore.util.SiteUtil;
import com.zving.deploy.util.DeployItem;
import com.zving.deploy.util.DeployManager;
import com.zving.deploy.util.FileListUtil;
import com.zving.framework.Config;
import com.zving.framework.User;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.Q;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.security.LicenseInfo;
import com.zving.framework.security.SystemInfo;
import com.zving.framework.security.ZRSACipher;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.schema.ZCDeployConfig;
import com.zving.schema.ZCSite;

public class DeployBL {
	public static Mapx<String, String> tryToDeploy(String filePath) {
		return tryToDeploy(filePath, DeployItem.DeployType.UPLOAD);
	}

	public static Mapx<String, String> tryToDeploy(String filePath, DeployItem.DeployType deployType) {
		Mapx<String, String> map = new Mapx();
		filePath = FileUtil.normalizePath(filePath);
		DAOSet<ZCDeployConfig> configs = new ZCDeployConfig().query(new Q("where Enable=?", new Object[] { "Y" }));
		for (ZCDeployConfig config : configs) {
			ZCSite site = SiteUtil.getDAO(config.getSiteID());
			if (site == null) {
				config.deleteAndBackup();
			} else {
				String absoluteSourceDir = SiteUtil.getSiteRoot(config.getSiteID()) + config.getSourceDir();
				absoluteSourceDir = FileUtil.normalizePath(absoluteSourceDir);
				if (filePath.startsWith(absoluteSourceDir)) {
					if (StringUtil.isNotEmpty(config.getFileFilter())) {
						String[] ffs = config.getFileFilter().split("\\n");
						boolean flag = false;
						for (String ff : ffs) {
							if (filePath.startsWith(FileUtil.normalizePath(absoluteSourceDir + ff))) {
								flag = true;
								break;
							}
						}
						if (flag) {
							continue;
						}
					}
					String targetPath = config.getTargetDir() + filePath.substring(absoluteSourceDir.length());
					DeployManager.getInstance().addFile(config.getID(), deployType, filePath, targetPath);
					map.put(filePath, targetPath);
				}
			}
		}
		return map;
	}

	public static void scan(ZCDeployConfig config) {
		String platformID = StringUtil.isNull(config.getPlatformID()) ? "pc" : config.getPlatformID();
		String siteRoot = SiteUtil.getSiteRoot(config.getSiteID(), platformID);

		long beginTime = System.currentTimeMillis();
		LogUtil.info("Begin to scan deploy directory: " + siteRoot + config.getSourceDir());
		Mapx<String, Long> allFileMap = FileListUtil.getAllFiles(siteRoot + config.getSourceDir());

		List<String> delFileList = new ArrayList();

		List<String> allDirs = FileListUtil.getAllDirs(siteRoot + config.getSourceDir());
		String targetDir = config.getTargetDir();

		for (String dirPath : allDirs) {
			Mapx<String, Long> mapDeployLog = DeployLogBL.readLog(dirPath, config.getID() + "");
			// LogUtil.info("E-2: " + mapDeployLog.size());
			for (String key : mapDeployLog.keySet()) {
				if (!key.startsWith(siteRoot)) {
					continue;
				}
				if (!allFileMap.containsKey(key)) {
					delFileList.add(key);
					mapDeployLog.remove(key);
					continue;
				}
				if ((mapDeployLog.getLong(key) >= allFileMap.getLong(key))) {
					allFileMap.remove(key);
				}
			}

		}
		LogUtil.debug("Add deploy file to pending list...");
		try {
			for (String filePath : allFileMap.keySet()) {
				if (StringUtil.isNotEmpty(config.getFileFilter())) {
					String[] ffs = config.getFileFilter().split("\\n");
					boolean flag = false;
					for (int i = 0; i < ffs.length; i++) {
						String ff = ffs[i];
						ff = ff.replace(":", "\\");
						String relativePath = filePath.substring(FileUtil.normalizePath(siteRoot + config.getSourceDir()).length());
						if (relativePath.matches(ff)) {
							flag = true;
							break;
						}
					}
					if (!flag) {
						continue;
					}
				}
				filePath = FileUtil.normalizePath(filePath);
				String tarFile = targetDir + filePath.substring(siteRoot.length());
				tarFile = FileUtil.normalizePath(tarFile);

				DeployManager.getInstance().addFile(config.getID(), DeployItem.DeployType.UPLOAD, filePath, tarFile);
			}
			for (String filePath : delFileList) {
				filePath = FileUtil.normalizePath(filePath);
				String tarFile = targetDir + filePath.substring(siteRoot.length());
				tarFile = FileUtil.normalizePath(tarFile);

				DeployManager.getInstance().addFile(config.getID(), DeployItem.DeployType.DELETE, filePath, tarFile);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		LogUtil.info("Deploy scan cost : " + (System.currentTimeMillis() - beginTime) / 1000L);
	}
}

/*
 * Location: D:\tmpp\
 * 
 * Qualified Name: com.zving.deploy.bl.DeployBL
 * 
 * JD-Core Version: 0.7.0.1
 */
