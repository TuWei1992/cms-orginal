package com.zving.framework.ui.control;

import java.util.ArrayList;

import com.zving.framework.thirdparty.commons.fileupload.FileItem;
import com.zving.framework.utility.ObjectUtil;

/**
 * 上传数据绑定行为类
 * 
 * @author 王育春
 * @mail wyuch@zving
 * @date 2011-5-31
 */
public class UploadAction {
	ArrayList<FileItem> items;

	public FileItem getFirstFile() {
		if (ObjectUtil.empty(items)) {
			return null;
		}
		return items.get(0);
	}

	public ArrayList<FileItem> getAllFiles() {
		return items;
	}

	public void setItems(ArrayList<FileItem> items) {
		this.items = items;
	}
}
