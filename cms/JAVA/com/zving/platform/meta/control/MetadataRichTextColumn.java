package com.zving.platform.meta.control;

import com.zving.framework.User;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.meta.AbstractMetaDataColumnControlType;
import com.zving.platform.meta.MetaUtil;
import com.zving.platform.meta.MetadataService;
import com.zving.schema.ZDMetaColumn;

/**
 * author: 李伟仪
 * Email: lwy@zving.com
 * Date: 2013-5-10
 */
public class MetadataRichTextColumn extends AbstractMetaDataColumnControlType {

	public static final String ID = "RichText";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "@{Platform.RichText}";
	}

	@Override
	public String getHtml(ZDMetaColumn mc, String value) {
		String code = MetadataService.ControlPrefix + mc.getCode();
		if (StringUtil.isEmpty(value)) {
			value = MetaUtil.getValue(mc);
		}

		String styleText = MetaUtil.getClass(mc);
		// String verify = MetaUtil.getVerifys(mc);

		StringBuilder sb = new StringBuilder();
		sb.append("<textarea id=\"").append(code).append("\" ").append("name=\"").append(code).append("\" ").append(styleText)
				.append(">" + value + "</textarea>");
		sb.append("<script>");
		sb.append("UE.delEditor(\""+ code +"\");");
		/*
		sb.append("for(var editorKey in UE.instants) {");
		sb.append("	if(UE.instants[editorKey].key && UE.instants[editorKey].key==\""+code+"\") {");
		sb.append("		UE.instants[editorKey].destroy();");
		sb.append("	}");
		sb.append("}");
		*/
		sb.append("var editor_" + code + " = UE.getEditor(\"").append(code).append("\", {").append("wordCount:false,")
				.append("readonly:typeof(disableExtendUeditor)!='undefined'&&disableExtendUeditor?true:false,")
				.append("elementPathEnabled:false,").append("theme:\"default\",").append("initialFrameWidth:500,autoHeightEnabled:false,")
				.append("toolbars:[['undo','redo','source','fontfamily','fontsize','bold','italic',")
				.append(" 'underline','strikethrough', 'forecolor', 'backcolor','justifyleft', ")
				.append("'justifycenter', 'justifyright', 'justifyjustify','indent',")
				.append("'insertorderedlist', 'insertunorderedlist','link', 'unlink']],").append("lang:\"").append(User.getLanguage())
				.append("\"});");
		sb.append("</script>");
		return sb.toString();
	}

}
