package com.zving.test;

import com.zving.contentcore.AbstractContent;
import com.zving.contentcore.IContent;
import com.zving.schema.ZCCatalog;

public class BookContent extends AbstractContent {

	@Override
	public String getContentTypeID() {
		return BookContentType.ID;
	}
	
	@Override
	public void save() {
		super.save();
	}
	
	@Override
	public long insert() {
		 super.insert();
		 return this.content.getID();
	}
	
	@Override
	public IContent copy(ZCCatalog targetCatalog, int copyType) {
		return super.copy(targetCatalog, copyType);
	}

}
