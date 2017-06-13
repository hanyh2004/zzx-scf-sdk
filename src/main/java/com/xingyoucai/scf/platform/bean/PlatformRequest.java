package com.xingyoucai.scf.platform.bean;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlatformRequest<T> {
	private String method;
	private String ver;
	private String channelId;
	private String signType;
	private String sign;
	
	private T params;
}
