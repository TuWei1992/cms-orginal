package com.zving.platform.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.zving.framework.extend.AbstractExtendService;
import com.zving.framework.json.JSON;
import com.zving.framework.json.JSONObject;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.platform.IEventType;
import com.zving.platform.MessageEvent;
import com.zving.platform.config.EventEnabled;
import com.zving.platform.config.EventUrl;

public class EventTypeService extends AbstractExtendService<IEventType<Object>> {

	public static EventTypeService getInstance() {
		return findInstance(EventTypeService.class);
	}

	public static void start() {
		if ("true".equalsIgnoreCase(EventEnabled.getValue())) {
			enabled = true;
			url = EventUrl.getValue();
		}
		if (enabled) {
			LogUtil.debug("事件发布列队已启动");
			new Thread() {
				// 执行消息线程
				@Override
				public void run() {
					for (;;) {
						try {
							MessageEvent event = take();
							JSONObject json = new JSONObject();
							json.put("topic", event.getTopic());
							json.put("message", event.getMessage());
							json.put("valid", "0");
							json.put("expiredate", "");
							post(url, json);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

				// 发布消息到SDP
				private void post(String url, JSONObject json) throws InterruptedException {
					String encode = "UTF-8";
					for (;;) {
						HttpURLConnection conn = null;
						try {
							byte[] entity = json.toString().getBytes(encode);
							URL _url = new URL(url);
							conn = (HttpURLConnection) _url.openConnection();
							conn.setConnectTimeout(5 * 1000);
							conn.setRequestMethod("POST");
							conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
							// Http头
							conn.setRequestProperty("ClientId", "com.bgctv.sdp.app.cms");
							conn.setRequestProperty("OperationCode", "com.bgctv.sdp.base.sdp.pubsub.publish");
							conn.setRequestProperty("TransactionId", Long.toString(System.currentTimeMillis()));
							conn.setDoOutput(true);// 允许输出数据
							conn.setDoInput(true);
							conn.connect();
							OutputStream outStream = conn.getOutputStream();
							outStream.write(entity);
							outStream.flush();
							outStream.close();
							if (conn.getResponseCode() == 200) {
								InputStream is = conn.getInputStream();
								String result = FileUtil.readText(is, encode);
								JSONObject obj = JSON.parseJSONObject(result);
								if (obj.getInt("code") == 0) {
									break;
								}
							}

						} catch (IOException e) {
							LogUtil.warn(e.getMessage());
							Thread.sleep(5000);
							post(url, json);
						} finally {
							if (conn != null) {
								conn.disconnect();
							}
						}
					}
				}
			}.start();
		}
	}

	private static String url;
	private static boolean enabled = false;

	private static BlockingQueue<MessageEvent> queue = new LinkedBlockingQueue<MessageEvent>();

	public static void put(String id, Object sender) {
		if (enabled) {
			MessageEvent o = getInstance().get(id).getEvent(sender);
			try {
				queue.put(o);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static MessageEvent take() throws InterruptedException {
		return queue.take();
	}

}
