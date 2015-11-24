package com.zving.platform.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.zving.framework.data.Transaction;
import com.zving.framework.extend.AbstractExtendService;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.FixedCodeType;
import com.zving.platform.FixedCodeType.FixedCodeItem;
import com.zving.platform.util.OrderUtil;
import com.zving.schema.ZDCode;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-11-17
 */
public class CodeService extends AbstractExtendService<FixedCodeType> {

	public static CodeService getInstance() {
		return findInstance(CodeService.class);
	}

	/**
	 * 将各插件注册的Code持久化到数据库中 可变代码如果数据库中以存在则不持久化
	 */
	public static void init() {

		Transaction trans = new Transaction();
		ArrayList<String> dbSet = new ArrayList<String>();
		for (ZDCode code : new ZDCode().query()) {
			dbSet.add("@CodeType=" + code.getCodeType() + "@ParentCode=" + code.getParentCode() + "@CodeValue=" + code.getCodeValue());
		}
		for (FixedCodeType fct : CodeService.getInstance().getAll()) {
			ZDCode code = new ZDCode();
			code.setCodeType(fct.getCodeType());
			code.setParentCode("System");
			code.setCodeValue("System");

			if (!dbSet.contains("@CodeType=" + fct.getCodeType() + "@ParentCode=System@CodeValue=System")) {
				// 不存在的插入数据库
				code.setCodeName(fct.getCodeName());
				code.setCodeOrder(OrderUtil.getDefaultOrder());
				code.setAddTime(new Date());
				code.setAddUser("System");
				trans.add(code, Transaction.INSERT);
			}

			List<FixedCodeItem> items = fct.getFixedItems();
			for (FixedCodeItem item : items) {
				// 如果数据库不存在则插入
				if (!dbSet.contains("@CodeType=" + code.getCodeType() + "@ParentCode=" + code.getCodeType() + "@CodeValue="
						+ item.getValue())) {
					if (StringUtil.isEmpty(item.getValue())) {
						continue;
					}
					ZDCode codeChild = new ZDCode();
					codeChild.setCodeType(code.getCodeType());
					codeChild.setParentCode(code.getCodeType());
					codeChild.setCodeValue(item.getValue());
					codeChild.setCodeName(item.getName());
					codeChild.setCodeOrder(OrderUtil.getDefaultOrder());
					codeChild.setMemo(item.getMemo());
					codeChild.setIcon(item.getIcon());
					codeChild.setAddTime(new Date());
					codeChild.setAddUser("System");
					trans.add(codeChild, Transaction.DELETE_AND_INSERT);
				}
			}
		}
		if (!trans.commit()) {
			LogUtil.error("Code 初始化失败！");
		}
	}
}
