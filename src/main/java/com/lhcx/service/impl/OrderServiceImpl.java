package com.lhcx.service.impl;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.lhcx.dao.OrderMapper;
import com.lhcx.model.DriverInfo;
import com.lhcx.model.DriverLocation;
import com.lhcx.model.Order;
import com.lhcx.model.OrderLog;
import com.lhcx.model.OrderType;
import com.lhcx.model.PushNotification;
import com.lhcx.model.ResponseCode;
import com.lhcx.model.ResultBean;
import com.lhcx.model.User;
import com.lhcx.service.IDriverInfoService;
import com.lhcx.service.IDriverLocationService;
import com.lhcx.service.IOrderLogService;
import com.lhcx.service.IOrderService;
import com.lhcx.service.IPushNotificationService;
import com.lhcx.utils.ConfigUtils;
import com.lhcx.utils.JpushClientUtil;
import com.lhcx.utils.MD5Kit;
import com.lhcx.utils.DateUtils;

@Transactional(rollbackFor=Exception.class)
@Service
public class OrderServiceImpl implements IOrderService {

	@Autowired
	private OrderMapper orderMapper;
	@Autowired
	private IDriverLocationService driverLocationService;
	@Autowired
	private IDriverInfoService driverInfoService;
	@Autowired  
    private HttpSession session;
	@Autowired
	private IPushNotificationService pushNotificationService;
	@Autowired
	private IOrderLogService orderLogService;
	
	public int insertSelective(Order order) {
		return orderMapper.insertSelective(order);
	}
	
	public int updateByOrderIdSelective(Order order){
		return orderMapper.updateByOrderIdSelective(order);
	}
	
	public Order selectByOrderId(String orderId) {
		Order order = orderMapper.selectByOrderId(orderId);
		List<OrderLog> orderLog = orderLogService.selectByOrderId(orderId);
		order.setOrderLog(orderLog);
		return order;
	}
	
	public String create(JSONObject jsonRequest) throws ParseException {
		String result = "";
		Order order = new Order(jsonRequest);
		String orderId = MD5Kit.encode(String.valueOf(System.currentTimeMillis()));
		order.setOrderid(orderId);
		//保存订单日志记录
		Integer operatortype = jsonRequest.getInteger("OperatorType");
		OrderLog orderLog = new OrderLog();
		orderLog.setOrderid(orderId);
		orderLog.setOperatorphone(order.getPassengerphone());
		orderLog.setOperatortime(new Date());
		orderLog.setOperatorstatus(OrderType.BILL.value());
		orderLog.setOperatordescription(OrderType.BILL.message());
		orderLog.setOperatortype(operatortype);
		
		if (insertSelective(order) > 0 && orderLogService.insertSelective(orderLog) > 0) {
			result = orderId;
		}
		
		return result;
	}
	
