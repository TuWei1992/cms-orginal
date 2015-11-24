package com.zving.test.tag;

import com.zving.contentcore.tag.AbstractContentListTag;
import com.zving.contentextend.bl.ContentExtendBL;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.media.bl.AudioBL;
import com.zving.test.BookContentType;
import com.zving.test.TestPlugin;

public class CmsBookTag
  extends AbstractContentListTag
{
  public boolean isEditEnable()
  {
    return true;
  }
  
  public String getPrefix()
  {
    return "cms";
  }
  
  public String getTagName()
  {
    return "book";
  }
  
  public String getDescription()
  {
    return "书籍标签";
  }
  
  public String getExtendItemName()
  {
    return "书籍标签";
  }
  
  public String getPluginID()
  {
    return TestPlugin.ID;
  }
  
  public String getEditURL()
  {
    return "test/cmsBookTag.zhtml";
  }
  
  public Q loadContentQueryBuilder()
  {
    return new Q("select a.*,b.ISBN,b.Category,b.PublishTime from ZCContent a,Book b where a.SiteID=? and a.ID=b.ID", new Object[] {
    
      Long.valueOf(this.siteID) });
  }
  
  public String getItemName()
  {
    return "Book";
  }
  
  public String getContentType()
  {
    return BookContentType.ID;
  }
  
  public void invokeTagSqlExtend(AbstractExecuteContext context, Q qb)
  {
    ExtendManager.invoke("com.zving.test.extend.BookTagSqlExtend", new Object[] { context, this, qb });
  }
  
  public void dealExtend()
  {
    ContentExtendBL.dealContentExtendDataForAllCatalogs(this.data);
  }
  
  public void dealContent()
  {
    if (!this.data.containsColumn("Name")) {
      this.data.insertColumn("Name", this.data.getColumnValues("Title"));
    }
  }
  
  public void dealLink(DataRow dr)
  {
    dr.fill(AudioBL.dealCopyAudio(dr.toMapx(), true));
  }
}


/* Location:           D:\work\workspace\zcms22426\UI\WEB-INF\plugins\lib\com.zving.media.plugin.jar
 * Qualified Name:     com.zving.media.tag.CmsAudioTag
 * JD-Core Version:    0.7.0.1
 */