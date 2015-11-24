package com.zving.test;

import com.zving.contentcore.AbstractContentType;
import com.zving.contentcore.IContent;
import com.zving.framework.orm.DAO;
import com.zving.schema.Book;

public class BookContentType extends AbstractContentType {
	public static final String ID = "Book";
	public static final String NAME = "图书";
	public static final String CATALOG_NAME = "图书栏目";
	public static final String DATA_IMPORT_ID_KEY = new Book().table() + ".ID";
	
	@Override
	public DAO<?> createExtendDAOInstance() {
		// TODO Auto-generated method stub
		return new Book();
	}

	@Override
	public String getCatalogIcon() {
		// TODO Auto-generated method stub
		return "test/images/bookCatalog.png";
	}

	@Override
	public String getCatalogName() {
		// TODO Auto-generated method stub
		return CATALOG_NAME;
	}

	@Override
	public String getDataImpIdKey() {
		// TODO Auto-generated method stub
		return DATA_IMPORT_ID_KEY;
	}

	@Override
	public String getDetailIcon() {
		// TODO Auto-generated method stub
		 return "test/images/book.png";
	}

	@Override
	public String getDetailTemplateTypeID() {
		// TODO Auto-generated method stub
		return "BookDetail";
	}

	@Override
	public String getEditorURL() {
		// TODO Auto-generated method stub
		return "test/bookQuickEditor.zhtml?CatalogID={CatalogID}&ContentID={ID}";
	}

	@Override
	public String getExtendItemID() {
		// TODO Auto-generated method stub
		return ID;
	}

	@Override
	public String getExtendItemName() {
		// TODO Auto-generated method stub
		return NAME;
	}

	@Override
	public String getListTemplateTypeID() {
		// TODO Auto-generated method stub
		return "BookList";
	}

	@Override
	public String getListURL() {
		// TODO Auto-generated method stub
		return "test/bookList.zhtml";
	}

	@Override
	public String getQuickEditURL() {
		// TODO Auto-generated method stub
		return "test/bookQuickEditor.zhtml?CatalogID={CatalogID}&ContentID={ID}";
	}

	@Override
	public boolean hasExtendTable() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public IContent newContent() {
		// TODO Auto-generated method stub
		return new BookContent();
	}

}
