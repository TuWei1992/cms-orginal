package com.zving.platform.update;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.zving.framework.Config;
import com.zving.framework.collection.Mapx;
import com.zving.framework.utility.PropertiesUtil;
import com.zving.framework.utility.ServletUtil;
import com.zving.framework.xml.XMLElement;
import com.zving.framework.xml.XMLParser;

/**
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-8-16
 */
public class UpdateServer {
	private String url;
	private List<PluginUpdateRecord> pluginUpdateRecords = new ArrayList<PluginUpdateRecord>();
	private List<Product> products = new ArrayList<Product>();

	public UpdateServer(String url) {
		this.url = url;
	}

	public void loadContent() throws Exception {
		String xml = ServletUtil.getURLContent(url + "/update.xml", "UTF-8");
		XMLParser loader = new XMLParser(xml);
		List<XMLElement> list = loader.getDocument().elements("update.plugin");

		File statusFile = new File(Config.getPluginPath() + "update/update.status");
		Mapx<String, String> map = statusFile.exists() ? PropertiesUtil.read(statusFile) : new Mapx<String, String>();
		for (XMLElement nd : list) {
			String id = nd.elementText("id").trim();
			String time = nd.elementText("time").trim();
			String size = nd.elementText("size").trim();
			PluginUpdateRecord pur = new PluginUpdateRecord();
			pur.ID = id;
			pur.LastUpdateTime = Long.parseLong(time);
			pur.FileSize = Long.parseLong(size);
			if (map.getLong(id) < pur.LastUpdateTime) {
				pur.NeedUpdate = true;
			}
			pluginUpdateRecords.add(pur);
		}
		list = loader.getDocument().elements("update.product");
		for (XMLElement nd : list) {
			String id = nd.getAttributes().get("id").trim();
			Product p = new Product();
			p.ID = id;
			for (XMLElement child : nd.elements("plugin")) {
				p.PluginList.add(child.getText().trim());
			}
			products.add(p);
		}
	}

	public String getUrl() {

		return url;
	}

	public List<PluginUpdateRecord> getPluginUpdateRecords() {
		return pluginUpdateRecords;
	}

	public List<Product> getProducts() {
		return products;
	}

	public static class PluginUpdateRecord {
		public String ID;
		public long LastUpdateTime;
		public long FileSize;
		public boolean NeedUpdate;
	}

	public static class Product {
		public String ID;
		public List<String> PluginList = new ArrayList<String>();
	}
}
