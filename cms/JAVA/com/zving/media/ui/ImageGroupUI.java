package com.zving.media.ui;

import com.zving.contentcore.bl.SiteBL;
import com.zving.contentcore.resource.ResourceRelaBL;
import com.zving.contentcore.resource.ResourceUtil;
import com.zving.contentcore.ui.contentEditor.ContentEditorUI;
import com.zving.contentcore.util.PublishPlatformUtil;
import com.zving.contentcore.util.SiteUtil;
import com.zving.framework.Current;
import com.zving.framework.RequestData;
import com.zving.framework.UIFacade;
import com.zving.framework.User;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.core.handler.ZAction;
import com.zving.framework.data.Transaction;
import com.zving.framework.i18n.Lang;
import com.zving.framework.security.PrivCheck;
import com.zving.framework.utility.StringUtil;
import com.zving.media.ImageContent;
import com.zving.media.bl.ImageGroupBL;
import com.zving.schema.ZCContent;
import com.zving.schema.ZCImage;
import java.util.Date;

@Alias("ImageGroup")
public class ImageGroupUI extends UIFacade {
  @Priv
  public void init() {
    long contentid = $L("ContentID");
    new ContentEditorUI().init();
    if (contentid == 0L) {
      return;
    }
    $S("SrcFileName", Lang.get("Platform.DataBackup.Upload", new Object[0]));
    boolean hasEditPriv = PrivCheck.check("com.zving.cms.Catalog.Content.Edit." + $V("CatalogID"));
    $S("HasEditPriv", Boolean.valueOf(hasEditPriv));
    $S("AllowType", ResourceUtil.getResourceAllowType("ResourceImageAllowType"));
    $S("Prefix", SiteUtil.getPreviewPrefix(SiteBL.getCurrentSite()));
  }

  @Priv("com.zving.cms.Catalog.Content.Edit.${CatalogID}")
  public void quickEditSave() {
    for (String key : this.Request.keySet()) {
      if (key.startsWith("Info_")) {
        String value = $V(key);
        long id = Long.parseLong(key.split("_")[1]);
        ZCImage schema = new ZCImage();
        schema.setID(id);
        if (schema.fill()) {
          schema.setInfo(value);
        }
        schema.setModifyTime(new Date());
        schema.setModifyUser(User.getUserName());
        Current.getTransaction().add(schema, 2);
      }
      if (key.startsWith("Name_")) {
        String value = $V(key);
        
        //可以为空
        if (value == null) {
        	value = "";
          //fail(Lang.get("Media.ImageNameNotNull", new Object[0]));
          //return;
        }
        long id = Long.parseLong(key.split("_")[1]);
        ZCImage schema = new ZCImage();
        schema.setID(id);
        if (schema.fill()) {
          schema.setName(value);
        }
        schema.setModifyTime(new Date());
        schema.setModifyUser(User.getUserName());
        Current.getTransaction().add(schema, 2);
      }
      
      //zq 2015/05/26 增加SourceURL字段
      if (key.startsWith("SourceURL_")) {
          String value = $V(key);
          long id = Long.parseLong(key.split("_")[1]);
          ZCImage schema = new ZCImage();
          schema.setID(id);
          if (schema.fill()) {
            schema.setSourceURL(value);;
          }
          schema.setModifyTime(new Date());
          schema.setModifyUser(User.getUserName());
          Current.getTransaction().add(schema, 2);
        }
      
    }
    new ContentEditorUI().save();
  }

  @Priv("com.zving.cms.Catalog.Content.Edit.${CatalogID}")
  public void saveLogo() {
    long imagegroupid = $L("GroupID");
    long resourceID = $L("resourceID");
    ZCContent group = new ZCContent();
    group.setID(imagegroupid);
    if (!(group.fill())) {
      fail(Lang.get("Common.InvalidID", new Object[0]));
      return;
    }
    String logoFile = this.Request.getString("LogoFile");
    if (StringUtil.isNull(logoFile)) {
      fail(Lang.get("Contentcore.Catalog.LoseRequiredParams", new Object[0]) + "：LogoFile");
      return;
    }
    group.setLogoFile(logoFile);
    ResourceRelaBL.delAllRelationship(group.getID(), "Image", Current.getTransaction());
    ResourceRelaBL.addRelationship(group.getID(), "Image", resourceID, Current.getTransaction());
    Current.getTransaction().update(group);
    if (Current.getTransaction().commit())
      success(Lang.get("Common.ExecuteSuccess", new Object[0]));
    else
      fail(Lang.get("Common.ExecuteFailed", new Object[0]));
  }

  @Priv
  public void editorPreview(ZAction za)
  {
    long id = $L("ID");
    ImageContent ic = new ImageContent();
    ic.loadWithID(id);

    String platformID = PublishPlatformUtil.getPlatformID(this.Request);
    String content = ImageGroupBL.generateJSContent(ic, platformID, true);
    za.writeJS(content);
  }
}