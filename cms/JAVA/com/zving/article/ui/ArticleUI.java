package com.zving.article.ui;

import java.io.File;
import java.util.Date;

import com.zving.article.bl.ArticleBL;
import com.zving.article.bl.OfficeImporter;
import com.zving.article.property.ArticleImageHeight;
import com.zving.article.property.ArticleImageWidth;
import com.zving.article.property.ArticleUEEditorImportCSS;
import com.zving.contentcore.bl.ContentBL;
import com.zving.contentcore.bl.ContentLogBL;
import com.zving.contentcore.bl.SiteBL;
import com.zving.contentcore.code.ResourceAudioAllowType;
import com.zving.contentcore.property.PropertyUtil;
import com.zving.contentcore.resource.ResourceUtil;
import com.zving.contentcore.ui.contentEditor.ContentEditorUI;
import com.zving.contentcore.util.CatalogUtil;
import com.zving.contentcore.util.SiteUtil;
import com.zving.framework.Config;
import com.zving.framework.Current;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.annotation.Verify;
import com.zving.framework.i18n.Lang;
import com.zving.framework.json.JSONArray;
import com.zving.framework.thirdparty.commons.fileupload.FileItem;
import com.zving.framework.ui.control.UploadAction;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.code.YesOrNo;
import com.zving.platform.util.OrderUtil;
import com.zving.schema.ZCCatalog;
import com.zving.schema.ZCContent;
import com.zving.schema.ZCSite;
import com.zving.search.index.IndexUtil;
import com.zving.wordmanage.bl.BadWordBL;

