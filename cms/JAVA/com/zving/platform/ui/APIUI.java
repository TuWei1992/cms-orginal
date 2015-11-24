package com.zving.platform.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.config.UploadMaxSize;
import com.zving.framework.core.handler.ZAction;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.DataTypes;
import com.zving.framework.extend.ExtendItemConfig;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.i18n.Lang;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.thirdparty.commons.fileupload.FileItem;
import com.zving.framework.thirdparty.commons.fileupload.FileItemFactory;
import com.zving.framework.thirdparty.commons.fileupload.FileUploadBase;
import com.zving.framework.thirdparty.commons.fileupload.disk.DiskFileItemFactory;
import com.zving.framework.thirdparty.commons.fileupload.servlet.ServletFileUpload;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.ui.control.TreeAction;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.ServletUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.IAPIMethod;
import com.zving.platform.api.APIOutput;
import com.zving.platform.api.APIParam;
import com.zving.platform.api.APIUtil;
import com.zving.platform.api.format.JSONFormat;
import com.zving.platform.api.format.XMLFormat;
import com.zving.platform.service.APIMethodService;

/**
 * 数据接口Action
 * 
 * @author lwy@zving.com
 * @since 2012-5-2
 */
@Alias("API")
public class APIUI extends UIFacade {

	@Priv(login = false)
	@Alias("json")
	public void getJSON(ZAction za) {
		invoke(za, JSONFormat.ID);
	}

	@Priv(login = false)
	@Alias("xml")
	public void getXML(ZAction za) {
		invoke(za, XMLFormat.ID);
	}

	@Priv(login = false)
	@Alias("common")
	public void getCommon(ZAction za) {
		invoke(za, $V("DataFormat"));
	}

	@Priv
	public void bindTree(TreeAction ta) {
		DataTable dt = new DataTable();
		dt.insertColumn("ID");
		dt.insertColumn("Name");
		for (IAPIMethod m : APIMethodService.getInstance().getAll()) {
			dt.insertRow(new Object[] { m.getExtendItemID(), m.getExtendItemName() });
		}
		ta.setRootText(Lang.get("Platform.APIList"));
		dt.insertColumn("Icon", "icons/icon096a1.png");
		ta.bindData(dt);
	}

	@Priv
	public void bindInputGrid(DataGridAction dga) {
		String id = $V("ID");
		IAPIMethod m = APIMethodService.getInstance().get(id);
		if (m != null) {
			DataTable dt = new DataTable();
			dt.insertColumn("Name");
			dt.insertColumn("DataType");
			dt.insertColumn("AllowNull");
			dt.insertColumn("Memo");
			List<APIParam> list = m.getParams();
			if (list != null) {
				for (APIParam ap : list) {
					dt.insertRow(new Object[] { ap.getName(), DataTypes.toString(ap.getType()), ap.isAllowNull(), ap.getMemo() });
				}
			}
			dga.bindData(dt);
		}
	}

	@Priv
	public void bindOutputGrid(DataGridAction dga) {
		String id = $V("ID");
		IAPIMethod m = APIMethodService.getInstance().get(id);
		if (m != null) {
			DataTable dt = new DataTable();
			dt.insertColumn("ParentID");
			dt.insertColumn("ID");
			dt.insertColumn("DataType");
			dt.insertColumn("AllowNull");
			dt.insertColumn("Memo");
			for (APIOutput o : m.getOutput()) {
				dt.insertRow(new Object[] { o.getParentName(), o.getName(), DataTypes.toString(o.getType()), o.isAllowNull(), o.getMemo() });
			}
			dga.bindData(dt);
		}
	}

	@Priv
	public void basicInfo() {
		String id = $V("ID");
		IAPIMethod m = APIMethodService.getInstance().get(id);
		if (m != null) {
			$S("ID", m.getExtendItemID());
			$S("Name", Lang.get(m.getExtendItemName()));
			for (ExtendItemConfig ei : ExtendManager.getInstance().findItemsByServiceID(APIMethodService.ID)) {
				if (ei.getClassName().equals(m.getClass().getName())) {
					$S("Plugin", ei.getPluginConfig().getID());
					break;
				}
			}
		} else {
			$S("ID", "");
		}
	}

	@SuppressWarnings({ "deprecation" })
	private void invoke(ZAction za, String dataFormat) {
		String result = null;
		String username = $V("username");
		String password = $V("password");
		String clientIP = ServletUtil.getRealIP(za.getRequest());

		String language = $V("language");
		if (ObjectUtil.empty(language)) {
			language = LangUtil.getDefaultLanguage();
		}
		String methodID = $V("method");
		String params = $V("params");

		try {
			HttpServletRequest request = za.getRequest();
			if (FileUploadBase.isMultipartContent(request)) {
				FileItemFactory fileFactory = new DiskFileItemFactory();
				ServletFileUpload upload = new ServletFileUpload(fileFactory);
				upload.setHeaderEncoding("UTF-8");
				upload.setSizeMax(UploadMaxSize.getValue());
				List<FileItem> items = upload.parseRequest(request);
				Map<String, Object> fileMap = new HashMap<String, Object>();
				List<FileItem> fileItemList = new ArrayList<FileItem>();
				Iterator<FileItem> iter = items.iterator();
				Map<String, String> fieldMap = new HashMap<String, String>();
				while (iter.hasNext()) {
					FileItem item = iter.next();
					if (!item.isFormField()) {
						String oldFileName = item.getName();
						long size = item.getSize();
						if ((oldFileName == null || oldFileName.equals("")) && size == 0) {
							continue;
						} else {
							fileItemList.add(item);
						}
					} else {
						fieldMap.put(item.getFieldName(), item.getString("UTF-8"));
					}
				}
				if (StringUtil.isEmpty(username)) {
					username = fieldMap.get("username");
				}
				if (StringUtil.isEmpty(password)) {
					password = fieldMap.get("password");
				}
				if (StringUtil.isEmpty(methodID)) {
					methodID = fieldMap.get("method");
				}
				if (StringUtil.isEmpty(params)) {
					params = fieldMap.get("params");
				}
				if (ObjectUtil.notEmpty(fileItemList) && fileItemList.size() > 0) {
					fileMap.put("fileItems", fileItemList);
					fileMap.put("hasFile", true);
				} else {
					fileMap.put("hasFile", false);
				}

				result = APIUtil.invoke(methodID, username, password, clientIP, language, dataFormat, params, fileMap);
			} else {
				result = APIUtil.invoke(methodID, username, password, clientIP, language, dataFormat, params);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = e.toString();
		}
		if(JSONFormat.ID.equalsIgnoreCase(dataFormat)){
			za.setContentType("application/json");
		}else if(XMLFormat.ID.equalsIgnoreCase(dataFormat)){
			za.setContentType("text/xml");
		}
		result = LangUtil.replace(result, language);
		za.writeHTML(result);
	}

}
