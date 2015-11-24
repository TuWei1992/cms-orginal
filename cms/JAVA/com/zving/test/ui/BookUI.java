package com.zving.test.ui;

import com.zving.contentcore.ui.contentEditor.ContentEditorUI;
import com.zving.contentextend.bl.ContentExtendBL;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.collection.Mapx;
import com.zving.framework.ui.control.UploadAction;
import com.zving.schema.Book;

@Alias("Book")
public class BookUI
  extends UIFacade
{
  @Priv
  public void init()
  {
    long contentid = $L("ContentID");
    new ContentEditorUI().init();
    if (contentid == 0L) {
      return;
    }
    Mapx<String, Object> map = this.Request;
    ContentExtendBL.dealContentExtendData(map);
    this.Response.putAll(map);
    Book book = new Book();
    int copytype = this.Request.getInt("CopyType");
    if (copytype > 1) {
    	book.setID($L("CopyID"));
    } else {
    	book.setID(contentid);
    }
    if (book.fill()) {
      this.Response.putAll(book.toMapx());
    }
  }
  
  @Priv("com.zving.cms.Catalog.Content.Add.${CatalogID}||com.zving.cms.Catalog.Content.Edit.${CatalogID}")
  public void save(UploadAction ua)
  {
    new ContentEditorUI().save();
  }
}


/* Location:           D:\work\workspace\zcms22426\UI\WEB-INF\plugins\lib\com.zving.media.plugin.jar
 * Qualified Name:     com.zving.media.ui.AudioUI
 * JD-Core Version:    0.7.0.1
 */