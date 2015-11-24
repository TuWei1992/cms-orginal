package com.zving.cxdata.tag;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zving.framework.data.DataTable;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.utility.StringUtil;
import com.zving.staticize.tag.AbstractListTag;

public class ZMatchTag extends AbstractListTag {
	private String value;
	private String regex;

	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = super.getTagAttrs();
		list.add(new TagAttr("value", true, 1, "需要匹配查寻的内容"));
		list.add(new TagAttr("regex", false, 1, "匹配正则表达式"));
		return list;
	}

	public void prepareData() throws TemplateRuntimeException {
		this.item = "Data";
		if ((StringUtil.isEmpty(this.value))) {
			return;
		}
		if ((StringUtil.isEmpty(this.regex))) {
			this.regex = ".+";
		}
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(value);
		this.data = new DataTable();
		this.data.insertColumn("Value");
		this.data.insertColumn("Group");

		while (m.find()) {
			String [] groups = new String[m.groupCount() + 1];
			for (int i = 0; i < m.groupCount()+1; i++) {
				groups[i] = m.group(i);
			}
			this.data.insertRow(new Object[]{m.group(), groups});
		}
		this.context.setPageTotal(data.getRowCount());
	}

	public String getDescription() {
		return "正则匹配文本";
	}

	public String getPrefix() {
		return "z";
	}

	public String getTagName() {
		return "match";
	}

	public String getExtendItemName() {
		return "正则匹配文本";
	}

	public String getPluginID() {
		return "com.zving.cxdata";
	}

	public int getPageTotal() {
		return this.context.getPageTotal();
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}
	
	public static void main(String[] args) {
		String value = "<a style='a123' onclick='test();' href='acd'>链接</a>&nbsp;<a href=\"link2\" >link2</a>outher thing<a>132</a>";
		String regex = "<a.*?(\\s*href=('|\")\\S+?\\2\\s*)?.*?>.+?</a>";
		String rega = "<a.*?>.+?</a>";
		Pattern p = Pattern.compile(rega);
		Matcher m = p.matcher(value);
		int c = 1;
		while (m.find()) {
			System.out.println("----------------"+ c++ +"------------------");
			System.out.println(m.groupCount());
			System.out.println(" m.group(): " + m.group());
			for (int i = 0; i < m.groupCount()+1; i++) {
				System.out.println("m.group("+ i +")：" + m.group(i));
			}
		}
		
	}

}
