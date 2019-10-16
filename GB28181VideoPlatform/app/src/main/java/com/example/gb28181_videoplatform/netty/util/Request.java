package com.example.gb28181_videoplatform.netty.util;

import java.io.Serializable;


public class Request  implements Serializable {

	private static final long serialVersionUID = 5204767100894818949L;
	private String requestId;//消息唯一ID
	private String sessionId; //唯一码
	private String pushNum; //推送编码
	private String type;//消息类型，用着消息路由
	private Object message; //传输消息
	private String token; // 校验
	private byte[][] bytes; //传入二进制
	
	public String getToken() {		
		return  token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public byte[][] getBytes() {
		return bytes;
	}

	public void setBytes(byte[][] bytes) {
		this.bytes = bytes;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getPushNum() {
		return pushNum;
	}

	public void setPushNum(String pushNum) {
		this.pushNum = pushNum;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRequestId() {
		return requestId;
	}
	public Request setRequestId(String requestId) {
		this.requestId = requestId;
		return this;
	}

	public Object getMessage() {
		return message;
	}
	public Request setMessage(Object message) {
		this.message = message;
		return this;
	}

	@Override
	public String toString() {
		return "Request{" +
				"requestId='" + requestId + '\'' +
				", sessionId='" + sessionId + '\'' +
				", pushNum='" + pushNum + '\'' +
				", type='" + type + '\'' +
				", message=" + message +
				'}';
	}

}
