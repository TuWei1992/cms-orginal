package com.zving.test.template;

import com.zving.contentcore.template.types.AbstractCatalogListTemplate;
import com.zving.test.TestPlugin;

public class BookListTemplate
  extends AbstractCatalogListTemplate
{
  public static final String ID = "BookList";
  
  public String getExtendItemID()
  {
    return ID;
  }
  
  public String getExtendItemName()
  {
    return "书籍列表";
  }
  
  public String getPluginID()
  {
    return TestPlugin.ID;
  }
}


/* Location:           D:\work\workspace\zcms22426\UI\WEB-INF\plugins\lib\com.zving.media.plugin.jar
 * Qualified Name:     com.zving.media.template.AudioListTemplate
 * JD-Core Version:    0.7.0.1
 */