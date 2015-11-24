package com.zving.cxdata.tag;

import java.util.ArrayList;
import java.util.List;

import com.zving.cxdata.CXDataPlugin;
import com.zving.framework.data.DataTypes;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.staticize.tag.SimpleTag;

public class ResourceURLTag extends SimpleTag {
	private String url;
	@Override
	public int onTagStart() throws TemplateRuntimeException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "资源标签";
	}

	@Override
	public String getExtendItemName() {
		// TODO Auto-generated method stub
		return "资源标签";
	}

	@Override
	public String getPluginID() {
		// TODO Auto-generated method stub
		return CXDataPlugin.ID;
	}

	@Override
	public String getPrefix() {
		// TODO Auto-generated method stub
		return "cms";
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		ArrayList<TagAttr> list = new ArrayList();
	    list.add(new TagAttr("url", true, DataTypes.STRING, "链接"));
	    return list;
	}

	@Override
	public String getTagName() {
		// TODO Auto-generated method stub
		return "url";
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}
