package com.zving.framework.extend;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.zving.framework.core.FrameworkException;
import com.zving.framework.extend.plugin.PluginConfig;
import com.zving.framework.extend.plugin.PluginException;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.xml.XMLElement;

/**
 * 扩展服务配置信息类
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-3-9
 */
public class ExtendServiceConfig {
	private boolean enable;
	private PluginConfig pluginConfig;
	private String id;
	private String description;
	private String className;
	private String itemClassName;

	private IExtendService<?> instance = null;
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
			if (nd.getQName().equalsIgnoreCase("class")) {
				className = nd.getText().trim();
			}
			if (nd.getQName().equalsIgnoreCase("itemClass")) {
				itemClassName = nd.getText().trim();
			}
		}
		if (ObjectUtil.empty(id)) {
			throw new PluginException("extendPoint's id is empty!");
		}
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

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public PluginConfig getPluginConfig() {
		return pluginConfig;
	}

	public String getItemClassName() {
		return itemClassName;
	}

	public void setItemClassName(String itemClassName) {
		this.itemClassName = itemClassName;
	}

	public IExtendService<?> getInstance() {
		try {
			if (instance == null) {
				lock.lock();
				try {
					if (instance == null) {
						Class<?> clazz = Class.forName(className);
						IExtendService<?> tmp = (IExtendService<?>) clazz.newInstance();
						try {
							List<ExtendItemConfig> list = ExtendManager.getInstance().findItemsByServiceID(id);
							if (ObjectUtil.notEmpty(list)) {
								for (ExtendItemConfig item : list) {
									try {
										tmp.register(item.getInstance());
									} catch (Exception e) {
										e.printStackTrace();
										LogUtil.error("Load ExtendItem " + item.getClassName() + " failed!");
									}
								}
							}
							instance = tmp;
						} catch (Exception e) {
							e.printStackTrace();
							LogUtil.error("Load ExtendService " + className + " failed!");
						}
					}
				} finally {
					lock.unlock();
				}
			}
			return instance;
		} catch (Exception e) {
			throw new FrameworkException(e);
		}
	}

	public void destory() {
		if (instance != null) {
			instance.destory();
		}
	}
}
