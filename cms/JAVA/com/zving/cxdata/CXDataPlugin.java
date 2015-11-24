package com.zving.cxdata;


import com.zving.framework.extend.plugin.AbstractPlugin;
import com.zving.framework.extend.plugin.PluginException;
/**
 * 车享插件类
 * @author v_zhouquan
 *
 */
public class CXDataPlugin extends AbstractPlugin {
	public static final String ID = "com.zving.cxdata";
	
	@Override
	public void start() throws PluginException {
		// TODO Auto-generated method stub
		UCMConfig.addToConfig();
		
	}

	@Override
	public void stop() throws PluginException {
		// TODO Auto-generated method stub

	}

}
