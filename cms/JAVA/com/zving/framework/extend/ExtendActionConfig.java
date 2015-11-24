package com.zving.framework.extend;

import java.util.concurrent.locks.ReentrantLock;

import com.zving.framework.extend.exception.CreateExtendActionInstanceException;
import com.zving.framework.extend.plugin.PluginConfig;
import com.zving.framework.extend.plugin.PluginException;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.xml.XMLElement;

/**
 * 扩展行为配置信息类
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-8-9
 */
public class ExtendActionConfig {
	private boolean enable;
	private PluginConfig pluginConfig;
	private String id;
	private String description;
	private String extendPointID;
	private String className;
	private IExtendAction instance = null;
	private static ReentrantLock lock = new ReentrantLock();

	public void init(PluginConfig pc, XMLElement parent) throws PluginException {
		pluginConfig = pc;
		for (XMLElement nd : parent.elements()) {
			if (nd.getQName().equalsIgnoreCase("id")) {
				id = nd.getText().trim();
			}
			if (nd.getQName().equalsIgnoreCase("description")) {
				description = nd.getText().trim();
			}
			if (nd.getQName().equalsIgnoreCase("extendPoint")) {
				extendPointID = nd.getText().trim();
			}
			if (nd.getQName().equalsIgnoreCase("class")) {
				className = nd.getText().trim();
			}
		}
		if (ObjectUtil.empty(id)) {
			throw new PluginException("extendAction's id is empty!");
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

	public String getExtendPointID() {
		return extendPointID;
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

	public void setExtendPointID(String extendPointID) {
		this.extendPointID = extendPointID;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public IExtendAction getInstance() {
		try {
			if (instance == null) {
				lock.lock();
				try {
					if (instance == null) {
						Class<?> clazz = Class.forName(className);
						ExtendPointConfig ep = ExtendManager.getInstance().findExtendPoint(extendPointID);
						if (ep.isChild(clazz)) {
							throw new CreateExtendActionInstanceException("ExtendAction " + className + " must extends "
									+ ep.getClassName());
						}
						instance = (IExtendAction) clazz.newInstance();
					}
				} finally {
					lock.unlock();
				}
			}
			return instance;
		} catch (Exception e) {
			throw new CreateExtendActionInstanceException(e);
		}
	}
}
