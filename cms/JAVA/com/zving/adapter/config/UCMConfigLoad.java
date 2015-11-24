package com.zving.adapter.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import com.gagu.ucm.agent.service.handler.impl.UCMPropertiesHandlerImpl;
import com.gagu.ucm.core.constant.PropertyConstants;
import com.gagu.ucm.core.exception.biz.AgentPathFormatException;
import com.gagu.ucm.core.utils.ConfigUtils;
import com.zving.framework.Config;
import com.zving.framework.collection.Mapx;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.PropertiesUtil;

public class UCMConfigLoad {
    /** 配置文件路径 */
    private static final String APPLICATION_PATH = "classes/application.properties";
    /** 属性配置 */
    private static Properties properties;

    /***
     * 
     * 功能描述: <br>
     * 读取配置文件中
     * 
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    private static Mapx<String, String> applicationPath() {
        StringBuilder builder = new StringBuilder(Config.getPluginPath());
        builder.append(APPLICATION_PATH);
        return PropertiesUtil.read(new File(builder.toString()));
    }

    /***
     * 
     * 功能描述: <br>
     * 从加载UCM
     * 
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    private static void loadUCMProperties() {
        Mapx<String, String> mapx = applicationPath();
        Properties localProps = new Properties();
        localProps.put(PropertyConstants.UCM_SERVER_HOSTS, mapx.get(PropertyConstants.UCM_SERVER_HOSTS));
        localProps.put(PropertyConstants.UCM_ROOT_PATH, mapx.get(PropertyConstants.UCM_ROOT_PATH));
        localProps.put(PropertyConstants.UCM_COMMON_PATH, mapx.get(PropertyConstants.UCM_COMMON_PATH));
        localProps.put(PropertyConstants.UCM_PROJ_VERSION_PATH, mapx.get(PropertyConstants.UCM_PROJ_VERSION_PATH));
        try {
            properties = UCMPropertiesHandlerImpl.getUCMProperties(localProps);
            loadExtendProperties(properties, mapx);
        } catch (AgentPathFormatException e) {
            LogUtil.warn(e);
        } catch (IOException e) {
            LogUtil.warn(e);
        } catch (Exception e) {
            LogUtil.warn(e);
        }
    }

    /**
     * 
     * 功能描述: <br>
     * 获取UCM属性文件
     * 
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public static Properties ucmProperties() {
        if (properties == null) {
            loadUCMProperties();
        }
        return properties;
    }

    /***
     * 
     * 功能描述: <br>
     * 刷新加载Propterties
     * 
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public static void refresh() {
        loadUCMProperties();
    }
    
    private static void loadExtendProperties(Properties properties, Mapx<String, String> mapx){
        Set<Entry<String, String>> entrySet = mapx.entrySet();
        String keyName = "";
        HashMap<String, String> excludeMap = new HashMap<String, String>();
        excludeMap.put(PropertyConstants.UCM_SERVER_HOSTS, PropertyConstants.UCM_SERVER_HOSTS);
        excludeMap.put(PropertyConstants.UCM_ROOT_PATH, PropertyConstants.UCM_ROOT_PATH);
        excludeMap.put(PropertyConstants.UCM_COMMON_PATH, PropertyConstants.UCM_COMMON_PATH);
        excludeMap.put(PropertyConstants.UCM_PROJ_VERSION_PATH, PropertyConstants.UCM_PROJ_VERSION_PATH);
        for(Entry<String, String> entry : entrySet){
            keyName = entry.getKey();
            if(excludeMap.get(keyName) == null){
                properties.setProperty(keyName, entry.getValue());
            }
        }
    }
}