@Alias("Article")
public class ArticleUI
  extends UIFacade
{
  @Priv
  public void initQuickEditor()
  {
    $S("AllowAudioType", ResourceAudioAllowType.getString());
    if (StringUtil.isEmpty($V("CatalogID"))) {
      return;
    }
    ZCCatalog c = CatalogUtil.getDAO($V("CatalogID"));
    $S("ArticleFileDownloadPath", PropertyUtil.getValue(c.getConfigProps(), "ArticleFileDownloadPath"));
    
    $S("ArticleImageWidth", Integer.valueOf(ArticleImageWidth.getValue(c.getConfigProps())));
    $S("ArticleImageHeight", Integer.valueOf(ArticleImageHeight.getValue(c.getConfigProps())));
    if (StringUtil.isNotEmpty(ArticleUEEditorImportCSS.getValue(c.getConfigProps()))) {
      $S("ArticleUEEditorImportCSS", 
        SiteBL.getPreviewPrefix(c.getSiteID()).substring(0, SiteBL.getPreviewPrefix(c.getSiteID()).length() - 1) + 
        ArticleUEEditorImportCSS.getValue(c.getConfigProps()));
    }
    $S("TopFlag", "N");
    
    ZCSite site = SiteUtil.getDAO(c.getSiteID());
    String openArticleAutoSave = PropertyUtil.getValue(site.getConfigProps(), "ArticleAutoSave");
    $S("OpenArticleAutoSave", openArticleAutoSave);
    
    String articleImageWhetherOriginal = PropertyUtil.getValue(site.getConfigProps(), "ArticleImageWhetherOriginal");
    $S("ArticleImageWhetherOriginal", articleImageWhetherOriginal);
    if (ObjectUtil.empty($V("ContentID")))
    {
      new ContentEditorUI().init();
      $S("ContentType", "Article");
      return;
    }
    new ContentEditorUI().init();
    String bodyText = this.Response.getString("BodyText");
    if ((StringUtil.isNotEmpty(bodyText)) && 
      (bodyText.indexOf("<!--_ZVING_PAGE_BREAK_-->") <= -1))
    {
      String ptID = "pc";
      bodyText = ArticleBL.dealArticleAutoPaged(bodyText, c.getSiteID(), ptID);
    }
    if (this.Response.getLong("TopFlag") > 0L) {
      $S("TopFlag", "Y");
    }
    boolean linkFlag = YesOrNo.isYes(this.Response.getString("LinkFlag"));
    if (!linkFlag)
    {
      bodyText = StringUtil.replaceEx(bodyText, "{ContextPath}", Config.getContextPath());
      
      bodyText = ArticleBL.dealContent(bodyText, $L("ContentID"));
    }
    $S("BodyText", bodyText);
  }
  
  @Priv
  public void getThumbImage()
  {
    JSONArray imagePaths = this.Request.getJSONArray("imagePaths");
    for (int i = 0; i < imagePaths.length(); i++) {
      imagePaths.set(i, ResourceUtil.getResourceLogoFile("image", imagePaths.getString(i), 
        SiteBL.getCurrentSite(), $I("ImageWidth"), $I("ImageHeight")));
    }
    this.Response.put("imageLinks", imagePaths);
  }
  
  @Priv
  @Verify(ignoreAll=true)
  public void checkBadWord()
  {
    String contents = $V("BodyText");
    if (StringUtil.isNotNull(contents)) {
      contents = IndexUtil.getTextFromHtml(contents);
    }
    String badWords = BadWordBL.checkBadWord(contents, SiteBL.getCurrentSite(), $V("Priority"));
    if ((badWords != null) && (badWords.length() > 0))
    {
      $S("HasBadWord", Boolean.valueOf(true));
      fail(badWords);
      return;
    }
    success(Lang.get("Common.ExecuteSuccess", new Object[0]));
  }
  
  @Priv("com.zving.cms.Catalog.Content.Add.${CatalogID}")
  @Verify(ignoredKeys="BodyText")
  public void insert()
  {
    this.Request.put("BodyText", $V("_Contents"));
    this.Request.remove("_Contents");
    save();
  }
  
  @Priv("com.zving.cms.Catalog.Content.Add.${CatalogID}||com.zving.cms.Catalog.Content.Edit.${CatalogID}")
  @Verify(ignoredKeys="BodyText")
  public void save()
  {
    long articleID = $L("ContentID");
    long siteID = $L("SiteID");
    if (articleID == 0L)
    {
      fail(Lang.get("Common.InvalidID", new Object[0]));
      return;
    }
    String topFlag = $V("TopFlag");
    ZCContent c = new ZCContent();
    c.setID(articleID);
    c.fill();
    if ((StringUtil.isNotEmpty(topFlag)) && (topFlag.equals($V("OldTopFlag")))) {
      this.Request.remove("TopFlag");
    } else if (YesOrNo.isYes(topFlag)) {
		String topDate = $V("TopDate");
	    Date date = null;
	    if (DateUtil.isDateTime(topDate)) {
	      date = DateUtil.parseDateTime(topDate);
	    }
	    if ((date != null) && (date.before(new Date())))
	    {
	      fail(Lang.get("Article.VerifyTopEndTime", new Object[0]));
	      return;
	    }
	    this.Request.put("TopFlag", System.currentTimeMillis());
	    this.Request.put("TopDate", date);
	    if (c.getTopFlag()<=0) {
	    	this.Request.put("OrderFlag", c.getOrderFlag() * 100);
	    }
    } else {
      this.Request.put("TopFlag", Integer.valueOf(0));
      	if(c.getTopFlag() > 0) {
    	  this.Request.put("OrderFlag", c.getOrderFlag() / 100);
      }
    }
    if ((StringUtil.isNotEmpty($V("LinkFlag"))) && ($V("LinkFlag").equals("Y"))) {
      this.Request.put("BodyText", $V("RedirectURL"));
    } else if (YesOrNo.isYes($V("ReplaceBadWord"))) {
      this.Request.put("BodyText", BadWordBL.badwordDeal($V("BodyText"), siteID, $V("Priority")));
    }
    new ContentEditorUI().save();
  }
  
  @Priv("com.zving.cms.Catalog.Content.Edit.${CatalogID}")
  @Verify(ignoredKeys="BodyText")
  public void quickEditSave()
  {
    new ContentEditorUI().save();
  }
  
  @Priv("com.zving.cms.Catalog.Content.Add.${CatalogID}||com.zving.cms.Catalog.Content.Edit.${CatalogID}")
  @Verify(ignoreAll=true)
  public void getKeywordOrSummary()
  {
    String type = $V("Type");
    String title = $V("Title");
    String content = $V("Content");
    String text = "";
    if ("Keyword".equals(type))
    {
      if (YesOrNo.isYes($V("LinkFlag"))) {
        content = "";
      }
      text = ArticleBL.getKeyword(content, title);
    }
    else
    {
      if (YesOrNo.isYes($V("LinkFlag"))) {
        content = title;
      }
      text = ArticleBL.getSummary(content, title);
      if ((StringUtil.isNotEmpty(text)) && (text.length() > 500)) {
        text = text.substring(0, 500);
      }
    }
    $S("Text", StringUtil.isNull(text) ? "" : text);
  }
  
  @Priv("com.zving.cms.Catalog.Content.Add.${CatalogID}")
  public void wordImport(UploadAction ua)
  {
    if ((ua.getAllFiles() == null) || (ObjectUtil.empty($V("CatalogID"))))
    {
      fail(Lang.get("Article.UploadFile.MissMsg", new Object[0]));
      return;
    }
    long catalogID = $L("CatalogID");
    String dirPath = SiteUtil.getSiteRoot(SiteBL.getCurrentSite()) + "upload/_wordimport/";
    for (FileItem fi : ua.getAllFiles())
    {
      try
      {
        String title = fi.getName().substring(0, fi.getName().lastIndexOf("."));
        int count = ContentBL.sameTitleCount(title, catalogID, SiteBL.getCurrentSite(), 0L);
        if (count > 0)
        {
          fail(Lang.get("Content.TitleExists", new Object[0]));
          return;
        }
        if (!new File(dirPath).exists()) {
          FileUtil.mkdir(dirPath);
        }
        File in = new File(dirPath + OrderUtil.getDefaultOrder() + "." + FileUtil.getExtension(fi.getName()));
        fi.write(in);
        
        OfficeImporter importer = new OfficeImporter(in);
        importer.extractHtml();
        importer.importArticle(catalogID, title);
        
        ContentLogBL.success(importer.getContent().getContentDAO(), "add", "@{Contentcore.Import}");
      }
      catch (Exception e)
      {
        e.printStackTrace();
        fail(e.getMessage());
        return;
      }
      finally
      {
        FileUtil.deleteFromDir(dirPath);
      }
      FileUtil.deleteFromDir(dirPath);
    }
    if (Current.getTransaction().commit()) {
      success(Lang.get("Common.ExecuteSuccess", new Object[0]));
    } else {
      fail(Lang.get("Common.ExecuteFailed", new Object[0]));
    }
  }
}
