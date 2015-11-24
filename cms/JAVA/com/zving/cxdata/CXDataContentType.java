package com.zving.cxdata;

import com.zving.contentcore.AbstractContentType;
import com.zving.contentcore.IContent;
import com.zving.framework.orm.DAO;
import com.zving.schema.Book;
/**
 * 车享数据内容类型
 * @author v_zhouquan
 *
 */
public class CXDataContentType extends AbstractContentType {
	public static final String ID = "CXData";
	public static final String NAME = "车享数据";
	public static final String CATALOG_NAME = "车享数据栏目";

	
	@Override
	public DAO<?> createExtendDAOInstance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCatalogIcon() {
		// TODO Auto-generated method stub
		return "cxdata/images/cxdata.gif";
	}

	@Override
	public String getCatalogName() {
		// TODO Auto-generated method stub
		return CATALOG_NAME;
	}

	@Override
	public String getDataImpIdKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDetailIcon() {
		// TODO Auto-generated method stub
		 return "cxdata/images/cxdata.gif";
	}

	@Override
	public String getDetailTemplateTypeID() {
		// TODO Auto-generated method stub
		return "CXDataDetail";
	}

	@Override
	public String getEditorURL() {
		// TODO Auto-generated method stub
		return "cxdata/cxDataQuickEditor.zhtml?CatalogID={CatalogID}&ContentID={ID}";
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
		return "CXDataList";
	}

	@Override
	public String getListURL() {
		// TODO Auto-generated method stub
		return "cxdata/cxDataList.zhtml";
	}

	@Override
	public String getQuickEditURL() {
		// TODO Auto-generated method stub
		return "test/cxDataQuickEditor.zhtml?CatalogID={CatalogID}&ContentID={ID}";
	}

	@Override
	public boolean hasExtendTable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IContent newContent() {
		// TODO Auto-generated method stub
		return new CXDataContent();
	}

}
