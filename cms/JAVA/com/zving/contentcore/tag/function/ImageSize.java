package com.zving.contentcore.tag.function;

import com.zving.contentcore.util.PublishPlatformUtil;
import com.zving.contentcore.util.SiteUtil;
import com.zving.contentcore.util.ZImageUtil;
import com.zving.framework.expression.AbstractFunction;
import com.zving.framework.expression.ExpressionException;
import com.zving.framework.expression.IVariableResolver;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ObjectUtil;
import java.io.File;

public class ImageSize extends AbstractFunction {
	public String getFunctionName() {
		return "imageSize";
	}

	public String getUsage() {
		return "@{Contentcore.ImageSizeModifierName}";
	}

	public Object execute(IVariableResolver resolver, Object... args)
			throws ExpressionException {
		Object value = args[0];
		if (ObjectUtil.empty(value)) {
			return value;
		}
		String zoomMode = "fill";
		if (args.length > 3) {
			zoomMode = (String) args[3];
		}
		String path = (String) value;
		Integer w = (Integer) args[1];
		Integer h = Integer.valueOf(120);
		if ((w == null) || (w.intValue() <= 0)) {
			w = Integer.valueOf(2048);
			zoomMode = "fit";
		}
		if (args.length > 2) {
			h = (Integer) args[2];
			if ((h == null) || (h.intValue() <= 0)) {
				h = Integer.valueOf(2048);
				zoomMode = "fit";
			}
		}
		w = Integer.valueOf(w.intValue() > 2048 ? 2048 : w.intValue());
		h = Integer.valueOf(h.intValue() > 2048 ? 2048 : h.intValue());
		AbstractExecuteContext context = (AbstractExecuteContext) resolver;
		if ((path.endsWith("/")) || (path.indexOf(".") < 0)) {
			return value;
		}
		value = resize(context, path, w.intValue(), h.intValue(), zoomMode);
		return value;
	}

	protected String resize(AbstractExecuteContext context, String url, int w, int h, String zoomMode) {
		String path = getRelativePath(context, url);
		if (path.contains(":/")) {
			return url;
		}
		String platformID = PublishPlatformUtil.getPlatformID(context);
		String siteRoot = SiteUtil.getSiteRoot(context.evalLong("Site.ID"), platformID);
		File src = new File(siteRoot + path);
		if (!src.exists()) {
			return url;
		}
		String suffix = path.substring(path.lastIndexOf('.'));
		String prefix = path.substring(0, path.lastIndexOf('.'));
		path = prefix + "_" + w + "x" + h + suffix;
		File f = new File(siteRoot + path);
		if ((!f.exists()) || (f.lastModified() != src.lastModified())) {
			ZImageUtil.saveThumbnail(src.getAbsolutePath(), w, h, zoomMode, true);
			LogUtil.debug("ImageSizeModifier:w=" + w + ",h=" + h + ",path=" + path);
			f.setLastModified(src.lastModified());
		}
		String fileName = path;
		if (path.indexOf('/') >= 0) {
			fileName = path.substring(path.lastIndexOf('/'));
		}
		if (url.indexOf('/') >= 0) {
			url = url.substring(0, url.lastIndexOf('/')) + fileName;
		} else {
			url = fileName;
		}
		return url;
	}

	protected String getRelativePath(AbstractExecuteContext context, String url) {
		String siteURL = context.eval("Site.URL");
		if (url.startsWith(siteURL)) {
			url = url.substring(siteURL.length());
		} else {
			String prefix = context.eval("Prefix");
			if (url.startsWith(prefix)) {
				url = url.substring(prefix.length());
			}
			if (url.startsWith("/")) {
				url = url.substring(1);
			}
		}
		if (url.indexOf('?') > 0) {
			url = url.substring(0, url.lastIndexOf('?'));
		}
		if (url.indexOf('#') > 0) {
			url = url.substring(0, url.lastIndexOf('#'));
		}
		int i1 = url.lastIndexOf('/');
		int i2 = url.lastIndexOf('_');
		int i3 = url.lastIndexOf('x');
		int i4 = url.lastIndexOf('.');
		if ((i2 < 0) || (i3 < 0) || (i4 < 0) || (i1 > i2) || (i1 > i3) || (i2 > i3) || (i3 > i4)) {
			return url;
		}
		url = url.substring(0, i2) + url.substring(i4);
		return url;
	}

	public String getFunctionPrefix() {
		return "";
	}

	public Class<?>[] getArgumentTypes() {
		return new Class[] { Object.class, Integer.class, Integer.class };
	}
}
