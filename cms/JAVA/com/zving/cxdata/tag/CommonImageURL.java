package com.zving.cxdata.tag;

import com.zving.cxdata.bl.ResourceURLBL;
import com.zving.framework.expression.AbstractFunction;
import com.zving.framework.expression.IVariableResolver;

public class CommonImageURL
  extends AbstractFunction
{
  public Object execute(IVariableResolver resolver, Object... args) {
	  String url = (String)args[0];
	    
	    if (url == null) {
	    	url = "";
	    }
	    
	    String result = ResourceURLBL.getCommonImageURL(url);
	    return result;
  }
  
  public Class<?>[] getArgumentTypes()
  {
    return new Class[] { String.class};
  }
  
  public String getFunctionPrefix()
  {
    return "";
  }
  
  public String getFunctionName()
  {
    return "commonImg";
  }
}



/* Location:           D:\work\workspace\zcms22426\UI\WEB-INF\plugins\lib\com.zving.framework.plugin.jar

 * Qualified Name:     com.zving.framework.expression.function.Replace

 * JD-Core Version:    0.7.0.1

 */