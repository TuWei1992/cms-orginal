package com.zving.platform.ui;

import java.io.File;
import java.util.Date;

import com.zving.cxdata.config.LicensePath;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.i18n.Lang;
import com.zving.framework.security.LicenseInfo;
import com.zving.framework.security.PasswordUtil;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.FileUtil;
import com.zving.platform.config.AdminUserName;
import com.zving.schema.ZDUser;

/**
 * @Author 王育春
 * @Date 2009-4-17
 * @Mail wyuch@zving.com
 */
@Alias("License")
public class LicenseUI extends UIFacade {
	@Priv(login = false)
	public void getRequest() {
		try {
			$S("Request", LicenseInfo.generateRequestSN());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Priv(login = false)
	public void saveLicense() {
		String license = $V("License");
		if (LicenseInfo.verifyLicense(license)) {
			ZDUser user = new ZDUser();
			user.setUserName(AdminUserName.getValue());
			user.fill();
			String password = $V("Password");
			if (!PasswordUtil.verify(password, user.getPassword())) {
				fail(Lang.get("Platform.AdminPasswordInvalid"));
				return;
			}
			//CX控制licenes文件的读取位置
			String fileName = LicensePath.getValue();
			File file = new File(fileName);
			if (!file.getParentFile().exists()) {
		        file.getParentFile().mkdirs();
		    }
			FileUtil.writeText(fileName, license);
			
			LicenseInfo.update();
			success(Lang.get("Common.SaveSuccess"));
		} else {
			fail(Lang.get("Platform.InvalidLicense"));
		}
	}

	public static boolean needWarning() {
		Date endDate = LicenseInfo.getEndDate();
		if (DateUtil.addDay(endDate, -30).getTime() < System.currentTimeMillis()) {
			return true;
		}
		return false;
	}

	public static boolean nearEndDateWarning() {
		Date endDate = LicenseInfo.getEndDate();
		if (DateUtil.addDay(endDate, -7).getTime() < System.currentTimeMillis()) {
			return true;
		}
		return false;
	}
}
