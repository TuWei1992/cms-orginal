package com.zving.framework.schedule;

import java.util.List;

import com.zving.framework.ConfigLoader;
import com.zving.framework.extend.AbstractExtendService;
import com.zving.framework.extend.IExtendItem;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.xml.XMLElement;

/**
 * 系统定时任务扩展服务类
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-3-9
 */
public class SystemTaskService extends AbstractExtendService<SystemTask> {

	public static SystemTaskService getInstance() {
		return findInstance(SystemTaskService.class);
	}

	@Override
	public void register(IExtendItem item) {
		super.register(item);
		loadCronConfig((SystemTask) item);
	}

	public static void loadCronConfig(SystemTask task) {
		List<XMLElement> datas = ConfigLoader.getElements("*.cron.task");
		for (XMLElement data : datas) {
			String id = data.getAttributes().get("id");
			String time = data.getAttributes().get("time");
			String disabled = data.getAttributes().get("disabled");
			if (ObjectUtil.empty(id)) {
				continue;
			}
			if (task.getExtendItemID().equals(id)) {
				if (!ObjectUtil.empty(time)) {
					task.setCronExpression(time);
				}
				task.setDisabled("true".equals(disabled));
				break;
			}
		}
	}
}
