package com.zving.cxdata.bl;

import java.io.File;

import com.zving.contentcore.item.PCPublishPlatform;
import com.zving.contentcore.util.SiteUtil;
import com.zving.cxdata.UCMConfig;
import com.zving.framework.Config;
import com.zving.framework.expression.IVariableResolver;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;

public class ResourceURLBL {
	public static String processResourceURL(String url, String resHost, int start, int end, String replaceHolder) {
		if (StringUtil.isEmpty(url)) {
			return "";
		}
	    int suffix = Math.abs(url.hashCode() % end) + start;
	    

	    String hostName = resHost.replace(replaceHolder, String.valueOf(suffix));
	    if (!hostName.endsWith("/")) {
	      hostName = hostName + "/";
	    }
	    if (url.startsWith("/")) {
	      url = url.substring(1);
	    }
	    String result = hostName + url;
	    
	    return result;
	}
	
	public static String processResourceURL(String url, String resourcePrefix, String resHostKey, String startKey, String endKey, String replaceHolderKey) {
		String resHost = UCMConfig.getValue(resHostKey);
		if (ObjectUtil.notEmpty(resourcePrefix)) {
			resHost += resourcePrefix;
		}
		int	start = Integer.parseInt(UCMConfig.getValue(startKey));
		int	end = Integer.parseInt(UCMConfig.getValue(endKey));
		String	replaceHolder = UCMConfig.getValue(replaceHolderKey);
		return processResourceURL(url, resHost, start, end, replaceHolder);
	}
	
	public static String processStaticURL(String resHostKey, String startKey, String endKey, String replaceHolderKey, IVariableResolver resolver, Object[] args) {
    	AbstractExecuteContext context = (AbstractExecuteContext) resolver;
		String url = (String)args[0];
		String suffix = null;
		if (needProcessSuffix(url)) {
			int index = url.lastIndexOf(".");
			suffix = url.substring(index+1);
			url = url.substring(0, index);
		}
	    boolean compress = true;
	    /*
	    if (args.length > 1) {
	    	compress = (Boolean)args[1];
	    }
	    */
	    if (url == null) {
	    	url = "";
	    }

	    String result;
	    if(context.isPreview()) {
	    	result = context.eval("Prefix") + url;
	    } else {
	    	String resourcePrefix = getResourcePrefix(context);
	    	if (args.length > 1) {
	    		resourcePrefix = (String)args[1];
	    	}
	    	result = processResourceURL(url, resourcePrefix, resHostKey, startKey, endKey, replaceHolderKey);
	    }
	    
	    if (suffix != null) {
		    String minSuffix = UCMConfig.getValue("ebiz.zcms.web.minSuffix");
		    if (minSuffix == null) {
		    	minSuffix = "";
		    }
		    if (compress) {
		    	result += minSuffix;
		    	url += minSuffix;
		    }
	    	result += "." + suffix;
	    	url += "." + suffix;
	    }
    	String platformID = context.eval("PlatformID");
    	if (StringUtil.isEmpty(platformID)) {
    		platformID = PCPublishPlatform.ID;
    	}
    	File f = new File(SiteUtil.getSiteRoot(context.evalLong("Site.ID"), platformID) + url);
    	if (f.exists() && (isCssOrJS(url) || isImage(url))) {
    		result += "?t="+ f.lastModified();
    	}
	    return result;
	}
	
	public static String getCMSStatictResourceURL(IVariableResolver resolver, Object[] args) {
		return processStaticURL("ebiz.zcms.web.static.url",
				"ebiz.zcms.web.static.url.startIdx",
				"ebiz.zcms.web.static.url.endIdx",
				"ebiz.zcms.web.static.url.placeholder",
				resolver, 
				args);
	}
	
	public static String getCommonImageURL(String url) {
		return processResourceURL(url, null,
				"ebiz.zcms.web.common.image.url",
				"ebiz.zcms.web.common.image.url.startIdx", 
				"ebiz.zcms.web.common.image.url.endIdx",
				"ebiz.zcms.web.common.image.url.placeholder");
	}
	
	public static String getCMSDynamicResourceURL(IVariableResolver resolver, Object... args) {
		AbstractExecuteContext context = (AbstractExecuteContext) resolver;
		String resourcePrefix = getResourcePrefix(context);
	    String url = (String)args[0];
	    if (url == null) {
	    	url = "";
	    }
	    if (args.length > 1 && StringUtil.isNotEmpty((String)args[1])) {
	    	resourcePrefix = (String)args[1];
	    }
	    Boolean preview = false;
	    if (args.length > 2) {
	    	preview = (Boolean)args[2];
	    }
	    if (preview) {
	    	return context.eval("Prefix") + url;
	    }
	    
		return processResourceURL(url,
				 resourcePrefix,
				"ebiz.zcms.web.dynamic.url",
				"ebiz.zcms.web.dynamic.url.startIdx", 
				"ebiz.zcms.web.dynamic.url.endIdx",
				"ebiz.zcms.web.dynamic.url.placeholder");
		}
	
	public static boolean isCssOrJS(String url) {
		return StringUtil.isNotEmpty(url) && (url.endsWith(".css") || url.endsWith(".js"));
	}
	
	public static boolean needProcessSuffix(String url) {
		String minSuffix = UCMConfig.getValue("ebiz.zcms.web.minSuffix");
		return isCssOrJS(url) && !url.endsWith(minSuffix + ".css") && !url.endsWith(minSuffix + ".js");
	}
	
	public static boolean isImage(String url) {
		return StringUtil.isNotEmpty(url) && (url.endsWith(".png") || url.endsWith(".jpg")) || url.endsWith(".gif");
	}
	
	public static String getImageRelativePath(String url) {
		int uploadIndex = url.indexOf("upload");
		if (uploadIndex != -1) {
			String dynamicURL = UCMConfig.getValue("ebiz.zcms.web.dynamic.url");
			if (ObjectUtil.notEmpty(dynamicURL)) {
				if (url.matches(dynamicURL.replace(UCMConfig.getValue("ebiz.zcms.web.dynamic.url.placeholder"), "\\d") + ".+")) {
					return url.substring(uploadIndex);
				}
			}
		}
		return url;
	}
	
	public static String getResourcePrefix(AbstractExecuteContext context) {
		String prefix = context.eval("Site.Config_resourcePrefix");
		String platformID = context.eval("PlatformID");
		if (!PCPublishPlatform.ID.equals(platformID)) {
			prefix = context.eval("Site.Config_resourcePrefix_" + platformID);
		}
		return prefix;
	}
	
	
	public static void main(String[] args) {
		String surl ="http://s?.dds.com";
		String p1 = "http://s2.dds.com/msweb02/upload/image/2014/12/12/123.jpg";
		String p2 = "http://s1.ddss.com/upload/image/2014/12/12/123.jpg";
		System.out.println(p1.matches(surl.replace("?", "\\d")+".+"));
		System.out.println(p2.matches(surl.replace("?", "\\d")+".+"));
		System.out.println(p1.substring(p1.indexOf("upload")));
		System.out.println(p2.substring(p2.indexOf("upload")));
	}
	
}
