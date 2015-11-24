package com.zving.framework.ui.control;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.Config;
import com.zving.framework.data.DataTypes;
import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.ui.util.TagUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 上传控件标签　
 * 
 * @Author 徐喆
 * @Date 2010-03-12
 * @Mail xuzhe@zving.com
 */
public class UploaderTag extends AbstractTag {
	private String id;

	private String name;

	private String barColor;

	private int width;

	private int height;

	private String allowedType;

	private int fileCount;

	private int fileMaxSize;

	private String fileName;// 用于编辑时显示文件

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "uploader";
	}

	@Override
	public int doStartTag() throws TemplateRuntimeException {
		return EVAL_BODY_BUFFERED;
	}

	@Override
	public int doEndTag() throws TemplateRuntimeException {// 没有body时也要执行
		String content = getBody();
		if (StringUtil.isEmpty(content)) {
			content = "";
		}
		pageContext.getOut().write(getHtml(""));
		return EVAL_PAGE;
	}

	/**
	 * 便于在Java文件中调用
	 */
	public String getHtml(String content) {
		String FlashVars = "";
		String srcSWF = Config.getContextPath() + "framework/components/ZUploader2.swf";
		if (StringUtil.isEmpty(id)) {// 产生随机ID
			id = TagUtil.getTagID(pageContext, "File");
		}
		if (StringUtil.isEmpty(name)) {
			name = id;
		}
		if (StringUtil.isNotEmpty(allowedType)) {
			FlashVars += "elemId=" + id;
		}
		if (StringUtil.isNotEmpty(allowedType)) {
			FlashVars += "&fileType=" + allowedType;
		}
		if (StringUtil.isNotEmpty(fileName)) {
			FlashVars += "&fileName=" + StringUtil.htmlEncode(fileName);
		}
		if (StringUtil.isNotEmpty(barColor)) {
			FlashVars += "&barColor=" + barColor;
		}
		if (fileCount != 0) {
			FlashVars += "&fileCount=" + fileCount;
		}
		if (fileMaxSize != 0) {
			FlashVars += "&fileMaxSize=" + fileMaxSize;
		}
		if (width == 0) {
			width = 250;
		}
		if (height == 0) {
			height = 25;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("<!--[if IE]>");
		sb.append("<object id=\"" + id + "\" name=\"" + name + "\" classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000\" width=\"" + width
				+ "\" height=\"" + height + "\" style=\"vertical-align:middle;\">\n");
		sb.append("<param name=\"movie\" value=\"" + srcSWF + "\">\n");
		sb.append("<param name=\"quality\" value=\"high\">\n");
		sb.append("<param name=\"wmode\" value=\"transparent\">\n");
		sb.append("<param name=\"allowScriptAccess\" value=\"always\">\n");
		sb.append("<param name=\"FlashVars\" value=\"" + FlashVars + "\">\n");
		sb.append("</object>");
		sb.append("<![endif]-->\n");
		sb.append("<!--[if !IE]>-->");
		sb.append("<object id=\"" + id + "\" name=\"" + name + "\" type=\"application/x-shockwave-flash\" data=\"" + srcSWF + "\" width=\""
				+ width + "\" height=\"" + height + "\" style=\"vertical-align:middle;\">\n");
		sb.append("<param name=\"quality\" value=\"high\">\n");
		sb.append("<param name=\"wmode\" value=\"transparent\">\n");
		sb.append("<param name=\"allowScriptAccess\" value=\"always\">\n");
		sb.append("<param name=\"FlashVars\" value=\"" + FlashVars + "\">\n");
		sb.append("</object>");
		sb.append("<!--<![endif]-->");

		sb.append("<script>Zving.Uploader.checkVersion();</script>\n");
		return sb.toString();
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBarColor() {
		return barColor;
	}

	public void setBarColor(String barColor) {
		this.barColor = barColor;
	}

	public String getAllowType() {
		return allowedType;
	}

	public void setAllowType(String allowType) {
		allowedType = allowType;
	}
	/*
	 * 获取上传文件名
	 */
	public String getFileName() {
		return fileName;
	}
	/*
	 * 设置上传文件名
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("id", true));
		list.add(new TagAttr("allowType"));// 应废弃
		list.add(new TagAttr("allowedType"));
		list.add(new TagAttr("barcolor"));
		list.add(new TagAttr("fileCount", DataTypes.INTEGER));
		list.add(new TagAttr("height", DataTypes.INTEGER));
		list.add(new TagAttr("width", DataTypes.INTEGER));
		list.add(new TagAttr("fileMaxSize", DataTypes.INTEGER));
		list.add(new TagAttr("fileName"));
		list.add(new TagAttr("name"));
		return list;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getFileCount() {
		return fileCount;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}

	public int getFileMaxSize() {
		return fileMaxSize;
	}

	public void setFileMaxSize(int fileMaxSize) {
		this.fileMaxSize = fileMaxSize;
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.UIControl.UploaderTagName}";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public String getPluginID() {
		return FrameworkPlugin.ID;
	}

	public String getAllowedType() {
		return allowedType;
	}

	public void setAllowedType(String allowedType) {
		this.allowedType = allowedType;
	}
}
