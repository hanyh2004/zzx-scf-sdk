package com.xingyoucai.util;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.xingyoucai.scf.platform.bean.PlatformRequest;
import com.xingyoucai.scf.platform.bean.PlatformResponse;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CallInterfaceUtil {

	private static OkHttpClient getClient() {
		final okhttp3.OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
		return httpBuilder.connectTimeout(10, TimeUnit.SECONDS)
					.writeTimeout(15, TimeUnit.SECONDS).build(); // 设置超时
	}
	
	/**
	 * 供应链金融---调用--->echemi接口 对参数签名
	 * @param url
	 * @param methodName
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static Object callRequestByBean(String url,String methodName,Object params) throws Exception{
		PlatformRequest<Object> req = new PlatformRequest<Object>();
		Gson gson = new Gson();
		String channedId="";
		req.setChannelId(channedId);
		req.setMethod(methodName);
		req.setParams(params);
		req.setSignType("SHA256WithRSA");
		req.setVer("1.0");
		
		StringBuffer sign = new StringBuffer();
		sign.append("channedId=").append(channedId).append("&");
		sign.append("method=").append(methodName).append("&");
		
		if(params!=null){
			sign.append("params=").append(gson.toJson(params)).append("&");
		}
		sign.append("signType=").append("SHA256WithRSA").append("&");
		sign.append("ver=").append("1.0");
		req.setSign(RsaUtil.sign(sign.toString().getBytes()));
		String content = gson.toJson(req);
		RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), content);
		Request request = new Request.Builder()
					.post(body)
					.url(url)
					.build();
		OkHttpClient client = getClient();
		Response response = client.newCall(request).execute();
		if (response.isSuccessful()) {
			String result = response.body().toString();//响应
			PlatformResponse resp = gson.fromJson(result, PlatformResponse.class);
			int statusCode = resp.getStatusCode();
			if(statusCode == 200){
				//校验参数//校验参数
				boolean check = checkSignByResponse(resp);
				if(check){//校验成功
					return resp.getParams();
				}else{//校验失败
				}
			}else{
				//调用失败操作
			}
		}
		return "";
	}
	
	private static boolean checkSignByResponse(PlatformResponse response) throws Exception{
		//签名校验
		String echemiSign =response.getSign();
		StringBuffer sign = new StringBuffer();
		sign.append("channedId=").append(response.getChannelId()).append("&");
		sign.append("method=").append(response.getMethod()).append("&");
		String params = response.getParams();
		if(StringUtils.isNotBlank(params)){
			sign.append("params=").append(params).append("&");
		}
		sign.append("signType=").append(response.getSignType()).append("&");
		sign.append("ver=").append(response.getVer());
		boolean bool = RsaUtil.verify(sign.toString().getBytes(), echemiSign);
		return bool;
	}
	
	
	/**
	 * 根据请求参数签名校验(echemi---调用--->供应链金融)
	 * @param result
	 * @return
	 * @throws Exception
	 */
	public static boolean checkSignByRequest(PlatformRequest<String> requestInfo) throws Exception{
		//签名校验
		String echemiSign =requestInfo.getSign();		
		StringBuffer sign = new StringBuffer();
		sign.append("channedId=").append(requestInfo.getChannelId()).append("&");
		sign.append("method=").append(requestInfo.getMethod()).append("&");
		String params = requestInfo.getParams();
		if(StringUtils.isNotBlank(params)){
			sign.append("params=").append(params).append("&");
		}
		sign.append("signType=").append(requestInfo.getSignType()).append("&");
		sign.append("ver=").append(requestInfo.getVer());
		
		boolean bool = RsaUtil.verify(sign.toString().getBytes(), echemiSign);
		
		return bool;
	}
	
	/**
	 * 对响应的数据进行签名 (供应链金融---响应--->echemi)
	 * @return
	 */
	public static PlatformResponse callReturn(PlatformResponse response){
		
		try {
			StringBuffer sign = new StringBuffer();
			sign.append("channedId=").append(response.getChannelId()).append("&");
			sign.append("method=").append(response.getMethod()).append("&");
			String params = response.getParams();
			if(StringUtils.isNotBlank(params)){
				sign.append("params=").append(params).append("&");
			}
			sign.append("signType=").append(response.getSignType()).append("&");
			sign.append("ver=").append(response.getVer());
			response.setSign(RsaUtil.sign(sign.toString().getBytes()));
		} catch (Exception e) {
			response.setStatusCode(903);
			response.setErrMsg("签名校验失败!");
		}
		
		return response;
	}
}
