package com.zving.staticize.tag;

import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataTable;
import com.zving.framework.expression.ExpressionException;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.exception.TemplateRuntimeException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractListTag
  extends SimpleTag
{
  protected DataTable data;
  protected String item;
  protected int count;
  protected int begin;
  protected String condition;
  protected boolean page;
  protected int pageSize;
  protected int size;
  protected int pageTotal;
  
  public List<TagAttr> getTagAttrs()
  {
    ArrayList<TagAttr> list = new ArrayList();
    list.add(new TagAttr("begin", false, 8, "@{Staticize.ListTag.Begin}"));
    
    TagAttr tad = new TagAttr("page", false, 1, "@{Staticize.ListTag.Page}");
    Mapx<String, String> options = new Mapx();
    options.put("true", "true");
    options.put("false", "false");
    tad.setOptions(options);
    list.add(tad);
    
    list.add(new TagAttr("pageSize", 8, "@{Staticize.ListTag.PageSize}"));
    list.add(new TagAttr("count", 8, "@{Staticize.ListTag.Count}"));
    list.add(new TagAttr("condition", 1, "@{Staticize.ListTag.Condition}"));
    return list;
  }
  
  public int onTagStart()
    throws TemplateRuntimeException, ExpressionException
  {
    this.pageSize = (this.pageSize == 0 ? this.size : this.pageSize);
    this.pageSize = (this.pageSize == 0 ? this.context.getPageSize() : this.pageSize);
    if (this.count == 0) {
      this.count = 20;
    }
    if (this.begin > 0) {
      this.begin -= 1;
    }
    this.count += this.begin;
    if (this.pageSize != 0) {
      this.begin = 0;
    } else {
      this.pageSize = 20;
    }
    if (this.page)
    {
      this.context.setPageSize(this.pageSize);
      int pageIndex = this.context.evalInt("Request.PageIndex");
      if (pageIndex > 1) {
        this.context.setPageIndex(pageIndex - 1);
      }
    }
    prepareData();
    if (this.data == null) {
      this.data = new DataTable();
    }
    this.data = this.data.clone();
    for (int i = 0; (i < this.begin) && (this.data.getRowCount() > 0); i++) {
      this.data.deleteRow(0);
    }
    if (this.page) {
      this.context.setPageTotal(getPageTotal());
    }
    this.context.addDataVariable("Total", Integer.valueOf(getPageTotal()));
    this.context.addDataVariable("__Total", Integer.valueOf(this.data.getRowCount()));
    this.context.addDataVariable("ListData", this.data);
    this.context.addDataVariable("_Zving_ZList_Item", this.item);
    this.context.addDataVariable("_Zving_ZList_Data", this.data);
    if (this.data.getRowCount() > 0) {
      this.context.addDataVariable(this.item, this.data.getDataRow(0));
    }
    return 2;
  }
  
  public int onTagEnd()
  {
    return 6;
  }
  
  public abstract void prepareData()
    throws TemplateRuntimeException, ExpressionException;
  
  public abstract int getPageTotal();
  
  public DataTable getData()
  {
    return this.data;
  }
  
  public void setData(DataTable data)
  {
    this.data = data;
  }
  
  public String getItem()
  {
    return this.item;
  }
  
  public void setItem(String item)
  {
    this.item = item;
  }
  
  public int getCount()
  {
    return this.count;
  }
  
  public void setCount(int count)
  {
    this.count = count;
  }
  
  public int getBegin()
  {
    return this.begin;
  }
  
  public void setBegin(int begin)
  {
    this.begin = begin;
  }
  
  public String getCondition()
  {
    return this.condition;
  }
  
  public void setCondition(String condition)
  {
    this.condition = condition;
  }
  
  public boolean isPage()
  {
    return this.page;
  }
  
  public void setPage(boolean page)
  {
    this.page = page;
  }
  
  public int getPageSize()
  {
    return this.pageSize;
  }
  
  public void setPageSize(int pageSize)
  {
    this.pageSize = pageSize;
  }
  
  public int getSize()
  {
    return this.size;
  }
  
  public void setSize(int size)
  {
    this.size = size;
  }
}
