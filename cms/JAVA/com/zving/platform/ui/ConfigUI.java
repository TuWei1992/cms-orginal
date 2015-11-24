package com.zving.platform.ui;

import java.util.Date;
import java.util.List;

import com.zving.framework.Config;
import com.zving.framework.Current;
import com.zving.framework.UIFacade;
import com.zving.framework.User;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Transaction;
import com.zving.framework.extend.plugin.PluginConfig;
import com.zving.framework.extend.plugin.PluginManager;
import com.zving.framework.i18n.Lang;
import com.zving.framework.i18n.LangMapping;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.ui.control.RadioTag;
import com.zving.framework.ui.control.TreeAction;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.FixedConfigItem;
import com.zving.platform.code.ControlType;
import com.zving.platform.code.DataType;
import com.zving.platform.privilege.ConfigPriv;
import com.zving.platform.service.ConfigService;
import com.zving.platform.util.PlatformUtil;
import com.zving.schema.ZDConfig;

/**
 * @Author 陈海强
 * @Date 2007-8-6
 * @Mail chq@zving.com
 */
@Alias("Config")
public class ConfigUI extends UIFacade {
	@Priv(ConfigPriv.MenuID)
	public void init() {
		List<FixedConfigItem> items = ConfigService.getInstance().getAll();
		List<PluginConfig> plugins = PluginManager.getInstance().getAllPluginConfig();
		DataTable dt = new DataTable();
		dt.insertColumns("dataType", "code", "name", "pluginID", "value", "pluginName", "controlType", "id");
		for (PluginConfig pc : plugins) {// 按插件顺序输出
			for (FixedConfigItem fc : items) {
				if (fc.getPluginID().equals(pc.getID())) {
					String pluginName = StringUtil.isEmpty(LangMapping.get(pc.getName())) ? pc.getName() : LangMapping.get(pc.getName());
					String name = StringUtil.isEmpty(LangMapping.get(fc.getExtendItemName())) ? fc.getExtendItemName() : LangMapping.get(fc
							.getExtendItemName());
					dt.insertRow(new Object[] { fc.getDataType(), fc.getCode(), name, fc.getPluginID(),
							Config.getValue(fc.getExtendItemID()), pluginName, fc.getControlType(), fc.getExtendItemID() });
				}
			}
		}

		StringBuilder sb = new StringBuilder();
		String lastPluginID = "";
		for (DataRow dr : dt) {
			if (!dr.getString("pluginID").equals(lastPluginID)) {
				lastPluginID = dr.getString("pluginID");
				sb.append("</table>");
				sb.append("\n<a id=\"" + dr.getString("pluginID").replaceAll("\\.", "_") + "\"></a>");
				sb.append("<div class=\"z-legend\" name=\"" + dr.getString("pluginID").replaceAll("\\.", "_") + "\"><b>"
						+ dr.getString("pluginName") + "</b></div>");
				sb.append("\n");
				sb.append("<table width=\"600\" border=\"1\" cellpadding=\"4\" cellspacing=\"0\" bordercolor=\"#eeeeee\" class=\"formTable\">");
				sb.append("\n");
			}
			sb.append("<tr>\n<td width=\"300\">" + dr.getString("name") + "：</td><td>");
			// 处理输入的控件类型
			String type = dr.getString("ControlType");
			if (ControlType.Text.equals(type)) {
				sb.append("<input name=\"" + dr.getString("code") + "\" type=\"text\" style=\"width:280px\"");
				if (DataType.Double.equals(dr.getString("DataType"))) {
					sb.append("verify='Number'");
				}
				if (DataType.Long.equals(dr.getString("DataType"))) {
					sb.append("verify='Int'");
				}
				sb.append(" value=\"" + dr.getString("value") + "\"  ></td>\n");
			} else if (ControlType.Password.equals(type)) {
				sb.append("<input name=\"" + dr.getString("code") + "\" type=\"password\" style=\"width:280px\" value=\""
						+ dr.getString("value") + "\"  ></td>\n");
			} else if (ControlType.Radio.equals(type)) {
				RadioTag radioTag = new RadioTag();
				FixedConfigItem fci = ConfigService.getInstance().get(dr.getString("id"));
				String option = "";
				Mapx<String, String> opm = fci.getOptions();
				if (opm != null) {
					for (String key : opm.keySet()) {
						option += "," + opm.getString(key) + ":" + key;
					}
				}
				if (option.length() > 0) {
					option = option.substring(1);
				}
				radioTag.setOptions(option);
				radioTag.setType("Radio");
				radioTag.setCode("");
				radioTag.setName(dr.getString("code"));
				radioTag.setValue(dr.getString("value"));
				sb.append(radioTag.getHtml() + "\n");
			} else if (ControlType.TextArea.equals(type)) {
				sb.append("<textarea name=\"" + dr.getString("code") + "\" type=\"text\" style=\"width:280px\"   >" + dr.getString("value")
						+ "</textarea>\n");
			} else {
				sb.append("<input name=\"" + dr.getString("code") + "\" type=\"text\" style=\"width:280px\" value=\""
						+ dr.getString("value") + "\"  ></td>\n");
			}

			sb.append("</tr>\n");
		}
		String body = sb.toString();
		if (body.length() > 0) {
			body = body.substring(9) + body.substring(0, 9);
		}
		$S("body", body);
	}

