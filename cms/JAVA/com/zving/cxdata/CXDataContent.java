package com.zving.cxdata;

import com.zving.contentcore.AbstractContent;
import com.zving.contentcore.IContent;
import com.zving.schema.ZCCatalog;

/**
 * 车享数据内容
 * @author v_zhouquan
 *
 */
public class CXDataContent extends AbstractContent {
	
	@Override
	public String getContentTypeID() {
		return CXDataContentType.ID;
	}
	

}
