package com.lhcx.utils;

import com.alibaba.fastjson.JSONObject;

/**
 * 验证工具类
 * 
 * @author YangGuang
 * @since 1.0 create on 2017/5/23
 */
public class VerificationUtils {

	/**
	 * 发送验证码参数
	 * 
	 * @return -1表示参数不完整 0表示手机号格式错误 1通过验证
	 */
	public static int sendPhoneCodeValidation(JSONObject jsonRequest) {
		int count = -1;
		String mobile = jsonRequest.getString("phone");
		String userType = jsonRequest.getString("userType");
		if (StringUtils.isOrNotEmpty(mobile)
				&& StringUtils.isOrNotEmpty(userType)) {
			if (!Regex.isPhoneLegal(mobile))
				count = 0;
			count = 1;
		}
		return count;
	}

	/**
	 * 验证验证码参数
	 * 
	 * @return -1表示参数不完整 0表示手机号格式错误 1通过验证
	 */
	public static int checkPhoneCodeValidation(JSONObject jsonRequest) {
		int count = -1;
		String mobile = jsonRequest.getString("phone");
		String userType = jsonRequest.getString("userType");
		String code = jsonRequest.getString("code");
		if (StringUtils.isOrNotEmpty(mobile)
				&& StringUtils.isOrNotEmpty(userType)
				&& StringUtils.isOrNotEmpty(code)) {
			if (!Regex.isPhoneLegal(mobile))
				count = 0;
			count = 1;
		}
		return count;
	}

	/**
	 * 创建订单
	 * 
	 * @return
	 */
	public static boolean createOrderValidation(JSONObject jsonRequest) {
		boolean flag = false;
		// 乘客出发地
		String departure = jsonRequest.getString("Departure");
		String destination = jsonRequest.getString("Destination");
		String depLongitude = jsonRequest.getString("DepLongitude");
		String depLatitude = jsonRequest.getString("DepLatitude");
		String destLongitude = jsonRequest.getString("DestLongitude");
		String destLatitude = jsonRequest.getString("DestLatitude");
		String passengerPhone = jsonRequest.getString("PassengerPhone");
		String orderTime = jsonRequest.getString("OrderTime");
		String dePartTime = jsonRequest.getString("DePartTime");
		String fee = jsonRequest.getString("Fee");
		Integer orderType = jsonRequest.getInteger("OrderType");
		Integer carType = jsonRequest.getInteger("CarType");
		if (StringUtils.isOrNotEmpty(departure)
				&& StringUtils.isOrNotEmpty(destination)
				&& StringUtils.isOrNotEmpty(depLongitude)
				&& StringUtils.isOrNotEmpty(depLatitude)
				&& StringUtils.isOrNotEmpty(destLongitude)
				&& StringUtils.isOrNotEmpty(destLatitude)
				&& StringUtils.isOrNotEmpty(passengerPhone)
				&& StringUtils.isOrNotEmpty(orderTime)
				&& StringUtils.isOrNotEmpty(dePartTime)
				&& StringUtils.isOrNotEmpty(fee)
				&& StringUtils.isOrNotEmpty(orderType.toString())
				&& StringUtils.isOrNotEmpty(carType.toString())) {
			flag = true;
		}
		return flag;
	}

	/**
	 * 撤销订单
	 * 
	 * @param jsonRequest
	 * @return
	 */
	public static boolean cancelOrderValidation(JSONObject jsonRequest) {
		boolean flag = false;
		String operator = jsonRequest.getString("Operator");
		String cancelTypeCode = jsonRequest.getString("CancelTypeCode");
		String cancelReason = jsonRequest.getString("CancelReason");
		if (StringUtils.isOrNotEmpty(operator)
				&& StringUtils.isOrNotEmpty(cancelTypeCode)
				&& StringUtils.isOrNotEmpty(cancelReason)) {
			flag = true;
		}
		return flag;
	}

	/**
	 * 司机接到乘客后发车
	 * 
	 * @param jsonRequest
	 * @return
	 */
	public static boolean departOrderValidation(JSONObject jsonRequest) {
		boolean flag = false;
		String orderId = jsonRequest.getString("OrderId");
		if (StringUtils.isOrNotEmpty(orderId)) {
			flag = true;
		}
		return flag;
	}

	/**
	 * 推送开关
	 * 
	 * @param jsonRequest
	 * @return
	 */
	public static boolean pushButtonValidation(JSONObject jsonRequest) {
		boolean flag = false;
		String longitude = jsonRequest.getString("Longitude");
		String latitude = jsonRequest.getString("Latitude");
		String isDel = jsonRequest.getString("isDel");
		if (StringUtils.isOrNotEmpty(longitude)
				&& StringUtils.isOrNotEmpty(latitude)
				&& StringUtils.isOrNotEmpty(isDel)) {
			flag = true;
		}
		return flag;
	}
	
	/**
	 * 更新经纬度
	 * @param jsonRequest
	 * @return
	 */
	public static boolean updateValidation(JSONObject jsonRequest) {
		boolean flag = false;
		String longitude = jsonRequest.getString("Longitude");
		String latitude = jsonRequest.getString("Latitude");
		if (StringUtils.isOrNotEmpty(longitude)
				&& StringUtils.isOrNotEmpty(latitude)) {
			flag = true;
		}
		return flag;
	}
	
	/**
	 * 登录
	 * @param jsonRequest
	 * @return
	 */
	public static boolean loginValidation(JSONObject jsonRequest) {
		boolean flag = false;
		String phone = jsonRequest.getString("phone");
	    String userType = jsonRequest.getString("userType");
	    String code = jsonRequest.getString("code");
		if (StringUtils.isOrNotEmpty(phone)
				&& StringUtils.isOrNotEmpty(userType)
				&&StringUtils.isOrNotEmpty(code)) {
			flag = true;
		}
		return flag;
	}
}