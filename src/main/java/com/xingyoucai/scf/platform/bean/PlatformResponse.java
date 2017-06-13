package com.xingyoucai.scf.platform.bean;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlatformResponse {
	private int statusCode;
	private String errMsg;
	private String method;
	private String ver;
	private String channelId;
	private String signType;
	private String sign;
	private String params;
	
	public PlatformResponse(int statusCode,String errMsg,String params) {
		this.statusCode = statusCode;
		this.errMsg = errMsg;
		this.params = params;
	}
	public PlatformResponse() {
	}
	
}
