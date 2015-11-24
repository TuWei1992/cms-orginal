package com.zving.framework.xml;

import com.zving.framework.utility.FastStringBuilder;

/**
 * 表示一个XML指令
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-14
 */
public class XMLInstruction extends XMLNode {
	String instruction;

	public XMLInstruction(String instruction) {
		this.instruction = instruction;
	}

	@Override
	public void toString(String prefix, FastStringBuilder sb) {
		sb.append(prefix).append("<?").append(instruction).append(" ?>");
	}

	@Override
	public String getText() {
		return "";
	}

	@Override
	public int getType() {
		return XMLNode.INSTRUCTION;
	}

	@Override
	void repack() {
		instruction = new String(instruction.toCharArray());
	}

	public String getInstruction() {
		return instruction;
	}

	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}

}
