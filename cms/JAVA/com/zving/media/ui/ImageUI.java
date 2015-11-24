package com.zving.media.ui;

import com.zving.contentcore.IContent;
import com.zving.contentcore.IContentType;
import com.zving.contentcore.bl.SiteBL;
import com.zving.contentcore.config.AllowedUploadFileType;
import com.zving.contentcore.resource.ResourceBL;
import com.zving.contentcore.resource.ResourceManager;
import com.zving.contentcore.resource.ResourceRelaBL;
import com.zving.contentcore.service.ContentTypeService;
import com.zving.contentcore.ui.contentEditor.ContentEditorUI;
import com.zving.contentcore.util.CatalogUtil;
import com.zving.contentcore.util.InternalURLUtil;
import com.zving.contentcore.util.InternalURLUtil.InternalURL;
import com.zving.contentcore.util.SiteUtil;
import com.zving.framework.Current;
import com.zving.framework.RequestData;
import com.zving.framework.UIFacade;
import com.zving.framework.User;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.i18n.Lang;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.media.ImageUtil;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.orm.DAOUtil;
import com.zving.framework.security.PrivCheck;
import com.zving.framework.thirdparty.commons.fileupload.FileItem;
import com.zving.framework.ui.control.UploadAction;
import com.zving.framework.utility.Errorx;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.RarUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.framework.utility.ZipUtil;
import com.zving.media.bl.ImageBL;
import com.zving.media.util.Image;
import com.zving.schema.ZCCatalog;
import com.zving.schema.ZCContent;
import com.zving.schema.ZCImage;
import com.zving.schema.ZCResources;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

