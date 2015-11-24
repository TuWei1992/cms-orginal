package com.zving.framework.extend;

import com.zving.framework.extend.exception.CreateExtendItemInstanceException;
import com.zving.framework.extend.plugin.PluginConfig;
import com.zving.framework.extend.plugin.PluginException;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.xml.XMLElement;

/**
 * 扩展项配置信息类
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-3-9
 */
public class ExtendItemConfig {
	private boolean enable;
	private PluginConfig pluginConfig;
	private String id;
	private String description;
	private String extendServiceID;
	private String className;
	private IExtendItem instance = null;

	public void init(PluginConfig pc, XMLElement parent) throws PluginException {
		pluginConfig = pc;
		for (XMLElement nd : parent.elements()) {
			if (nd.getQName().equalsIgnoreCase("id")) {
				id = nd.getText().trim();
			}
			if (nd.getQName().equalsIgnoreCase("description")) {
				description = nd.getText().trim();
			}
			if (nd.getQName().equalsIgnoreCase("extendService")) {
				extendServiceID = nd.getText().trim();
			}
			if (nd.getQName().equalsIgnoreCase("class")) {
				className = nd.getText().trim();
			}
		}
		if (ObjectUtil.empty(id)) {
			throw new PluginException("extendItem's id is empty!");
		}
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public boolean isEnable() {
		return enable;
	}

	public PluginConfig getPluginConfig() {
		return pluginConfig;
	}

	public String getID() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public String getDescription(String language) {
		if (description == null) {
			return null;
		}
		return LangUtil.get(description, language);
	}

	public String getExtendServiceID() {
		return extendServiceID;
	}

	public String getClassName() {
		return className;
	}

	public void setID(String id) {
		this.id = id;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setExtendServiceID(String extendServiceID) {
		this.extendServiceID = extendServiceID;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public IExtendItem getInstance() {
		try {
			if (instance == null) {
				Class<?> clazz = Class.forName(className);
				try {
					instance = (IExtendItem) clazz.newInstance();
				} catch (Exception e) {
					e.printStackTrace();
					throw new CreateExtendItemInstanceException("ExtendItem " + className + " must implements IExtendItem");
				}
			}
			return instance;
		} catch (Exception e) {
			throw new CreateExtendItemInstanceException(e);
		}
	}
}
