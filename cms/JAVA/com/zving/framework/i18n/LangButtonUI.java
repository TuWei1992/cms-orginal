package com.zving.framework.i18n;

import java.util.Map;
import java.util.Map.Entry;

import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Priv;
import com.zving.framework.data.DataTable;
import com.zving.framework.ui.tag.ListAction;

/**
 * 国际化按钮UI类，国际化按钮点击后弹出的对话框需要使用本类提供语言列表。
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-11-11
 */
public class LangButtonUI extends UIFacade {
	@Priv(login = false)
	public void bindLanguageList(ListAction la) {
		DataTable dt = new DataTable();
		dt.insertColumn("Key");
		dt.insertColumn("Name");
		Map<String, String> all = LangUtil.getSupportedLanguages();
		for (Entry<String, String> e : all.entrySet()) {
			dt.insertRow(new Object[] { e.getKey(), e.getValue() });
		}
		la.bindData(dt);
	}
}
