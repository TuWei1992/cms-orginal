/*
 * Copyright (C), 2013-2014, 上海汽车集团股份有限公司
 * FileName: SocketUtils.java
 * Author:   wanglijun
 * Date:     2014年10月9日 下午4:52:26
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.zving.adapter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;

import com.meidusa.fastjson.JSON;
import com.meidusa.toolkit.common.poolable.ObjectPool;
import com.meidusa.toolkit.util.StringUtil;
import com.meidusa.venus.io.network.VenusBIOConnection;
import com.meidusa.venus.io.packet.AbstractVenusPacket;
import com.meidusa.venus.io.packet.ErrorPacket;
import com.meidusa.venus.io.packet.OKPacket;
import com.meidusa.venus.io.packet.PacketConstant;
import com.zving.adapter.bl.JsonVenusRequestPacket;
import com.zving.adapter.bl.JsonVenusResponsePacket;
import com.zving.cxdata.UCMConfig;
import com.zving.framework.utility.LogUtil;

/**
 * Venus 连接器<br>
 * 〈功能详细描述〉
 * 
 * @author wanglijun
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class VenusSocketConnect {
    /** 实例化对象 */
    private static VenusSocketConnect venusSocketConnect;
    /** 请求ID */
    private static AtomicLong sequence = new AtomicLong(0);
    /** 插件 */
    private byte[] tmp = null;
    
    private static ObjectPool objectPool;
    /***
     * 默认构造函数
     * 
     * @throws IOException
     */
    private VenusSocketConnect() throws IOException {
        super();

    }

    /***
     * 
     * 功能描述:根据Venus插件 <br>
     * 〈功能详细描述〉
     * 
     * @param apiName
     * @param params
     * @return
     * @throws IOException
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public synchronized   String execute(String apiName, String params) {

        JsonVenusRequestPacket request = new JsonVenusRequestPacket();
        AbstractVenusPacket venusPacket = null;
        VenusBIOConnection conn = null;
        try {

            request.clientRequestId = sequence.getAndIncrement();
            request.serviceVersion = 1;
            request.apiName = apiName;
            if (StringUtils.isNotEmpty(params)) {
                request.params = params;
            }
            conn = (VenusBIOConnection)objectPool.borrowObject();
            conn.write(request.toByteArray());
            tmp = conn.read();

            switch (AbstractVenusPacket.getType(tmp)) {
                case PacketConstant.PACKET_TYPE_ERROR:
                    ErrorPacket error = new ErrorPacket();
                    error.init(tmp);
                    venusPacket = error;
                    Assert.assertEquals(request.clientRequestId, error.clientRequestId);
                    throw new RuntimeException(error.message);
                    //break;
                case PacketConstant.PACKET_TYPE_SERVICE_RESPONSE:
                    JsonVenusResponsePacket response = new JsonVenusResponsePacket();
                    venusPacket = response;
                    venusPacket.init(tmp);
                    Assert.assertEquals(request.clientRequestId, response.clientRequestId);
                    break;
                case PacketConstant.PACKET_TYPE_OK:
                    OKPacket okpacket = new OKPacket();
                    okpacket.init(tmp);
                    venusPacket = okpacket;
                    Assert.assertEquals(request.clientRequestId, okpacket.clientRequestId);
                    break;
                default:
                    LogUtil.info("error type" + StringUtil.dumpAsHex(tmp, tmp.length));
                    break;
            }
            JsonVenusResponsePacket response = ((JsonVenusResponsePacket) venusPacket);
            return (String) response.result;

        } catch (Exception e) {
        	//LogUtil.error(e);
        	//Errorx.addError(");
            throw new RuntimeException("\n<br />接口调用错误：" + apiName +"\n<br />" + e.getMessage());
        } finally {
        	if (conn != null) {
				try {
					objectPool.returnObject(conn);
				} catch (Exception e) {
					throw new RuntimeException("\n<br />连接释放错误："+ e.getMessage());
				}
			}
        }
        
    }

    /**
     * 没有参数用此方法
     * 
     * @param apiName
     */
    public String execute(String apiName) {
        return execute(apiName, StringUtils.EMPTY);
    }

    /**
     * map形式传入参数 key是参数名 ，value是值
     * 
     * @param apiName
     * @param params
     */
    public String execute(String apiName, Map<String, Object> params) {
        return execute(apiName, JSON.toJSON(params) + StringUtils.EMPTY);
    }

    /***
     * 功能描述:单例化对象 <br>
     * 〈功能详细描述〉
     * 
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public static VenusSocketConnect getInstance() {

        if (venusSocketConnect == null) {
            try {
            	objectPool = VenusBIOPoolUtil.createObjectPool(UCMConfig.getValue("service.venus.zcms.bus.ipAddressList"), null);
                return venusSocketConnect = new VenusSocketConnect();
            } catch (IOException e) {
                LogUtil.warn(e);
            }
        }
        return venusSocketConnect;
    }
    
    
    public static void main(String[] args) {
    	for (int i = 0; i < 5; i++) {
    		new Thread(){
    			public void run() {
    				for (int i = 0; i < 100; i++) {
    					
    					String r1 = getInstance().execute("ms.HotVelSeriesService.brands",  "{\"cityId\":\"310100\"}");
						System.out.println(Thread.currentThread().getName() + ": "+ i + " - " + r1);
						String r2 = getInstance().execute("ms.DealerService.queryDealer",  "{\"cityId\":\"310100\"}");
						System.out.println(Thread.currentThread().getName() + ": "+ i + " - " + r2);
					}
    			};
    		}.start();
    	}
    }
}
