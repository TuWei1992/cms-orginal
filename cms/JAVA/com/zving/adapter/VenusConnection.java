/*
 * Copyright (C), 2013-2014, 上海汽车集团股份有限公司
 * FileName: VenusConnection.java
 * Author:   wanglijun
 * Date:     2014年10月21日 下午1:19:27
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.zving.adapter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.meidusa.venus.io.network.VenusBIOConnection;
import com.meidusa.venus.io.packet.DummyAuthenPacket;
import com.meidusa.venus.io.packet.HandshakePacket;
import com.meidusa.venus.io.packet.OKPacket;
import com.saike.venus.socketclient.SocketClientManager;
import com.zving.adapter.bl.JsonVenusRequestPacket;
import com.zving.cxdata.UCMConfig;
import com.zving.framework.utility.LogUtil;

/**
 * 连接池<br>
 * 〈功能详细描述〉
 * 
 * @author wanglijun
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class VenusConnection {
    /***/
    private static VenusConnection connection;
//    private SocketClientManager scm = null;

    /** Socket连接器 */
    private Socket socket;
    /*** Venus连接器 */
    private VenusBIOConnection conn;
    /** 插件 */
    private byte[] tmp = null;

    private VenusConnection() throws IOException {
        super();
        this.socket = new Socket();
        String busAddr = UCMConfig.getValue("service.venus.zcms.bus.ipAddressList");
        int commonIdx = busAddr.indexOf(":");
        String ip = busAddr.substring(0, commonIdx);
        String port = busAddr.substring(commonIdx+1);
        socket.connect(new InetSocketAddress(ip,Integer.parseInt(port)));
        // 10.32.140.83
//        socket.connect(new InetSocketAddress("127.0.0.1", 16800));
        conn = new VenusBIOConnection(socket, System.currentTimeMillis());
        tmp = conn.read();
        HandshakePacket packet = new HandshakePacket();
        packet.init(tmp);
        DummyAuthenPacket dummy = new DummyAuthenPacket();
        socket.getOutputStream().write(dummy.toByteArray());
        tmp = conn.read();
        OKPacket ok = new OKPacket();
        ok.init(tmp);

    }
    
//    private VenusConnection() throws IOException{
//        String busAddr = UCMConfig.getValue("service.venus.zcms.bus.ipAddressList");
//        scm = new SocketClientManager(busAddr);
//    }

    public static VenusConnection instance() {
        if (connection == null) {
            try {
                return connection = new VenusConnection();
            } catch (IOException e) {
            	throw new RuntimeException(e);
            }
        }
        return connection;
    }
    
//    public byte[] send(JsonVenusRequestPacket request) {
//        try {
//            return scm.send(request.toByteArray());
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            throw new RuntimeException(e);
//        }
//    }

    public byte[] send(JsonVenusRequestPacket request) {
       try {
            conn.write(request.toByteArray());
            return conn.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void destory() {
        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException e) {
                LogUtil.warn(e);
            }
        }
    }
}
