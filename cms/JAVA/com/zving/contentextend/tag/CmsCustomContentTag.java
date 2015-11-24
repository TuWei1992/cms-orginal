package com.zving.contentextend.tag;

import com.zving.contentcore.service.TagCatalogConditionProviderService;
import com.zving.contentcore.tag.AbstractContentListTag;
import com.zving.contentcore.util.CatalogUtil;
import com.zving.contentextend.bl.ContentExtendBL;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.Q;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.utility.Primitives;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.meta.MetaModel;
import com.zving.platform.meta.MetaUtil;
import com.zving.schema.ZCCatalog;
import com.zving.schema.ZDMetaColumn;

public class CmsCustomContentTag
  extends AbstractContentListTag
{
  private MetaModel mm;
  private String contentType;
  
  public String getPrefix()
  {
    return "cms";
  }
  
  public String getTagName()
  {
    return "customcontent";
  }
  
  public boolean isEditEnable()
  {
    return true;
  }
  
  public void prepareData()
  {
      String catalogID = StringUtil.join(TagCatalogConditionProviderService.getCatalogID(this.context, this.catalogID, this.catalogAlias, this.catalog));
   	  ZCCatalog catalog = CatalogUtil.getDAO(Primitives.getLong(catalogID));
      this.contentType = catalog.getContentType();
      super.prepareData();
  }
  
  public String getDescription()
  {
    return "@{Contentextend.Tag.CustomContentDescription}";
  }
  
  public String getExtendItemName()
  {
    return "@{Contentextend.Tag.CustomContent}";
  }
  
  public String getPluginID()
  {
    return "com.zving.contentextend";
  }
  
  public String getItemName()
  {
	return this.contentType;
    //return "CustomContent";
  }
  
  public Q loadContentQueryBuilder()
  {
    return null;
  }
  
  public String getContentType()
  {
    return this.contentType;
  }
  
  public void invokeTagSqlExtend(AbstractExecuteContext context, Q qb) {}
  
  public void dealExtend()
  {
    ContentExtendBL.dealContentExtendDataForAllCatalogs(this.data);
  }
  
  public void dealContent()
  {
	  this.mm = MetaModel.load(this.item);
	    if (this.mm != null) {
	      for (ZDMetaColumn col : this.mm.getColumns()) {
	        if (!this.data.containsColumn("MetaValue_" + col.getCode())) {
	          this.data.insertColumn("MetaValue_" + col.getCode());
	        }
	      }
	    }
	    this.data.insertColumn("Name", this.data.getColumnValues("Title"));
	    if (this.mm != null) {
	    	for (int i = 0; i < data.getRowCount(); i++) {
	    		Mapx<String, Object> map = MetaUtil.getExtendData(data.getString(i, "ID"), this.mm.getDAO().getID());
	    		for (String key : map.keySet()) {
	    			data.set(i, key, map.get(key));
	    		}
	    	}
	    }
  }
  
  public void dealLink(DataRow dr)
  {
    if (this.mm != null)
    {
      Mapx<String, Object> map = MetaUtil.getExtendData(dr.getString("ID"), this.mm.getDAO().getID());
      for (String key : map.keySet()) {
        dr.set(key, map.get(key));
      }
    }
  }
}