	public ResultBean<?> match(JSONObject jsonRequest) throws Exception {
		ResultBean<?> resultBean = null;
		Map<String,Object> result = new HashMap<String, Object>();		
		
		User user = (User)session.getAttribute("CURRENT_USER");
		String driverPhone = user.getUserphone();
		DriverLocation driverLocation = driverLocationService.selectByPhone(driverPhone);
		String orderId = jsonRequest.getString("OrderId");
		Order order = selectByOrderId(orderId);
		
		if (driverLocation != null && driverLocation.getIsdel() == 1) {
			if (order == null || order.getStatus() != OrderType.BILL.value()) {
				resultBean = new ResultBean<Object>(ResponseCode.getError(),
						"该订单已失效或已被接单，如有疑问请联系售后！");
			}else {
				String longitude = jsonRequest.getString("Longitude");
				String latitude = jsonRequest.getString("Latitude");
				Integer operatortype = jsonRequest.getInteger("OperatorType");
				Date distributeTime = new Date();
				
				//step1：保存订单日志记录				
				OrderLog orderLog = new OrderLog();
				orderLog.setOrderid(orderId);
				orderLog.setOperatorphone(driverPhone);
				orderLog.setOperatortime(distributeTime);
				orderLog.setOperatorstatus(OrderType.Receiving.value());
				orderLog.setOperatordescription(OrderType.Receiving.message());
				orderLog.setOldstatus(OrderType.BILL.value());
				orderLog.setOperatortype(operatortype);
				orderLogService.insertSelective(orderLog);
				
				//step2：推送给发单乘客
				DriverInfo driverInfo = driverInfoService.selectByPhone(driverPhone);
				String vehicleNo = driverInfo.getVehicleNo();
				String distributeTimeString =  DateUtils.dateFormat(distributeTime);
				
				String content = "【来回出行】您的行程订单已被接单，请查看!";
				
				String passengerPhone = order.getPassengerphone();
				
				Map<String, String> extrasParam= new HashMap<String, String>();
				extrasParam.put("OrderId", orderId);
				extrasParam.put("passengerPhone", passengerPhone);
				extrasParam.put("DriverPhone", driverPhone);
				extrasParam.put("DistributeTime", distributeTimeString);				
				
				int flag = JpushClientUtil.getInstance(ConfigUtils.PASSENGER_JPUSH_APP_KEY,ConfigUtils.PASSENGER_JPUSH_MASTER_SECRET)
						.sendToRegistrationId("11", passengerPhone,
								content, content, content,
								extrasParam);
				
				if (flag == 1) {
					PushNotification pushNotification = new PushNotification();
					pushNotification.setPushPhone(driverPhone);
					pushNotification.setReceivePhone(passengerPhone);
					pushNotification.setOrderId(orderId);
					pushNotification.setAlert(content);
					pushNotification.setPushType(1);
					pushNotification.setData(extrasParam.toString());
					pushNotificationService.insertSelective(pushNotification);
				}else {
					throw new Exception();
				}
								
				//step3：返回结果字段
				result.put("OrderId", orderId);
				result.put("DriverPhone", driverPhone);
				result.put("VehicleNo", vehicleNo);
				result.put("Longittude", longitude);
				result.put("Latitude", latitude);
				result.put("DistributeTime",distributeTimeString);				

				resultBean = new ResultBean<Object>(ResponseCode.getSuccess(),
						"接单成功！",result);
			}
		} else {
			resultBean = new ResultBean<Object>(ResponseCode.getError(),
					"您没有经营上线，请经营上线后接单！");
		}
				
		return resultBean;
	}
	
	public int cancel(JSONObject jsonRequest) {
		String orderId = jsonRequest.getString("OrderId");
		String operator = jsonRequest.getString("Operator");
		String cancelTypeCode = jsonRequest.getString("CancelTypeCode");
		String cancelReason = jsonRequest.getString("CancelReason");
		
		User user = (User)session.getAttribute("CURRENT_USER");
		Order order = selectByOrderId(orderId);
		
		OrderLog orderLog = new OrderLog();
		orderLog.setOrderid(orderId);
		orderLog.setOperatorphone(user.getUserphone());
		orderLog.setOperatorstatus(OrderType.CANCEL.value());
		orderLog.setOperatordescription(OrderType.CANCEL.message());
		orderLog.setOldstatus(order.getStatus());
		orderLog.setDescription(cancelReason);
		orderLog.setOperatortype(Integer.parseInt(operator));
		orderLog.setOperatortime(new Date());
		orderLog.setCanceltypecode(Integer.parseInt(cancelTypeCode));
		return orderLogService.insertSelective(orderLog);
		
	}
	
	public int depart(JSONObject jsonRequest) {
		String orderId = jsonRequest.getString("OrderId");
		String operator = jsonRequest.getString("Operator");
		
		User user = (User)session.getAttribute("CURRENT_USER");
		Order order = selectByOrderId(orderId);
		
		OrderLog orderLog = new OrderLog();
		orderLog.setOrderid(orderId);
		orderLog.setOperatorphone(user.getUserphone());
		orderLog.setOperatorstatus(OrderType.ABORAD.value());
		orderLog.setOperatordescription(OrderType.ABORAD.message());
		orderLog.setOldstatus(order.getStatus());
		orderLog.setOperatortype(Integer.parseInt(operator));
		orderLog.setOperatortime(new Date());
		return orderLogService.insertSelective(orderLog);
		
	}
	
	public int arrive(JSONObject jsonRequest) {
		String orderId = jsonRequest.getString("OrderId");
		String operator = jsonRequest.getString("Operator");
		
		User user = (User)session.getAttribute("CURRENT_USER");
		Order order = selectByOrderId(orderId);
		
		OrderLog orderLog = new OrderLog();
		orderLog.setOrderid(orderId);
		orderLog.setOperatorphone(user.getUserphone());
		orderLog.setOperatorstatus(OrderType.ARRIVE.value());
		orderLog.setOperatordescription(OrderType.ARRIVE.message());
		orderLog.setOldstatus(order.getStatus());
		orderLog.setOperatortype(Integer.parseInt(operator));
		orderLog.setOperatortime(new Date());
		return orderLogService.insertSelective(orderLog);
		
	}

}
