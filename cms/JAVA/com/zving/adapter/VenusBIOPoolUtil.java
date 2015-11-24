package com.zving.adapter;

import java.util.concurrent.atomic.AtomicLong;

import junit.framework.Assert;

import com.meidusa.toolkit.common.poolable.MultipleLoadBalanceObjectPool;
import com.meidusa.toolkit.common.poolable.ObjectPool;
import com.meidusa.toolkit.common.poolable.PoolableObjectPool;
import com.meidusa.toolkit.net.BackendConnectionPool;
import com.meidusa.toolkit.util.StringUtil;
import com.meidusa.venus.io.authenticate.Authenticator;
import com.meidusa.venus.io.network.VenusBIOConnection;
import com.meidusa.venus.io.network.VenusBIOConnectionFactory;
import com.meidusa.venus.io.packet.AbstractVenusPacket;
import com.meidusa.venus.io.packet.ErrorPacket;
import com.meidusa.venus.io.packet.OKPacket;
import com.meidusa.venus.io.packet.PacketConstant;
import com.zving.adapter.bl.JsonVenusRequestPacket;
import com.zving.adapter.bl.JsonVenusResponsePacket;

public class VenusBIOPoolUtil {
	public static ObjectPool createObjectPool(String ipAddress,
			Authenticator authenticator) {
		if (!StringUtil.isEmpty(ipAddress)) {
			String ipList[] = StringUtil.split(ipAddress, ", ");
			PoolableObjectPool bioPools[] = new PoolableObjectPool[ipList.length];
			BackendConnectionPool nioPools[] = new BackendConnectionPool[ipList.length];

			for (int i = 0; i < ipList.length; i++) {
				VenusBIOConnectionFactory bioFactory = new VenusBIOConnectionFactory();
				if (authenticator != null) {
					bioFactory.setAuthenticator(authenticator);
				}

				bioPools[i] = new PoolableObjectPool();

				bioPools[i].setTestOnBorrow(true);
				bioPools[i].setTestWhileIdle(true);

				bioFactory.setNeedPing(true);

				String temp[] = StringUtil.split(ipList[i], ":");
				if (temp.length > 1) {
					bioFactory.setHost(temp[0]);
					bioFactory.setPort(Integer.valueOf(temp[1]));
				} else {
					bioFactory.setHost(temp[0]);
					bioFactory.setPort(16800);
				}

				bioPools[i].setName("B-" + bioFactory.getHost() + ":"
						+ bioFactory.getPort());
				bioPools[i].setFactory(bioFactory);
				bioPools[i].init();
			}

			if (ipList.length > 1) {
				MultipleLoadBalanceObjectPool bioPool = new MultipleLoadBalanceObjectPool(
						MultipleLoadBalanceObjectPool.LOADBALANCING_ROUNDROBIN,
						bioPools);

				bioPool.setName("B-V-" + ipAddress);

				bioPool.init();

				return bioPool;

			} else {
				return bioPools[0];
			}
		} else {
			throw new IllegalArgumentException("ipaddress cannot be null");
		}
	}

	public static void main(String[] args) throws Exception {
		ObjectPool pool = VenusBIOPoolUtil.createObjectPool("127.0.0.1", null);
		AtomicLong sequence = new AtomicLong(0);
		for (int i = 0; i < 10000; i++) {
			VenusBIOConnection conn = null;
			try {
				conn = (VenusBIOConnection) pool.borrowObject();

				JsonVenusRequestPacket request = new JsonVenusRequestPacket();
				request.clientRequestId = sequence.getAndIncrement();
				request.serviceVersion = 1;
				request.apiName = "HelloService.getHello";
				request.params = "{\"name\":\"Jack\"}";

				conn.write(request.toByteArray());
				byte[] tmp = conn.read();

				AbstractVenusPacket venusPacket = null;
				switch (AbstractVenusPacket.getType(tmp)) {
				case PacketConstant.PACKET_TYPE_ERROR:
					ErrorPacket error = new ErrorPacket();
					error.init(tmp);
					venusPacket = error;
					Assert.assertEquals(request.clientRequestId,
							error.clientRequestId);
					break;
				case PacketConstant.PACKET_TYPE_SERVICE_RESPONSE:
					JsonVenusResponsePacket response = new JsonVenusResponsePacket();
					venusPacket = response;
					venusPacket.init(tmp);
					System.out.println(response.result);
					Assert.assertEquals(request.clientRequestId,
							response.clientRequestId);
					break;

				case PacketConstant.PACKET_TYPE_OK:
					OKPacket okpacket = new OKPacket();
					okpacket.init(tmp);
					venusPacket = okpacket;
					Assert.assertEquals(request.clientRequestId,
							okpacket.clientRequestId);
					break;
				default:
					System.out.println("error type"
							+ StringUtil.dumpAsHex(tmp, tmp.length));
					break;
				}

				System.out.println(venusPacket);

			} catch (Exception e) {

				e.printStackTrace();
			} finally {
				if (conn != null) {
					try {
						pool.returnObject(conn);
					} catch (Exception e) {
					}
				}
			}
			Thread.sleep(1000L);
		}

	}
}
