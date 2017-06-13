package com.xingyoucai.scf.platform.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.gson.Gson;
import com.xingyoucai.scf.platform.bean.PlatformRequest;
import com.xingyoucai.scf.platform.bean.PlatformResponse;
import com.xingyoucai.scf.platform.service.LoadService;
import com.xingyoucai.util.CallInterfaceUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/open")
public class ApiController {
	
	@Autowired
	private LoadService loanService;
	
	@RequestMapping("/entry")
	@ResponseBody
	public PlatformResponse execute(HttpServletRequest request, HttpServletResponse response){
		
		PlatformResponse responseInfo = new PlatformResponse();

		PlatformRequest<String> requestInfo = null;
		String param = "";
		boolean workGoon = true;
		try {
			 param = receivePostJson(request);
		} catch (Exception e) {
			log.error("调用失败：", e);
			responseInfo.setStatusCode(901);
			responseInfo.setErrMsg("获取请求参数失败!");
			workGoon = false;
		}
		try{
			requestInfo = JSON.parseObject(param, new TypeReference<PlatformRequest<String>>(){});
		}catch (Exception e) {
			log.error("调用失败：", e);
			responseInfo.setStatusCode(902);
			responseInfo.setErrMsg("参数转换失败!");
			workGoon = false;
		}
		if(requestInfo == null) {
			responseInfo.setStatusCode(902);
			responseInfo.setErrMsg("参数转换失败!");
			workGoon = false;
		}
		
		responseInfo.setMethod(requestInfo.getMethod());
		responseInfo.setVer(requestInfo.getVer());
		responseInfo.setChannelId(requestInfo.getChannelId());
		responseInfo.setSignType(requestInfo.getSignType());
		if(workGoon) {
			try {
				if(!CallInterfaceUtil.checkSignByRequest(requestInfo)) {
					responseInfo.setStatusCode(903);
					responseInfo.setErrMsg("签名校验失败!");
				}else {
					try {
						Gson gson = new Gson();
						Object res  = invokeBusinessMethod(requestInfo.getMethod(), requestInfo.getChannelId(),requestInfo.getParams());
						responseInfo.setStatusCode(200);
						responseInfo.setParams(gson.toJson(res));
					} catch (Exception e) {
						responseInfo.setStatusCode(904);
						responseInfo.setErrMsg("签名失败!");
					}
				}
			} catch (Exception e) {
				responseInfo.setStatusCode(903);
				responseInfo.setErrMsg("签名校验失败!");
			}
		}
		//保存请求参数信息
		//...
		return CallInterfaceUtil.callReturn(responseInfo);
	}
	
	
	/***
	 * 从request流中读取参数
	 * @param request
	 * @return
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 * @throws ClassNotFoundException
	 */
	public static String receivePostJson(HttpServletRequest request) throws Exception{
        // 读取请求内容
        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String line = null;
        StringBuilder sb = new StringBuilder();
        while((line = br.readLine())!=null){
            sb.append(line);
        }
        // 将资料解码
        String reqBody = sb.toString();
        return URLDecoder.decode(reqBody, "UTF-8");
    }
	
	 /**
     * 执行业务方法
     * @param methodName
     * @param param
     * @return
     */
    private Object invokeBusinessMethod(String methodName, String channelId,String param){
    	//执行业务方法
		Object result = null;
    	try{
    		Method method = loanService.getClass().getMethod(methodName,String.class,String.class);
    		result = method.invoke(loanService,channelId,param);
    	}catch (NoSuchMethodException e) {
			log.error("调用失败：", e);
			return new PlatformResponse(904, "调用方法名无效", null);
		}catch (SecurityException e) {
			log.error("调用失败：", e);
			return new PlatformResponse(905, "调用方法名无效", null);
		}catch (Exception e) {
			log.error("调用失败：", e);
			return new PlatformResponse(906, "调用失败", null);
		}

    	return result;
    }
}