@Alias("ImageUpload")
public class ImageUI
  extends UIFacade
{
  public static long index = System.currentTimeMillis();
  
  @Priv
  public Mapx<String, Object> init()
  {
    long id = $L("ID");
    if (id != 0L)
    {
      ZCImage image = new ZCImage();
      image.setID(id);
      if (image.fill())
      {
        this.Request.putAll(image.toMapx());
        $S("Prefix", SiteUtil.getPreviewPrefix(SiteBL.getCurrentSite()));
        $S("Spath", ImageBL.getImageThumbnailSrc(image));
        if (InternalURLUtil.isInteralURL(image.getSourceURL()))
        {
          long contentID = InternalURLUtil.parseInteralURL(image.getSourceURL()).getContentID();
          long catID = InternalURLUtil.parseInteralURL(image.getSourceURL()).getCatalogID();
          String type = InternalURLUtil.parseInteralURL(image.getSourceURL()).getContentType();
          if (ObjectUtil.notEmpty(Long.valueOf(contentID)))
          {
            IContentType contype = ContentTypeService.getContentType(type);
            IContent con = contype.loadContent(Long.valueOf(contentID).longValue());
            if (ObjectUtil.notEmpty(con.getTitle())) {
              $S("CName", LangUtil.decode(contype.getExtendItemName()) + "：" + con.getTitle());
            } else {
              $S("CName", LangUtil.decode(contype.getExtendItemName()) + "：" + Lang.get("ContentCore.ContentNotFound", new Object[0]));
            }
          }
          else
          {
            ZCCatalog catalog = CatalogUtil.getDAO(catID);
            if ((catalog != null) && (catalog.fill())) {
              $S("CName", Lang.get("Contentcore.Catalog", new Object[0]) + "：" + catalog.getName());
            } else {
              $S("CName", Lang.get("Contentcore.Catalog", new Object[0]) + "：" + Lang.get("Contentcore.NotFindCatalog", new Object[0]));
            }
          }
        }
        else
        {
          $S("CName", image.getSourceURL());
        }
      }
    }
    return this.Request;
  }
  
  @Priv("com.zving.cms.Catalog.Content.Edit.${CatalogID}||com.zving.cms.Catalog.Content.Add.${CatalogID}")
  public void upload(UploadAction ua)
  {
    Image image = new Image();
    ZCImage dao = new ZCImage();
    image.setProperties(this.Request);
    image.setTransaction(Current.getTransaction());
    ArrayList<FileItem> list = ua.getAllFiles();
    if ((list != null) && (list.size() > 1))
    {
      Collections.sort(list, new Comparator<FileItem>()
      {
        public int compare(FileItem o1, FileItem o2)
        {
          return o2.getName().compareTo(o1.getName());
        }
      });
      for (FileItem fi : list) {
        if (fi != null)
        {
          image.setProperty("FileItem", fi);
          image.setProperty("Name", fi.getName().substring(0, fi.getName().lastIndexOf(".")));
          image.insert();
        }
      }
    }
    else
    {
      image.setProperty("FileItem", ua.getFirstFile());
      if (ua.getFirstFile() != null) {
        if (StringUtil.isNull($V("Name"))) {
          image.setProperty("Name", ua.getFirstFile().getName().substring(0, ua.getFirstFile().getName().lastIndexOf(".")));
        } else {
          image.setProperty("Name", $V("Name"));
        }
      }
      if ($L("ID") == 0L)
      {
        if (ua.getFirstFile() == null)
        {
          fail(Lang.get("Media.UploadFileMiss", new Object[0]));
          return;
        }
        image.insert();
      }
      else
      {
        dao.setID($L("ID"));
        if (!dao.fill())
        {
          fail(Lang.get("Common.InvalidID", new Object[0]));
          return;
        }
        image.save();
      }
    }
    if (Errorx.hasError())
    {
      fail(Errorx.getAllMessage());
      return;
    }

    updateContent();
    if (Current.getTransaction().commit()) {
      success(Lang.get("Common.ExecuteSuccess", new Object[0]));
    } else {
      fail(Lang.get("Common.ExecuteFailed", new Object[0]));
    }
    
  }
  
  @Priv("com.zving.cms.Catalog.Content.Edit.${CatalogID}||com.zving.cms.Catalog.Content.Add.${CatalogID}")
  public void uploadZip(UploadAction ua)
  {
    FileItem fi = ua.getFirstFile();
    if (fi == null)
    {
      fail(Lang.get("Media.UploadFileMiss", new Object[0]));
      return;
    }
    String ext = FileUtil.getExtension(fi.getName());
    if (!AllowedUploadFileType.isAllow(ext))
    {
      fail(Lang.get("Contentcore.ProhibitUploadFile", new Object[0]));
      return;
    }
    ZCContent content = new ZCContent();
    content.setID($L("GroupID"));
    content.fill();
    
    DAOSet<ZCImage> images = new DAOSet();
    DAOSet<ZCResources> resources = new DAOSet();
    String dir = ResourceBL.getResourceRelativeDirectory(content.getSiteID(), "Image", new Date());
    String physicalPath = SiteUtil.getSiteRoot(content.getSiteID()) + dir;
    FileUtil.mkdir(physicalPath);
    String zipFile = physicalPath + String.valueOf(index++) + "_" + ua.getFirstFile().getName();
    ArrayList<String> list;
    try
    {
      ua.getFirstFile().write(new File(zipFile));
      Mapx<String, Long> files = null;
      if (ext.equals("zip")) {
        files = ZipUtil.getFileListInZip(zipFile);
      } else {
        files = RarUtil.getFileListInRar(zipFile);
      }
      list = new ArrayList();
      for (String fileName : files.keySet()) {
        if (AllowedUploadFileType.isAllow(fileName)) {
          list.add(fileName);
        }
      }
      Collections.sort(list, new Comparator<String>()
      {
        public int compare(String o1, String o2)
        {
          return o2.compareTo(o1);
        }
      });
      ResourceManager rm = new ResourceManager(User.getUserName(), Current.getTransaction());
      for (String fileName : list)
      {
        ZCImage image = ImageBL.createImageDAO(fileName, ((Long)files.get(fileName)).longValue(), content);
        if (!ObjectUtil.empty(image))
        {
          Current.getTransaction().insert(image);
          images.add(image);
          
          ZCResources resource = rm.add(fileName, ((Long)files.get(fileName)).longValue(), image.getCatalogID(), image.getSiteID());
          if ("zip".equals(ext)) {
            ResourceBL.addResourceFile(ZipUtil.readFileInZip(zipFile, fileName), resource);
          } else {
            ResourceBL.addResourceFile(RarUtil.readFileInRar(zipFile, fileName), resource);
          }
          ResourceRelaBL.addRelationship(image.getID(), DAOUtil.getTableCode(image), resource, Current.getTransaction());
          String path = resource.getPath();
          image.setPath(path.substring(0, path.lastIndexOf("/") + 1));
          image.setSuffix(FileUtil.getExtension(path));
          image.setFileName(resource.getName());
          
          String imagePath = ImageBL.getImageAbsolutePath(image);
          try
          {
            Dimension dim = ImageUtil.getDimension(imagePath);
            image.setWidth((long)dim.getWidth());
            image.setHeight((long)dim.getHeight());
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      fail(Lang.get("Contentcore.UploadFailureMessage", new Object[0]) + ":" + StringUtil.htmlEncode(e.getMessage()));
    }
    finally
    {
      FileUtil.delete(zipFile);
    }
    if (Current.getTransaction().commit())
    {
      success(Lang.get("Common.ExecuteSuccess", new Object[0]));
    }
    else
    {
      for (ZCImage image : images) {
        FileUtil.delete(ImageBL.getImageAbsolutePath(image));
      }
      for (ZCResources resource : resources) {
        FileUtil.delete(SiteUtil.getSiteRoot(resource.getSiteID()) + resource.getPath());
      }
      fail(Lang.get("Common.ExecuteFailed", new Object[0]));
    }
    updateContent();
  }
  
  private void updateContent() {
    Long groupID = this.Request.getLong("GroupID");
    ZCContent con = new ZCContent();
    con.setID(groupID);
    con.fill();
	this.Request.clear();
    this.Request.put("ContentID", con.getID());
    this.Request.put("CatalogID", con.getCatalogID());
    this.Request.put("SiteID", con.getSiteID());
   
    new ContentEditorUI().save();
	    
  }
  
  @Priv("com.zving.cms.Catalog.Content.Delete.${CatalogID}||com.zving.cms.Catalog.Content.Edit.${CatalogID}")
  public void del()
  {
    String imageIDs = $V("ImageIDs");
    DAOSet<ZCImage> images = new ZCImage().query(new Q().where().in("id", imageIDs));
    for (ZCImage image : images) {
      if ((PrivCheck.check("com.zving.cms.Catalog.Content.Delete." + image.getCatalogID())) || 
        (PrivCheck.check("com.zving.cms.Catalog.Content.Edit." + image.getCatalogID()))) {}
    }
    if (images.deleteAndBackup())
    {
      for (ZCImage image : images) {
        ExtendManager.invoke("com.zving.media.BeforeImageDelete", new Object[] { image });
      }
      success(Lang.get("Common.ExecuteSuccess", new Object[0]));
    }
    else
    {
      fail(Lang.get("Common.ExecuteFailed", new Object[0]));
    }
    updateContent();
  }
  
  @Priv("com.zving.cms.Catalog.Content.Edit.${CatalogID}||com.zving.cms.Catalog.Content.Add.${CatalogID}")
  public void cuttingSave()
  {
    Image image = new Image();
    image.setProperties(this.Request);
    image.setTransaction(Current.getTransaction());
    try
    {
      image.cuttingSave();
    }
    catch (Exception e)
    {
      fail(e.getMessage());
      e.printStackTrace();
    }
    if (Current.getTransaction().commit()) {
      success(Lang.get("Common.ExecuteSuccess", new Object[0]));
    } else {
      fail(Lang.get("Common.ExecuteFailed", new Object[0]));
    }
  }
}
