package com.zving.test.template;

import com.zving.contentcore.template.types.AbstractDetailTemplate;
import com.zving.framework.collection.Mapx;
import com.zving.test.TestPlugin;

public class BookDetailTemplate
  extends AbstractDetailTemplate
{
  public static final String ID = "BookDetail";
  
  public String getExtendItemID()
  {
    return ID;
  }
  
  public String getExtendItemName()
  {
    return "书籍详情";
  }
  
  public String getPluginID()
  {
    return TestPlugin.ID;
  }
  
  public String getVarPrefix(Mapx<String, Object> map)
  {
    return "Book";
  }
}


/* Location:           D:\work\workspace\zcms22426\UI\WEB-INF\plugins\lib\com.zving.media.plugin.jar
 * Qualified Name:     com.zving.media.template.AudioDetailTemplate
 * JD-Core Version:    0.7.0.1
 */