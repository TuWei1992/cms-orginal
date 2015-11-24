package com.zving.framework.data.command;

/**
 * 修改字段长度指令（只支持长度扩大）。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-1-28
 */
public class ChangeColumnLengthCommand extends ChangeColumnMandatoryCommand {

	public static final String Prefix = "ChangeColumnLength:";

	@Override
	public String getPrefix() {
		return Prefix;
	}

}
