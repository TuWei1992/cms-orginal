package com.zving.framework.extend;

import com.zving.framework.extend.plugin.PluginConfig;
import com.zving.framework.extend.plugin.PluginException;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.xml.XMLElement;

/**
 * 扩展点配置信息类
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-7-15
 */
public class ExtendPointConfig {
	private boolean enable;
	private PluginConfig pluginConfig;
	private String id;
	private String description;
	private String className;
	private boolean UIFlag;
	private Class<?> clazz;

	public void init(PluginConfig pc, XMLElement parent) throws PluginException {
		pluginConfig = pc;
		for (XMLElement nd : parent.elements()) {
			if (nd.getQName().equalsIgnoreCase("id")) {
				id = nd.getText().trim();
			}
			if (nd.getQName().equalsIgnoreCase("description")) {
				description = nd.getText().trim();
			}
			if (nd.getQName().equalsIgnoreCase("class")) {
				className = nd.getText().trim();
			}
			if (nd.getQName().equalsIgnoreCase("UIFlag")) {
				UIFlag = "true".equals(nd.getText().trim());
			}
		}
		if (ObjectUtil.empty(id)) {
			throw new PluginException("extendPoint's id is empty!");
		}
	}

	public String getID() {
		return id;
	}

	public boolean getUIFlag() {
		return UIFlag;
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

	public String getClassName() {
		return className;
	}

	public void setID(String id) {
		this.id = id;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setUIFlag(boolean uIFlag) {
		UIFlag = uIFlag;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public PluginConfig getPluginConfig() {
		return pluginConfig;
	}

	public boolean isChild(Class<?> cls) {
		if (className == null) {
			return false;
		}
		if (clazz == null) {
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return cls.isAssignableFrom(clazz);
	}

	public Class<?> getParentClass() throws PluginException {
		if (className == null) {
			return null;
		}
		if (clazz == null) {
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new PluginException("ExtendPoint's class not found:" + className);
			}
		}
		return clazz;
	}
}
