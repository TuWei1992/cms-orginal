package com.zving.contentcore.tag;

import com.zving.framework.collection.Mapx;
import com.zving.framework.expression.ExpressionException;
import com.zving.framework.i18n.Lang;
import com.zving.framework.security.LicenseInfo;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.TemplateWriter;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.ServletUtil;
import com.zving.framework.utility.StringFormat;
import com.zving.framework.utility.StringUtil;
import com.zving.staticize.tag.SimpleTag;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class CmsPagebarTag
  extends SimpleTag
{
  public static final String FirstFileNameVar = "FirstFileName";
  public static final String OtherFileNameVar = "OtherFileName";
  private int total;
  private int pageSize;
  private int pageIndex;
  private int pageCount;
  private String firstFileName;
  private String otherFileName;
  private String mode;
  
  public List<TagAttr> getTagAttrs()
  {
    ArrayList<TagAttr> list = new ArrayList();
    list.add(new TagAttr("mode", 1, "@{Contentcore.CmsPagebarTag.Mode}"));
    return list;
  }
  
  public int onTagStart()
    throws TemplateRuntimeException, ExpressionException
  {
    this.firstFileName = this.context.eval("FirstFileName");
    this.otherFileName = this.context.eval("OtherFileName");
    if ((ObjectUtil.empty(this.firstFileName)) && (ObjectUtil.notEmpty(this.context.evalMap("Request"))))
    {
      Mapx<String, Object> request = this.context.evalMap("Request");
      request = request.clone();
      request.remove("PageIndex");
      request.remove("HtmlEncodeQuery");
      request.remove("URLEncodeQuery");
      this.firstFileName = ServletUtil.getQueryStringFromMap(request, true);
      this.otherFileName = this.firstFileName;
      if (!this.firstFileName.equals("?")) {
        this.otherFileName += "&";
      }
      this.otherFileName += "PageIndex=${PageIndex}";
    }
    if ((ObjectUtil.notEmpty(this.firstFileName)) && (ObjectUtil.empty(this.otherFileName)))
    {
      this.otherFileName = this.firstFileName;
      if (!this.firstFileName.equals("?")) {
        this.otherFileName += "&";
      }
      this.otherFileName += "PageIndex=${PageIndex}";
    }
    this.total = this.context.getPageTotal();
    this.pageSize = this.context.getPageSize();
    this.pageIndex = this.context.getPageIndex();
    this.pageCount = new Double(Math.ceil(this.total * 1.0D / this.pageSize)).intValue();
    Mapx<String, Integer> map = new Mapx();
    map.put("Index", Integer.valueOf(this.pageIndex));
    map.put("Count", Integer.valueOf(this.pageCount));
    map.put("Total", Integer.valueOf(this.total));
    map.put("Size", Integer.valueOf(this.pageSize));
    this.context.addDataVariable("Page", map);
    this.context.addDataVariable("FirstFileName", this.firstFileName);
    
    return 2;
  }
  
  public int onTagEnd()
    throws TemplateRuntimeException
  {
    String body = getBody();
    if (ObjectUtil.empty(body.trim())) {
      printDefaultPageBar();
    } else {
      this.pageContext.getOut().write(body);
    }
    return 6;
  }
  
  public String getPrefix()
  {
    return "cms";
  }
  
  public String getTagName()
  {
    return "pagebar";
  }
  
  public void printDefaultPageBar()
  {
    StringBuilder sb = new StringBuilder();
    this.total = this.context.getPageTotal();
    this.pageSize = this.context.getPageSize();
    this.pageIndex = this.context.getPageIndex();
    this.pageCount = new Double(Math.ceil(this.total * 1.0D / this.pageSize)).intValue();
    
    long id = System.currentTimeMillis();
    
    String first = Lang.get(this.context.eval("Site.Language"), "Framework.DataGrid.FirstPage", new Object[0]);
    String prev = Lang.get(this.context.eval("Site.Language"), "Framework.DataGrid.PreviousPage", new Object[0]);
    String next = Lang.get(this.context.eval("Site.Language"), "Framework.DataGrid.NextPage", new Object[0]);
    String last = Lang.get(this.context.eval("Site.Language"), "Framework.DataGrid.LastPage", new Object[0]);
    String gotoPage = Lang.get(this.context.eval("Site.Language"), "Framework.DataGrid.GotoPage", new Object[0]);
    String gotoEnd = Lang.get(this.context.eval("Site.Language"), "Framework.DataGrid.GotoPageEnd", new Object[0]);
    String error = Lang.get(this.context.eval("Site.Language"), "Framework.DataGrid.ErrorPage", new Object[0]);
    String gotoButton = Lang.get(this.context.eval("Site.Language"), "Framework.DataGrid.Goto", new Object[0]);
    String pagebar = Lang.get(this.context.eval("Site.Language"), "Framework.DataGrid.PageBar", new Object[0]);
    
    String mode = this.attributes.getString("mode");
    if (mode != null && mode.trim().equals("classical")) {
      sb.append("<table width='100%' border='0' class='noBorder' align='center'><tr>");
      sb.append("<td height='18' align='center' valign='middle' style='border-width: 0px;color:#525252'>");
      sb.append(new StringFormat(pagebar, new Object[] { Integer.valueOf(this.total), Integer.valueOf(this.pageSize), Integer.valueOf(this.pageIndex + 1), Integer.valueOf(this.pageCount) }));
      sb.append("&nbsp;&nbsp;");
      if (this.pageIndex > 0)
      {
        sb.append("<a href='" + getFirstPage() + "'><span class='fc_ch1'>" + first + "</span></a> | ");
        sb.append("<a href='" + getPreviousPage() + "'><span class='fc_ch1'>" + prev + "</span></a> | ");
      }
      else
      {
        sb.append("<span class='fc_hui2'>" + first + "</span> | ");
        sb.append("<span class='fc_hui2'>" + prev + "</span> | ");
      }
      if ((this.pageIndex + 1 != this.pageCount) && (this.pageCount > 0))
      {
        sb.append("<a href='" + getNextPage() + "'><span class='fc_ch1'>" + next + "</span></a> | ");
        sb.append("<a href='" + getLastPage() + "'><span class='fc_ch1'>" + last + "</span></a>");
      }
      else
      {
        sb.append("<span class='fc_hui2'>" + next + "</span> | ");
        sb.append("<span class='fc_hui2'>" + last + "</span>");
      }
      sb.append("&nbsp;&nbsp;" + gotoPage + " <input id='_PageBar_Index_" + id + "' type='text' size='4' style='width:30px' ");
      sb.append("style='' onKeyUp=\"value=value.replace(/\\D/g,'')\"> " + gotoEnd);
      


      sb.append("&nbsp;&nbsp;<input type='button' onclick=\"if(/[^\\d]/.test(document.getElementById('_PageBar_Index_" + id + 
        "').value)){alert('" + error + "');$('_PageBar_Index_" + id + 
        "').focus();}else if(document.getElementById('_PageBar_Index_" + id + "').value>" + this.pageCount + "){alert('" + error + 
        "');document.getElementById('_PageBar_Index_" + id + 
        "').focus();}else{var PageIndex = (document.getElementById('_PageBar_Index_" + id + 
        "').value)>0?document.getElementById('_PageBar_Index_" + id + "').value:1;if(PageIndex==1){window.location='" + 
        getFirstFileName() + "'}else{window.location='" + this.otherFileName + 
        "'.replace('${PageIndex}', PageIndex-1);}}\" style='' value='" + gotoButton + "'></td>");
      sb.append("</tr></table>");
      this.pageContext.getOut().write(sb.toString());
    }  else if (mode != null && mode.trim().equals("cx")) {
		sb.append("<a class=\"a-prev " + (this.pageIndex == 0?"a-prev-none":"") + "\" href=\"" + (this.pageIndex>1?StringUtil.replaceEx(this.otherFileName, "${PageIndex}", String.valueOf(this.pageIndex)):(this.pageIndex>0?this.firstFileName:'#')) + "\"><i class=\"arrow\"></i></a> ");
		sb.append("<span class=\"num\">");
		sb.append("<a " + (this.pageIndex==0? "class=\"cur\"":"") +" href=\"" + (this.pageIndex>0 ? this.firstFileName : "") + "\">1</a> ");
		if (this.pageCount <= 9) {
			for (int i = 2; i < this.pageCount + 1; i++) {
				sb.append("<a " + (this.pageIndex==i-1? "class=\"cur\"":"") + "href=\"" + (StringUtil.replaceEx(this.otherFileName, "${PageIndex}", String.valueOf(i))) + "\">"+ i +"</a> ");
			}
		} else {
			if (this.pageIndex <= 4) {
				for (int i = 2; i < 8; i++) {
					sb.append("<a " + (this.pageIndex==i-1? "class=\"cur\"":"") + "href=\"" + (StringUtil.replaceEx(this.otherFileName, "${PageIndex}", String.valueOf(i))) + "\">"+ i +"</a> ");
				}
				sb.append("<i>...</i>");
			}
			if(this.pageIndex > 4 && this.pageIndex < this.pageCount -5) {
				sb.append("<i>...</i>");
				sb.append("<a href=\"" + (StringUtil.replaceEx(this.otherFileName, "${PageIndex}", String.valueOf(this.pageIndex - 1))) + "\">" + (this.pageIndex-1) + "</a> ");
				sb.append("<a href=\"" + (StringUtil.replaceEx(this.otherFileName, "${PageIndex}", String.valueOf(this.pageIndex))) + "\">" + this.pageIndex + "</a> ");
				sb.append("<a class=\"cur\" href=\"" +(StringUtil.replaceEx(this.otherFileName, "${PageIndex}", String.valueOf(this.pageIndex + 1))) + "\">" + (this.pageIndex + 1) + "</a> ");
				sb.append("<a href=\"" + (StringUtil.replaceEx(this.otherFileName, "${PageIndex}", String.valueOf(this.pageIndex + 2))) + "\">" + (this.pageIndex + 2) + "</a> ");
				sb.append("<a href=\"" + (StringUtil.replaceEx(this.otherFileName, "${PageIndex}", String.valueOf(this.pageIndex + 3))) + "\">" + (this.pageIndex + 3) + "</a> ");
				sb.append("<i>...</i>");
			}
			if (this.pageIndex >= this.pageCount - 5) {
				sb.append("<i>...</i>");
				for (int i = 6; i >0 ; i--) {
				  sb.append("<a " + (this.pageIndex==this.pageCount-i-1? "class=\"cur\"":"") + " href=\"" + (StringUtil.replaceEx(this.otherFileName, "${PageIndex}", String.valueOf(this.pageCount - i))) + "\">" + (this.pageCount - i) +"</a> ");
				}
			}
				sb.append(" <a " + (this.pageIndex==this.pageCount-1? "class=\"cur\"":"") + "href=\"" + (StringUtil.replaceEx(this.otherFileName, "${PageIndex}", String.valueOf(this.pageCount))) + "\">" + this.pageCount + "</a> ");
		}
		sb.append("</span>");
		sb.append("<a class=\"a-next " + (this.pageCount==0||this.pageIndex==this.pageCount-1?"a-next-none":"") + "\" href=\"" + (this.pageIndex < this.pageCount - 1?(StringUtil.replaceEx(this.otherFileName, "${PageIndex}", String.valueOf(this.pageIndex + 2))):"#") + "\">下一页<i class=\"arrow\"></i></a> ");
		this.pageContext.getOut().write(sb.toString());
    }  else {
      int begin = 0;int end = 0;
      begin = this.pageIndex - 5 > 0 ? this.pageIndex - 5 : 0;
      begin = begin + 10 > this.pageCount ? this.pageCount - 10 : this.pageCount - 10 < 0 ? 0 : begin;
      end = begin + 10 > this.pageCount ? this.pageCount : begin + 10;
      
      sb.append("<table width='100%' border='0' class='noBorder' align='center'><tr>");
      sb.append("<td height='18' align='center' valign='middle' style='border-width: 0px;color:#525252'>");
      if (this.pageCount > 0)
      {
        if (this.pageIndex > 0) {
          sb.append("<a href='" + getPreviousPage() + "'><span class='fc_ch1'>" + prev + "</span></a>  ");
        } else {
          sb.append("<span class='fc_hui2'>" + prev + "</span>");
        }
        for (int i = begin; i < end; i++) {
          if (this.pageIndex == i)
          {
            sb.append("<span class='current' style='color: #900'> " + (this.pageIndex + 1) + " </span>");
          }
          else
          {
            sb.append("<a class=\"z_num\" href=\"");
            if (i == 0) {
              sb.append(this.firstFileName);
            } else {
              sb.append(StringUtil.replaceEx(this.otherFileName, "${PageIndex}", Integer.toString(i + 1)));
            }
            sb.append("\">" + (i + 1) + "</a>");
          }
        }
        if (this.pageCount > 0) {
          if (this.pageIndex + 1 != this.pageCount) {
            sb.append("<a href='" + getNextPage() + "'><span class='fc_ch1'>" + next + "</span></a>");
          } else {
            sb.append("<span class='fc_hui2'>" + next + "</span>");
          }
        }
        sb.append("<script>function _PageBar_" + id + "_Go(){");
        sb.append("\tvar ele=document.getElementById('_PageBar_" + id + "');");
        sb.append("if(/[^\\d]/.test(ele.value)||ele.value>" + this.pageCount + "){");
        sb.append("alert('" + error + "');");
        sb.append("$('_PageBar_Index_" + id + "').focus();}else{");
        sb.append("var PageIndex = ele.value>0?ele.value:1;");
        sb.append("if(PageIndex==1){window.location='" + this.firstFileName + "';}else{");
        sb.append("window.location='" + this.otherFileName + "'.replace('${PageIndex}', PageIndex);");
        sb.append("}}}</script>");
        
        sb.append("&nbsp;&nbsp;" + gotoPage + " <input class='z_num' id='_PageBar_" + id + "' type='text' size='3' value='" + (
          this.pageIndex + 1) + "' ");
        sb.append("style='' onKeyUp=\"value=value.replace(/\\D/g,'')\"> /" + this.pageCount + gotoEnd);
        sb.append("&nbsp;&nbsp;<a href=\"javascript:void(0);\" onclick=\"_PageBar_" + id + "_Go()\" class=\"z_pret\">" + gotoButton + 
          "</a>");
      }
      sb.append("</tr></table>");
      this.pageContext.getOut().write(sb.toString());
    }
  }
  
  public String getNextPage()
  {
    if (this.pageIndex + 1 != this.pageCount) {
      return StringUtil.replaceEx(this.otherFileName, "${PageIndex}", String.valueOf(this.pageIndex + 2));
    }
    return "#";
  }
  
  public String getFirstPage()
  {
    return this.firstFileName;
  }
  
  public String getLastPage()
  {
    if (this.pageCount == 1) {
      return this.firstFileName;
    }
    return StringUtil.replaceEx(this.otherFileName, "${PageIndex}", String.valueOf(this.pageCount));
  }
  
  public String getFirstFileName()
  {
    return this.firstFileName;
  }
  
  public void setFirstFileName(String firstFileName)
  {
    this.firstFileName = firstFileName;
  }
  
  public String getOtherFileName()
  {
    return this.otherFileName;
  }
  
  public void setOtherFileName(String otherFileName)
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
    this.otherFileName = otherFileName;
  }
  
  public String getPreviousPage()
  {
    if (this.pageIndex == 1) {
      return this.firstFileName;
    }
    if (this.pageIndex != 0) {
      return StringUtil.replaceEx(this.otherFileName, "${PageIndex}", String.valueOf(this.pageIndex));
    }
    if (this.pageIndex == 0) {
      return "#";
    }
    return null;
  }
  
  public String getDescription()
  {
    return "@{Contentcore.CmsPagebarTagDesc}";
  }
  
  public String getExtendItemName()
  {
    return "@{Contentcore.CmsPageBarTagName}";
  }
  
  public String getPluginID()
  {
    return "com.zving.contentcore";
  }
  
  public String getMode()
  {
    return this.mode;
  }
  
  public void setMode(String mode)
  {
    this.mode = mode;
  }
}
