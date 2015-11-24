package com.zving.framework.ui;

import java.util.concurrent.locks.ReentrantLock;

import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Priv;
import com.zving.framework.config.CodeSourceClass;
import com.zving.framework.core.method.IMethodLocator;
import com.zving.framework.core.method.MethodLocatorUtil;
import com.zving.framework.data.DataTable;
import com.zving.framework.security.PrivCheck;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 下拉框代码UI类，响应前端JS中的下拉框loadData请求
 */
public class CodeSourceUI extends UIFacade {
	private static CodeSource codeSourceInstance;
	private static ReentrantLock lock = new ReentrantLock();

	@Priv(login = false)
	public void getData() {
		String codeType = $V("CodeType");
		if (StringUtil.isEmpty($V("ConditionField"))) {
			Request.put("ConditionField", "1");
			Request.put("ConditionValue", "1");
		}
		DataTable dt = null;
		String method = $V("Method");
		if (StringUtil.isEmpty(method) && codeType.startsWith("#")) {
			method = codeType.substring(1);
		}
		if (StringUtil.isNotEmpty(method)) {
			try {
				IMethodLocator m = MethodLocatorUtil.find(method);
				PrivCheck.check(m);
				Object o = m.execute();
				dt = (DataTable) o;
			} catch (Exception e) {
				throw new UIException(method + " must return DataTable");
			}
		} else {
			CodeSource cs = getCodeSourceInstance();
			dt = cs.getCodeData(codeType, Request);
		}
		$S("DataTable", dt);
	}

	public static void initCodeSource() {
		if (codeSourceInstance == null) {
			lock.lock();
			try {
				if (codeSourceInstance == null) {
					String className = CodeSourceClass.getValue();
					if (StringUtil.isEmpty(className)) {
						LogUtil.warn("CodeSource class not found");
						return;
					}
					try {
						Class<?> c = Class.forName(className);
						Object o = c.newInstance();
						codeSourceInstance = (CodeSource) o;
					} catch (Exception e) {
						throw new UIException("Load CodeSource class failed:" + e.getMessage());
					}
				}
			} finally {
				lock.unlock();
			}
		}
	}

	public static CodeSource getCodeSourceInstance() {
		initCodeSource();
		return codeSourceInstance;
	}

}
