package com.zving.test.point;

import com.zving.framework.data.Q;
import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.template.AbstractTag;
import com.zving.staticize.template.TemplateContext;

public abstract class BookTagSqlExtend
  implements IExtendAction
{
  public static final String ExtendPointID = "com.zving.test.extend.BookTagSqlExtend";
  
  public Object execute(Object[] args)
    throws ExtendException
  {
    AbstractExecuteContext context = (TemplateContext)args[0];
    AbstractTag tag = (AbstractTag)args[1];
    Q qb = (Q)args[2];
    execute(context, tag, qb);
    return null;
  }
  
  public abstract void execute(AbstractExecuteContext paramAbstractExecuteContext, AbstractTag paramAbstractTag, Q paramQ)
    throws ExtendException;
}


/* Location:           D:\work\workspace\zcms22426\UI\WEB-INF\plugins\lib\com.zving.media.plugin.jar
 * Qualified Name:     com.zving.media.point.AudioTagSqlExtend
 * JD-Core Version:    0.7.0.1
 */