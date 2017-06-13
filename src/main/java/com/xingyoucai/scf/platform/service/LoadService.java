package com.xingyoucai.scf.platform.service;



public interface LoadService {
	
	public Object registerNewCustomer(String channelId,Object data)  throws Exception;
	
	public Object loanApply(String channelId,Object data)  throws Exception;
	
	public Object uploadAttachment(String channelId,Object data) throws Exception;
	
	public Object loanApplySubmit(String channelId,Object data) throws Exception;
	
	public Object loanContractConfirm(String channelId,Object data) throws Exception;
	
	public Object refundNotify(String channelId,Object data) throws Exception;

	/*********************************************************************/
	
//	public void loanApplyResultNotify(Loan loan) throws Exception;
//
//	public void paymentNotify(Loan loan) throws Exception;
//
//	public void refundResultNotify(Integer id) throws Exception;
//	
//	public void getOrderHistory(GetOrderHistoryBean orderHistoryBean) throws Exception;
//	
//	public void refundNotify(Integer id) throws Exception;
}
