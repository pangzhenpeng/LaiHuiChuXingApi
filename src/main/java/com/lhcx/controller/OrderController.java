package com.lhcx.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.lhcx.model.DriverLocation;
import com.lhcx.model.Order;
import com.lhcx.model.OrderDetail;
import com.lhcx.model.OrderStatus;
import com.lhcx.model.PushNotification;
import com.lhcx.model.ResponseCode;
import com.lhcx.model.ResultBean;
import com.lhcx.model.response.OrderResponse;
import com.lhcx.service.IDriverLocationService;
import com.lhcx.service.IOrderLogService;
import com.lhcx.service.IOrderService;
import com.lhcx.service.IPushNotificationService;
import com.lhcx.utils.ConfigUtils;
import com.lhcx.utils.DateUtils;
import com.lhcx.utils.JpushClientUtil;
import com.lhcx.utils.PointToDistance;
import com.lhcx.utils.StringUtils;
import com.lhcx.utils.Utils;
import com.lhcx.utils.VerificationUtils;

/**
 * 订单
 * 
 * @author william
 *
 */

@Controller
@RequestMapping(value = "/order")
public class OrderController {
	private static Logger log = Logger.getLogger(OrderController.class);
	@Autowired
	private IOrderService orderService;

	@Autowired
	private IDriverLocationService driverLocationService;

	@Autowired
	private IPushNotificationService pushNotificationService;

	@Autowired
	private IOrderLogService orderLogService;