	@Priv(ConfigPriv.Save)
	public void saveAll() {
		Transaction tran = Current.getTransaction();
		List<FixedConfigItem> items = ConfigService.getInstance().getAll();
		ZDConfig config;
		for (FixedConfigItem fci : items) {
			String value = Request.getString(fci.getExtendItemID());
			if (value != null) {
				config = new ZDConfig();
				config.setCode(fci.getExtendItemID());
				if (config.fill()) {
					if (ObjectUtil.empty(value)) {
						tran.delete(config);
						continue;
					}
					config.setValue(value);
					config.setName(fci.getExtendItemName());
					config.setModifyTime(new Date());
					config.setModifyUser(User.getUserName());
					tran.update(config);
				} else {
					if (ObjectUtil.empty(value)) {
						continue;
					}
					config.setValue(value);
					config.setAddTime(new Date());
					config.setAddUser(User.getUserName());
					config.setName(fci.getExtendItemName());
					tran.insert(config);
				}
			}
		}
		if (tran.commit()) {
			for (FixedConfigItem fci : items) {
				Config.getMapx().remove(fci.getExtendItemID());
			}
			PlatformUtil.refresh();
			success(Lang.get("Common.ExecuteSuccess"));
		} else {
			fail(Lang.get("Common.ExecuteFailed"));
		}
	}

	/**
	 * 添加查找配置项列表
	 */
	@Priv(ConfigPriv.MenuID)
	public void bindTree(TreeAction ta) {
		List<FixedConfigItem> items = ConfigService.getInstance().getAll();
		List<PluginConfig> plugins = PluginManager.getInstance().getAllPluginConfig();
		DataTable dt = new DataTable();
		dt.insertColumns(new String[] { "ID" });
		dt.insertColumns(new String[] { "Value" });
		for (PluginConfig pc : plugins) {// 按插件顺序输出
			for (FixedConfigItem fc : items) {
				if (fc.getPluginID().equals(pc.getID())) {
					dt.insertRow(new Object[] { pc.getID(),
							StringUtil.isEmpty(LangMapping.get(pc.getName())) ? pc.getName() : LangMapping.get(pc.getName()) });
					break;
				}
			}
		}
		ta.setRootText(LangUtil.get("@{Platform.PluginList}"));
		ta.setBranchIcon("icons/icon025a1.png");
		ta.setLeafIcon("icons/icon003a1.png");
		ta.bindData(dt);
	}
}
