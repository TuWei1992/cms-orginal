package com.zving.platform;

import com.zving.framework.json.JSONObject;

public class MessageEvent {
	private String topic;
	private JSONObject message;
	private int valid;
	private long expiredate;

	public MessageEvent(String topic, JSONObject message) {
		this.topic = topic;
		this.message = message;
		valid = 0;
		expiredate = 0;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public JSONObject getMessage() {
		return message;
	}

	public void setMessage(JSONObject message) {
		this.message = message;
	}

	public int getValid() {
		return valid;
	}

	public void setValid(int valid) {
		this.valid = valid;
	}

	public long getExpiredate() {
		return expiredate;
	}

	public void setExpiredate(long expiredate) {
		this.expiredate = expiredate;
	}
}
