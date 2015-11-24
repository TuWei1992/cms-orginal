package com.zving.platform.action;

import java.util.StringTokenizer;

import com.zving.framework.Config;
import com.zving.framework.data.QueryBuilder;
import com.zving.framework.data.Transaction;
import com.zving.framework.i18n.Lang;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.point.BeforeLocalUpdate;

public class BeforeLocalUpdateAction extends BeforeLocalUpdate {
	@Override
	public Object execute(Transaction trans) {
		String filePath = Config.getContextRealPath() + "WEB-INF/data/updater/beforUpdate.sql";
		if (FileUtil.exists(filePath)) {
			String executeSql = FileUtil.readText(filePath, "UTF-8");
			StringTokenizer token = new StringTokenizer(executeSql, ";");
			while (token.hasMoreElements()) {
				String updateSql = token.nextToken().replace(";", "").replace("\r\n", "");
				if (StringUtil.isNotEmpty(updateSql)) {
					trans.add(new QueryBuilder(updateSql));
				}
			}
			if (trans.commit()) {
				LogUtil.info(filePath + Lang.get("Common.ExecuteSuccess"));
			} else {
				LogUtil.info(filePath + "执行失败！");
			}
		}
		return null;
	}
}