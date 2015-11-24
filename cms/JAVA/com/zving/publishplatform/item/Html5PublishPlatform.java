package com.zving.publishplatform.item;

import com.zving.contentcore.AbstractPublishPlatform;

public class Html5PublishPlatform extends AbstractPublishPlatform {
	public static final String ID = "Html5";

	public String getExtendItemID() {
		return "Html5";
	}

	public String getExtendItemName() {
		return "Html5";
	}

	public String getConfigURL() {
		return "publishplatform/html5PublishPlatform.zhtml";
	}

	public String getIcon() {
		return "icons/icon004a1.png";
	}

	public int getOrder() {
		return 2;
	}

	public boolean isDetailTemplateMandatory() {
		return true;
	}

	public boolean isListTemplateMandatory() {
		return true;
	}

	public boolean isNeedCheckLinkInFiles() {
		return true;
	}
}
