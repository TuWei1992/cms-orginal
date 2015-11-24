package com.zving.framework.core.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.zving.framework.Config;
import com.zving.framework.Current;
import com.zving.framework.SessionListener;
import com.zving.framework.User;
import com.zving.framework.User.UserData;
import com.zving.framework.collection.ConcurrentMapx;
import com.zving.framework.config.UploadMaxSize;
import com.zving.framework.core.IURLHandler;
import com.zving.framework.core.method.IMethodLocator;
import com.zving.framework.core.method.MethodLocatorUtil;
import com.zving.framework.data.DataCollection;
import com.zving.framework.i18n.LangMapping;
import com.zving.framework.security.PrivCheck;
import com.zving.framework.security.VerifyCheck;
import com.zving.framework.security.exception.PrivException;
import com.zving.framework.thirdparty.commons.fileupload.FileItem;
import com.zving.framework.thirdparty.commons.fileupload.FileItemFactory;
import com.zving.framework.thirdparty.commons.fileupload.disk.DiskFileItemFactory;
import com.zving.framework.thirdparty.commons.fileupload.servlet.ServletFileUpload;
import com.zving.framework.ui.control.UploadAction;
import com.zving.framework.ui.control.UploadUI;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.preloader.facade.HttpSessionListenerFacade;

/**
 * 上传控件数据提交处理者
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-5
 */
public class UploadHandler implements IURLHandler {
	public static final String ID = "com.zving.framework.core.UploadHandler";

	// 多文件上传时需要先放到Map中，最后一个文件上传后才调用method
	private static ConcurrentMapx<String, TaskFiles> uploadFileMap = new ConcurrentMapx<String, TaskFiles>(5000);

	@Override
	public boolean match(String url) {
		return url.startsWith("/ZUploader.zhtml");
	}

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "Upload Invoke Processor";
	}

	@Override
	public boolean handle(String url, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("text/html; charset=" + Config.getGlobalCharset());
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter out = response.getWriter();
		FileItemFactory fileFactory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(fileFactory);
		upload.setHeaderEncoding("UTF-8");
		upload.setSizeMax(UploadMaxSize.getValue());
		try {
			List<?> items = upload.parseRequest(request);
			HashMap<String, String> fields = new HashMap<String, String>();
			ArrayList<FileItem> files = new ArrayList<FileItem>();
			Iterator<?> iter = items.iterator();
			while (iter.hasNext()) {
				FileItem item = (FileItem) iter.next();
				if (item.isFormField()) {
					fields.put(item.getFieldName(), item.getString("UTF-8"));
				} else {
					String OldFileName = item.getName();
					long size = item.getSize();
					if ((OldFileName == null || OldFileName.equals("")) && size == 0) {
						continue;
					} else {
						LogUtil.info("-----UploadFileName:-----" + OldFileName);
						files.add(item);
					}
				}
			}
			String taskID = fields.get("_ZUploder_TaskID");
			String totalStr = fields.get("_ZUploader_Total");

			// 必须先置入状态
			UploadUI.setTask(taskID, LangMapping.get("Framework.Upload.Status"));

			// 处理Firefox下的Session问题
			String ids = fields.get("_SessionID");
			if (StringUtil.isNotEmpty(ids)) {
				HttpSession session = request.getSession();
				String[] arr = ids.split("\\,");
				HttpSession sessionOld = null;
				for (String sessionID : arr) {
					if (session.getId().equals(sessionID)) {
						break;
					}
					sessionOld = HttpSessionListenerFacade.getSession(sessionID);
					if (sessionOld != null) {
						break;
					}
				}
				if (sessionOld != null) {
					// 从有效session中复制数据到新的session
					Enumeration<?> en = sessionOld.getAttributeNames();
					while (en.hasMoreElements()) {
						String n = (String) en.nextElement();
						session.setAttribute(n, sessionOld.getAttribute(n));
					}
					UserData u = SessionListener.getUserDataFromSession(session);
					if (u != null) {
						User.setCurrent(u);
					}
				}
			}

			int total = files.size();
			if (ObjectUtil.notEmpty(totalStr)) {
				total = Integer.parseInt(totalStr);
			}
			TaskFiles uploadedFiles = null;
			checkTimeout();
			uploadedFiles = uploadFileMap.get(taskID);
			if (uploadedFiles == null) {
				uploadedFiles = new TaskFiles();
				uploadFileMap.put(taskID, uploadedFiles);
			}
			uploadedFiles.LastTime = System.currentTimeMillis();
			uploadedFiles.Files.addAll(files);
			if (total == uploadedFiles.Files.size()) {
				String method = fields.get("_Method");
				IMethodLocator m = MethodLocatorUtil.find(method);
				Current.getRequest().putAll(fields);
				try {
					PrivCheck.check(m);
				} catch (PrivException e) {				
					uploadFileMap.remove(taskID);
					//在这儿约定550为权限校验失败返回的状态码
					UploadUI.setTask(taskID, "Error 550");
					for (FileItem file : uploadedFiles.Files) {
						file.delete();// 删除清理掉临时文件
					}
					throw e;
				}

				// 参数检查
				if (!VerifyCheck.check(m)) {
					String message = "Verify check failed:method=" + method + ",data=" + Current.getRequest();
					LogUtil.warn(message);

					uploadFileMap.remove(taskID);
					for (FileItem file : uploadedFiles.Files) {
						file.delete();// 删除清理掉临时文件
					}
					return true;// 参数检查未通过，则不继续执行
				}

				UploadAction ua = new UploadAction();
				ua.setItems(uploadedFiles.Files);

				m.execute(ua);

				for (FileItem file : uploadedFiles.Files) {
					file.delete();// 删除清理掉临时文件
				}
				uploadFileMap.remove(taskID);
				UploadUI.setTask(taskID, "Finished");// 必须是“Finished”
				response.getWriter().write(Current.getResponse().toXML());// 将结果返回给页面
			} else {
				response.getWriter().write(new DataCollection().toXML());// 输出空的数据集
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		out.flush();
		out.close();
		return true;
	}

	private static void checkTimeout() {// 不需要再加锁，外面已经加锁
		long yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
		for (Entry<String, TaskFiles> entry : uploadFileMap.entrySet()) {
			String id = entry.getKey();
			TaskFiles tf = entry.getValue();
			if (tf == null) {
				continue;
			}
			if (tf.LastTime < yesterday) {
				uploadFileMap.remove(id);
			}
		}
	}

	private static class TaskFiles {
		public long LastTime;// 最后活动时间
		public ArrayList<FileItem> Files = new ArrayList<FileItem>();
	}

	@Override
	public void init() {
	}

	@Override
	public void destroy() {
	}

	@Override
	public int getOrder() {
		return 9998;
	}

}
