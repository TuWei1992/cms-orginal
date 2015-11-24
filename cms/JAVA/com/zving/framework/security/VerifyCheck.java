package com.zving.framework.security;

import java.util.Map;
import java.util.Map.Entry;

import com.zving.framework.Current;
import com.zving.framework.annotation.Verify;
import com.zving.framework.collection.Mapx;
import com.zving.framework.core.method.IMethodLocator;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.extend.action.AfterVerifyFailedAction;
import com.zving.framework.utility.ObjectUtil;

/**
 * 数据校验类
 * 
 * @Author 王育春
 * @Date 2011-5-16
 * @Mail wyuch@zving.com
 */
public class VerifyCheck {
	public static boolean check(IMethodLocator m) {
		if (m == null) {
			return false;
		}
		// 从注解中获取权限信息
		boolean flag = m.isAnnotationPresent(Verify.class);
		if (flag) {
			Verify v = m.getAnnotation(Verify.class);
			boolean ignoreAll = v.ignoreAll();
			if (ignoreAll) {// 全部不需要检查
				return true;
			}
			String nocheck = v.ignoredKeys();
			Mapx<String, String> map = new Mapx<String, String>();
			String[] rules = v.value();
			for (String r : rules) {
				int i = r.indexOf("=");
				if (i < 0) {
					continue;
				}
				String name = r.substring(0, i);
				String value = r.substring(i + 1);
				map.put(name, value);
			}
			return check(m, Current.getRequest(), nocheck, map);
		} else {
			return check(m, Current.getRequest(), null, new Mapx<String, String>());// 默认全部检查
		}
	}

	public static boolean check(IMethodLocator m, Map<String, Object> data, String nocheck, Map<String, String> rules) {
		String nocheck2 = "," + nocheck + ",";
		for (Entry<String, Object> entry : data.entrySet()) {
			String k = entry.getKey();
			Object obj = entry.getValue();
			if (!(obj instanceof String)) {
				continue;
			}
			String v = (String) obj;
			if (ObjectUtil.empty(v) || nocheck2.indexOf(k) > 0) {
				continue;// 空字符串及已经声明过的不需要检查了
			}
			if (k.startsWith("_ZVING_")) {// 控件内部变量不需要检查
				continue;
			}
			v = rules.get(k);
			if (v != null) {
				VerifyRule verify = new VerifyRule(v);
				if (!verify.verify(v)) {
					log(m, k, v, rules.get(k));
					return false;
				}
			}
		}
		return true;
	}

	private static void log(IMethodLocator m, String k, String v, String rule) {
		String methodName = m == null ? "-" : m.getName();
		// 扩展点，主要用于记录可能的SQL注入日志
		ExtendManager.invoke(AfterVerifyFailedAction.ID, new Object[] { methodName, k, v, rule });
	}
}
