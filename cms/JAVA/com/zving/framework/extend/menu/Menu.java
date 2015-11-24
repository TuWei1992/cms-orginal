package com.zving.framework.extend.menu;

import com.zving.framework.extend.plugin.PluginConfig;
import com.zving.framework.extend.plugin.PluginException;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.xml.XMLElement;

/**
 * 表示插件配置文件中的一个菜单
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-8-12
 */
public class Menu {
	public static final String Type_Backend = "Backend";
	public static final String Type_Frontend = "Frontend";// NO_UCD

	private String ID;
	private String parentID;
	private String description;
	private String name;
	private String icon;
	private String order;
	private String URL;
	private String type;
	private PluginConfig config;

	public void parse(PluginConfig pc, XMLElement parent) throws PluginException {
		config = pc;
		for (XMLElement nd : parent.elements()) {
			if (nd.getQName().equalsIgnoreCase("id")) {
				ID = nd.getText().trim();
			}
			if (nd.getQName().equalsIgnoreCase("parentId")) {
				parentID = nd.getText().trim();
			}
			if (nd.getQName().equalsIgnoreCase("description")) {
				description = nd.getText().trim();
			}
			if (nd.getQName().equalsIgnoreCase("name")) {
				name = nd.getText().trim();
			}
			if (nd.getQName().equalsIgnoreCase("icon")) {
				icon = nd.getText().trim();
			}
			if (nd.getQName().equalsIgnoreCase("URL")) {
				URL = nd.getText().trim();
			}
			if (nd.getQName().equalsIgnoreCase("order")) {
				order = nd.getText().trim();
			}
			if (nd.getQName().equalsIgnoreCase("type")) {
				type = nd.getText().trim();
			}
		}
		if (ObjectUtil.empty(ID)) {
			throw new PluginException("menu's id is empty!");
		}
		if (ObjectUtil.empty(name)) {
			throw new PluginException("menu's name is empty!");
		}
	}

	public PluginConfig getPluginConfig() {
		return config;
	}

	public String getID() {
		return ID;
	}

	public String getParentID() {
		return parentID;
	}

	public String getName() {
		return name;
	}

	public String getName(String language) {
		if (name == null) {
			return null;
		}
		return LangUtil.get(name, language);
	}

	public String getURL() {
		return URL;
	}

	public String getType() {
		return type;
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

	public String getIcon() {
		return icon;
	}

	public String getOrder() {
		return order;
	}

	public void setPluginConfig(PluginConfig pc) {
		config = pc;
	}

	public void setID(String id) {
		ID = id;
	}

	public void setParentID(String parentId) {
		parentID = parentId;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	public void setType(String type) {
		this.type = type;
	}

}
