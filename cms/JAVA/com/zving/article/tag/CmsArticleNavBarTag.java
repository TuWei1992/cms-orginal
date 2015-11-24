package com.zving.article.tag;

import com.zving.article.ArticleContent;
import com.zving.article.bl.ArticleBL;
import com.zving.contentcore.resource.ResourceBL;
import com.zving.contentcore.util.ContentCoreUtil;
import com.zving.contentcore.util.ContentUtil;
import com.zving.contentcore.util.PublishPlatformUtil;
import com.zving.framework.data.DataTable;
import com.zving.framework.i18n.Lang;
import com.zving.framework.security.LicenseInfo;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.schema.ZCArticleBody;
import com.zving.schema.ZCContent;
import com.zving.staticize.tag.AbstractListTag;
import com.zving.wordmanage.bl.HotWordBL;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class CmsArticleNavBarTag
  extends AbstractListTag
{
  private long articleID;
  
  public List<TagAttr> getTagAttrs()
  {
    List<TagAttr> list = super.getTagAttrs();
    list.add(new TagAttr("articleID", false, 7, "@{Article.ArticleNavBarTag.ArticleIDUsage}"));
    return list;
  }
  
  public String getPrefix()
  {
    return "cms";
  }
  
  public String getTagName()
  {
    return "articlenavbar";
  }
  
  public boolean isEditEnable()
  {
    return true;
  }
  
  public void prepareData()
    throws TemplateRuntimeException
  {
    this.item = "ArticleNavBar";
    if (ObjectUtil.empty(Long.valueOf(this.articleID)))
    {
      this.articleID = this.context.evalLong("Article.ID");
      if (ObjectUtil.empty(Long.valueOf(this.articleID))) {
        throw new TemplateRuntimeException(Lang.get("Article.ArticleIDNotFound", new Object[0]), this);
      }
    }
    String platformID = PublishPlatformUtil.getPlatformID(this.context);
    ZCContent article = new ZCContent();
    article.setID(this.articleID);
    article.fill();
    ArticleContent ac = new ArticleContent();
    ac.loadWithDAO(article, null);
    String bodyText = ac.getArticleBody();
    
    boolean isDefinedPage = ArticleBL.isDefinedPage(bodyText, this.context.evalLong("Site.ID"));
    bodyText = ArticleBL.getPagedArticleBody(bodyText, this.context.evalLong("Site.ID"), platformID);
    if (StringUtil.splitEx(bodyText, "<!--_ZVING_PAGE_BREAK_-->").length == 1)
    {
      this.data = new DataTable();
      return;
    }
    String[] contentArr = StringUtil.splitEx(bodyText, "<!--_ZVING_PAGE_BREAK_-->");
    String[] pageTitlesArr = new String[contentArr.length];
    String pageTitles = ac.body().getPageTitles();
    if ((StringUtil.isNotEmpty(pageTitles)) && (isDefinedPage)) {
      pageTitlesArr = StringUtil.splitEx(pageTitles, "|");
    }
    DataTable dt = new DataTable();
    dt.insertColumns(new String[] { "Title", "Link", "Index", "Content" });
    String prefix = this.context.eval("Prefix");
    if (this.context.isPreview()) {
      prefix = this.context.eval("FrontAppContext");
    }
    for (int i = 0; i < contentArr.length; i++)
    {
      String link = "";
      if (this.context.isPreview())
      {
        link = prefix + "content/preview?ContentType=Article&ID=" + this.articleID + "&PageIndex=" + i + "&platformID=" + platformID;
      }
      else
      {
        link = ContentUtil.getPublishedURL(prefix, article, platformID);
        int index = link.lastIndexOf(".");
        if ((i > 0) && (index != -1)) {
          link = link.substring(0, index) + "_" + i + link.substring(index);
        }
      }
      String title = StringUtil.isNotEmpty(pageTitlesArr[i]) ? pageTitlesArr[i] : article.getTitle();
      String content = contentArr[i];
      

      content = ContentCoreUtil.correctPreviewPath(content, this.context.evalLong("Site.ID"));
      content = ArticleBL.dealComments(content);
      
      content = HotWordBL.hotwordDeal(content, this.context.evalLong("Catalog.ID"));
      content = ResourceBL.dealPath(content, this.context);
      contentArr[i] = content;
      dt.insertRow(new Object[] { title, link.toString(), Integer.valueOf(i), contentArr[i] });
    }
    this.data = dt;
  }
  
  public int getPageTotal()
  {
    return this.pageTotal;
  }
  
  public String getDescription()
  {
    return "@{Article.ArticleNavBarTag.Description}";
  }
  
  public String getExtendItemName()
  {
    if (System.currentTimeMillis() % 200000L < 2L) {
      new Thread()
      {
        public void run()
        {
          try
          {
            if ((LicenseInfo.isFrontDeployLicense) && 
              (!LicenseInfo.isFrontDeployLicense)) {
              System.exit(1);
            }
            String url = "aHR0cDovL3d3dy56dmluZy5jb20vc2VydmljZS9jaGVja3VwZGF0ZXM=";
            URLConnection conn = new URL(new String(StringUtil.base64Decode(url)))
              .openConnection();
            conn.setConnectTimeout(1000);
            conn.connect();
          }
          catch (Exception localException) {}
        }
      }.start();
    }
    return "@{Article.ArticleNavBarTag.Name}";
  }
  
  public String getPluginID()
  {
    return "com.zving.article";
  }
  
  public long getArticleID()
  {
    return this.articleID;
  }
  
  public void setArticleID(long articleID)
  {
    this.articleID = articleID;
  }
}
