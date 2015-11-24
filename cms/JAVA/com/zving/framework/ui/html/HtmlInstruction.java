package com.zving.framework.ui.html;

import com.zving.framework.utility.FastStringBuilder;

/**
 * HTML中的指令
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-14
 */
public class HtmlInstruction extends HtmlNode {
	String instruction;

	public HtmlInstruction(String instruction) {
		this.instruction = instruction;
	}

	@Override
	public String getText() {
		return "";
	}

	@Override
	public int getType() {
		return HtmlNode.INSTRUCTION;
	}

	@Override
	void repack() {
		instruction = new String(instruction.toCharArray());
	}

	@Override
	public void format(FastStringBuilder sb, String prefix) {
		sb.append(instruction);
	}

	@Override
	public HtmlNode clone() {
		return new HtmlInstruction(instruction);
	}

	public String getInstruction() {
		return instruction;
	}

	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}

}