	/**
	 * 乘客下单，并推送给附近上线司机
	 * 
	 * @param jsonRequest
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public ResponseEntity<String> create(@RequestBody JSONObject jsonRequest,
			HttpServletRequest request) {
		ResultBean<?> resultBean = null;
		// 取得参数值
		String jsonpCallback = jsonRequest.getString("jsonpCallback");
		if (!VerificationUtils.createOrderValidation(jsonRequest)) {
			resultBean = new ResultBean<Object>(
					ResponseCode.PARAMETER_WRONG.value(),
					ResponseCode.PARAMETER_WRONG.message());
		} else {

			// 乘客出发地经度
			double depLongitude = "".equals(jsonRequest
					.getString("DepLongitude")) ? -1
					: Double.parseDouble(jsonRequest.getString("DepLongitude")) / 1000000;
			// 乘客出发地纬度
			double depLatitude = ""
					.equals(jsonRequest.getString("DepLatitude")) ? -1
					: Double.parseDouble(jsonRequest.getString("DepLatitude")) / 1000000;
			// 乘客目的地经度
			double destLongitude = "".equals(jsonRequest
					.getString("DestLongitude")) ? -1
					: Double.parseDouble(jsonRequest.getString("DestLongitude")) / 1000000;
			// 乘客目的地纬度
			double destLatitude = "".equals(jsonRequest
					.getString("DestLatitude")) ? -1
					: Double.parseDouble(jsonRequest.getString("DestLatitude")) / 1000000;
			// 乘客手机号
			String passengerPhone = jsonRequest.getString("PassengerPhone");
			// 乘客发单时间
			long orderTime = Long.parseLong(jsonRequest.getString("OrderTime"));
			// 乘客出发时间
			long dePartTime = Long.parseLong(jsonRequest
					.getString("DePartTime"));
			// 乘客出发地
			String departure = jsonRequest.getString("Departure");
			// 乘客目的地
			String destination = jsonRequest.getString("Destination");
			// 乘客费用
			String fee = jsonRequest.getString("Fee");

			// 订单类型
			Integer orderType = jsonRequest.getInteger("OrderType");

			// 车辆类型
			Integer carType = jsonRequest.getInteger("CarType");

			// 路程距离
			double totalDistance = PointToDistance.distanceOfTwoPoints(
					depLatitude, depLongitude, destLatitude, destLongitude);
			Map<String, Object> result = new HashMap<String, Object>();
			try {
				Order newOrder = orderService
						.selectNewOrderByPhone(passengerPhone);
				if (newOrder == null) {
					String orderId = orderService.create(jsonRequest);
					if (!orderId.equals("")) {
						result.put("OrderId", orderId);
						// 推送内容
						SimpleDateFormat dateFormat = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm");
						DriverLocation d = new DriverLocation();
						d.setLatitude(jsonRequest.getString("DepLatitude"));
						d.setLongitude(jsonRequest.getString("DepLongitude"));
						List<DriverLocation> dLocations = driverLocationService
								.selectList(d);
						if (dLocations.size() > 0) {
							for (DriverLocation driverLocation : dLocations) {
								String driverPhone = driverLocation.getDriverPhone();
								Integer orderStatus = driverLocation.getOrderStatus();
								boolean isReceiving = (OrderStatus.Receiving.value() < orderStatus &&  orderStatus < OrderStatus.ARRIVE.value());
								if ( !Utils.isNullOrEmpty(driverPhone) && isReceiving ) {
									continue;
								}
								
								// 距离
								double distance = driverLocation.getDistance();
								String mobile = driverLocation.getPhone();
								Map<String, String> extrasParam = new HashMap<String, String>();
								extrasParam.put("mobile", passengerPhone);
								extrasParam.put("createTime",
										dateFormat.format(DateUtils
												.toDateTime(orderTime)));
								extrasParam.put("departure", departure);
								extrasParam.put("destination", destination);
								extrasParam.put("departureTime", dateFormat
										.format(DateUtils
												.toDateTime(dePartTime)));
								extrasParam.put("fee", fee);
								extrasParam.put("distance",
										String.valueOf(distance));
								extrasParam.put("totalDistance",
										String.valueOf(totalDistance));
								extrasParam.put("orderId", orderId);
								extrasParam.put("orderType",
										orderType.toString());
								extrasParam.put("carType", carType.toString());

								String content = "【来回出行】有用户发布新的行程订单消息，请前往查看抢单!";
								int flag = JpushClientUtil.getInstance(
										ConfigUtils.JPUSH_APP_KEY,
										ConfigUtils.JPUSH_MASTER_SECRET)
										.sendToRegistrationId("11", mobile,
												content, content, content,
												extrasParam);
								if (flag == 1) {
									PushNotification pushNotification = new PushNotification();
									pushNotification
											.setPushPhone(passengerPhone);
									pushNotification.setReceivePhone(mobile);
									pushNotification.setOrderId(orderId);
									pushNotification.setAlert(content);
									pushNotification.setData(extrasParam
											.toString());
									pushNotificationService
											.insertSelective(pushNotification);
								}
							}
						}
						resultBean = new ResultBean<Object>(
								ResponseCode.SUCCESS.value(),
								ResponseCode.SUCCESS.message(), result);
					} else {
						resultBean = new ResultBean<Object>(
								ResponseCode.RELEASE_ORDER_FAILED.value(),
								ResponseCode.RELEASE_ORDER_FAILED.message());
					}
				} else {
					resultBean = new ResultBean<Object>(
							ResponseCode.RELEASE_ORDER_REPEAT.value(),
							"您当前有未处理的订单,不能发单");
				}
			} catch (Exception e) {
				log.error("create order error by :" + e.getMessage());
				e.printStackTrace();
				resultBean = new ResultBean<Object>(ResponseCode.ERROR.value(),
						ResponseCode.ERROR.message());
			}
		}
		return Utils.resultResponseJson(resultBean, jsonpCallback);
	}

	@ResponseBody
	@RequestMapping(value = "/getFee", method = RequestMethod.POST)
	public ResponseEntity<String> getFee(@RequestBody JSONObject jsonRequest) {
		// 取得参数值
		String jsonpCallback = jsonRequest.getString("jsonpCallback");
		ResultBean<?> resultBean = null;
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result.put("Fee", "123.50");
			resultBean = new ResultBean<Object>(ResponseCode.SUCCESS.value(),
					ResponseCode.SUCCESS.message(), result);

		} catch (Exception e) {
			log.error("create order error by :" + e.getMessage());
			e.printStackTrace();
			resultBean = new ResultBean<Object>(ResponseCode.ERROR.value(),
					ResponseCode.ERROR.message());
		}
		return Utils.resultResponseJson(resultBean, jsonpCallback);
	}

	/**
	 * 撤销订单
	 */
	@ResponseBody
	@RequestMapping(value = "/cancel", method = RequestMethod.POST)
	public ResponseEntity<String> cancel(@RequestBody JSONObject jsonRequest) {
		ResultBean<?> resultBean = null;
		// 获取参数值
		String jsonpCallback = jsonRequest.getString("jsonpCallback");
		try {
			if (!VerificationUtils.cancelOrderValidation(jsonRequest)) {
				resultBean = new ResultBean<Object>(
						ResponseCode.PARAMETER_WRONG.value(),
						ResponseCode.PARAMETER_WRONG.message());
			} else {
				resultBean = orderService.cancel(jsonRequest);
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultBean = new ResultBean<Object>(ResponseCode.ERROR.value(),
					ResponseCode.ERROR.message());
		}

		return Utils.resultResponseJson(resultBean, jsonpCallback);
	}

	/**
	 * 司机抢单
	 * 
	 * @param jsonRequest
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/match", method = RequestMethod.POST)
	public ResponseEntity<String> match(@RequestBody JSONObject jsonRequest) {
		// 取得参数值
		String jsonpCallback = jsonRequest.getString("jsonpCallback");
		ResultBean<?> resultBean = null;
		try {
			resultBean = orderService.match(jsonRequest);
		} catch (Exception e) {
			log.error("order match error by :" + e.getMessage());
			e.printStackTrace();
			resultBean = new ResultBean<Object>(ResponseCode.ERROR.value(),
					ResponseCode.ERROR.message());
		}

		return Utils.resultResponseJson(resultBean, jsonpCallback);
	}

	/**
	 * 司机已到达乘客乘客位置
	 * 
	 * @param jsonRequest
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/reached", method = RequestMethod.POST)
	public ResponseEntity<String> reached(@RequestBody JSONObject jsonRequest) {
		// 取得参数值
		String jsonpCallback = jsonRequest.getString("jsonpCallback");
		ResultBean<?> resultBean = null;
		try {
			resultBean = orderService.reached(jsonRequest);
		} catch (Exception e) {
			log.error("order reached error by :" + e.getMessage());
			e.printStackTrace();
			resultBean = new ResultBean<Object>(ResponseCode.ERROR.value(),
					ResponseCode.ERROR.message());
		}

		return Utils.resultResponseJson(resultBean, jsonpCallback);
	}

	/**
	 * 司机接到乘客后发车
	 * 
	 * @param jsonRequest
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/depart", method = RequestMethod.POST)
	public ResponseEntity<String> depart(@RequestBody JSONObject jsonRequest) {
		// 取得参数值
		String jsonpCallback = jsonRequest.getString("jsonpCallback");
		ResultBean<?> resultBean = null;
		if (!VerificationUtils.departOrderValidation(jsonRequest)) {
			resultBean = new ResultBean<Object>(
					ResponseCode.PARAMETER_WRONG.value(),
					ResponseCode.PARAMETER_WRONG.message());
		} else {
			try {
				resultBean = orderService.depart(jsonRequest);
			} catch (Exception e) {
				log.error("order depart error by :" + e.getMessage());
				e.printStackTrace();
				resultBean = new ResultBean<Object>(ResponseCode.ERROR.value(),
						ResponseCode.ERROR.message());
			}
		}
		return Utils.resultResponseJson(resultBean, jsonpCallback);
	}

	/**
	 * 订单完成
	 * 
	 * @param jsonRequest
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/arrive", method = RequestMethod.POST)
	public ResponseEntity<String> arrive(@RequestBody JSONObject jsonRequest) {
		// 取得参数值
		String jsonpCallback = jsonRequest.getString("jsonpCallback");
		ResultBean<?> resultBean = null;
		if (!VerificationUtils.departOrderValidation(jsonRequest)) {
			resultBean = new ResultBean<Object>(
					ResponseCode.PARAMETER_WRONG.value(),
					ResponseCode.PARAMETER_WRONG.message());
		} else {
			try {
				resultBean = orderService.arrive(jsonRequest);

			} catch (Exception e) {
				log.error("order arrive error by :" + e.getMessage());
				e.printStackTrace();
				resultBean = new ResultBean<Object>(ResponseCode.ERROR.value(),
						ResponseCode.ERROR.message());
			}
		}
		return Utils.resultResponseJson(resultBean, jsonpCallback);
	}

	/**
	 * 订单行程
	 * 
	 * @param jsonRequest
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/info", method = RequestMethod.POST)
	public ResponseEntity<String> info(@RequestBody JSONObject jsonRequest) {
		// 取得参数值
		String jsonpCallback = jsonRequest.getString("jsonpCallback");
		String orderId = jsonRequest.getString("OrderId");
		ResultBean<?> resultBean = null;
		try {
			Order order = orderService.info(orderId);
			OrderResponse result = new OrderResponse(order);

			resultBean = new ResultBean<Object>(ResponseCode.SUCCESS.value(),
					"获取订单信息成功！", result);
		} catch (Exception e) {
			e.printStackTrace();
			resultBean = new ResultBean<Object>(ResponseCode.ERROR.value(),
					"获取订单信息失败！");
		}

		return Utils.resultResponseJson(resultBean, jsonpCallback);
	}
	
	/**
	 * 订单列表
	 */
	@ResponseBody
	@RequestMapping(value = "/orderList", method = RequestMethod.POST)
	public ResponseEntity<String> orderList(@RequestBody JSONObject jsonRequest){
		ResultBean<?> resultBean = null;
		String jsonpCallback = jsonRequest.getString("jsonpCallback");
		String operatorPhone = jsonRequest.getString("OperatorPhone");
		Map<String, Object> result = new HashMap<String, Object>();
		List<OrderDetail> list = new ArrayList<OrderDetail>();
		if (!StringUtils.isOrNotEmpty(operatorPhone)) {
			resultBean = new ResultBean<Object>(
					ResponseCode.PARAMETER_WRONG.value(),
					ResponseCode.PARAMETER_WRONG.message(),result);
		}else {
			result.put("userPhoto", "http://img2.imgtn.bdimg.com/it/u=3142877305,1290741287&fm=214&gp=0.jpg");
			result.put("userName", "大呲花");
			result.put("money", "86.68元");
			OrderDetail od1 = new OrderDetail();
			OrderDetail od2 = new OrderDetail();
			OrderDetail od3 = new OrderDetail();
			od1.setTime("昨天 19:30");
			od1.setFee("15.68元");
			od1.setDeparture("英协大厦");
			od1.setDestination("二七广场");
			od1.setOrderId("44efc11880b387d202d619e3100dae11");
			od2.setTime("今天 20:30");
			od2.setFee("21.84元");
			od2.setDeparture("英协大厦");
			od2.setDestination("中原万达");
			od2.setOrderId("44efc11880b387d202d619e3100dae11");
			od3.setTime("今天 20:50");
			od3.setFee("150.68元");
			od3.setDeparture("北关区");
			od3.setDestination("开发区");
			od3.setOrderId("44efc11880b387d202d619e3100dae11");
			list.add(od1);
			list.add(od2);
			list.add(od3);
			result.put("detailList", list);
			resultBean = new ResultBean<Object>(
					ResponseCode.SUCCESS.value(),
					ResponseCode.SUCCESS.message(),result);
		}
		return Utils.resultResponseJson(resultBean, jsonpCallback);
	}
	
}
