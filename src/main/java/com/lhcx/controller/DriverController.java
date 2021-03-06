package com.lhcx.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.lhcx.model.DriverInfo;
import com.lhcx.model.DriverLocation;
import com.lhcx.model.Order;
import com.lhcx.model.OrderStatus;
import com.lhcx.model.ResponseCode;
import com.lhcx.model.ResultBean;
import com.lhcx.model.User;
import com.lhcx.model.VerificationLogs;
import com.lhcx.service.IDriverInfoService;
import com.lhcx.service.IDriverLocationService;
import com.lhcx.service.IOrderService;
import com.lhcx.service.IPayCashLogService;
import com.lhcx.service.IUserService;
import com.lhcx.service.IVerificationLogsService;
import com.lhcx.utils.DateUtils;
import com.lhcx.utils.Utils;

/**
 * 司机端controller
 * 
 * @author YangGuang
 *
 */
@Controller
@RequestMapping(value = "/driver")
public class DriverController {
	private static Logger log = Logger.getLogger(DriverController.class);
	@Autowired
	private IOrderService orderService;
	@Autowired
	private HttpSession session;
	@Autowired
	private IDriverInfoService driverInfoService;

	@Autowired
	private IVerificationLogsService verificationLogsService;
	@Autowired
	private IPayCashLogService payCashLogService;
	@Autowired
	private IDriverLocationService driverLocationService;

	@Autowired
	private IUserService userService;

	/**
	 * 获取乘客未完成的订单
	 * 
	 * @param jsonRequest
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/order/noFinished", method = RequestMethod.POST)
	public ResponseEntity<String> noFinished() {
		// 取得参数值
		String jsonpCallback = "";
		ResultBean<?> resultBean = null;
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			User user = (User) session.getAttribute("CURRENT_USER");
			Order newOrder = orderService
					.selectNewOrderByDriverIdentityToken(user
							.getIdentityToken());
			if (newOrder != null) {
				result.put("OrderId", newOrder.getOrderid());
				result.put("Status", newOrder.getStatus());
			}

			resultBean = new ResultBean<Object>(ResponseCode.SUCCESS.value(),
					"获取成功！", result);
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			resultBean = new ResultBean<Object>(ResponseCode.ERROR.value(),
					"获取失败！");
		}

		return Utils.resultResponseJson(resultBean, jsonpCallback);
	}

	/**
	 * 获取司机基本信息
	 * 
	 * @param jsonRequest
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/verification", method = RequestMethod.POST)
	public ResponseEntity<String> info() {
		// 取得参数值
		String jsonpCallback = "";
		ResultBean<?> resultBean = null;
		Map<String, Object> info = new HashMap<String, Object>();
		List<Map<String, Object>> verification = new ArrayList<Map<String, Object>>();
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			User user = (User) session.getAttribute("CURRENT_USER");
			DriverInfo driverInfo = driverInfoService
					.selectByIdentityToken(user.getIdentityToken());
			info.put("phone", user.getUserphone());
			info.put("photo", driverInfo.getPhoto());
			info.put("addressName", driverInfo.getAddressname());
			info.put("driverName", driverInfo.getDrivername());
			info.put("licenseId", driverInfo.getLicenseid());
			info.put("driverNation", driverInfo.getDrivernation());
			info.put("driverNationlity", driverInfo.getDrivernationality());
			info.put("address", driverInfo.getAddress());

			// 证件信息
			info.put("licensePhoto", driverInfo.getLicensephoto());
			info.put("getDriverLicenseDate",
					DateUtils.dateFormat2(driverInfo.getGetdriverlicensedate()));
			info.put("driverLicenseOn",
					DateUtils.dateFormat2(driverInfo.getDriverlicenseon()));
			info.put("driverLicenseOff",
					DateUtils.dateFormat2(driverInfo.getDriverlicenseoff()));
			info.put("fullTimeDriver", driverInfo.getFulltimedriver());
			info.put("VehicleNo", driverInfo.getVehicleNo());

			data.put("status", user.getFlag());
			data.put("info", info);
			// 认证信息
			List<VerificationLogs> verLogsList = verificationLogsService
					.selectByDriverIdentityToken(user.getIdentityToken());
			if (verLogsList.size() == 0) {
				resultBean = new ResultBean<Object>(
						ResponseCode.SUCCESS.value(), "认证中！", data);
			} else {
				if (verLogsList.get(0).getVerificationStatus() == 1) {
					for (VerificationLogs verificationLogs : verLogsList) {
						Map<String, Object> error = new HashMap<String, Object>();
						error.put("codeName",
								verificationLogs.getVerificationName());
						error.put("message",
								verificationLogs.getVerificationContent());
						error.put("codeIndex", verificationLogs.getErrorCode());
						verification.add(error);
					}
				}
				data.put("error", verification);
				resultBean = new ResultBean<Object>(
						ResponseCode.SUCCESS.value(), "获取成功！", data);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			resultBean = new ResultBean<Object>(ResponseCode.ERROR.value(),
					"获取失败！");
		}

		return Utils.resultResponseJson(resultBean, jsonpCallback);
	}

	/**
	 * 司机端更新
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public ResponseEntity<String> update(@RequestBody JSONObject jsonRequest) {
		ResultBean<?> resultBean = null;
		// 取得参数值
		String jsonpCallback = jsonRequest.getString("jsonpCallback");
		try {
			User user = (User) session.getAttribute("CURRENT_USER");
			if (user.getFlag() == 1) {
				userService.updateStatus(user);
			}
			DriverInfo driverInfo = new DriverInfo(jsonRequest);
			driverInfo.setDriverphone(user.getUserphone());
			driverInfo.setIdentityToken(user.getIdentityToken());
			driverInfo.setFlag(2);
			driverInfo.setUpdatetime(DateUtils.currentTimestamp());
			driverInfoService.updateByIdentityTokenSelective(driverInfo);

			resultBean = new ResultBean<Object>(ResponseCode.SUCCESS.value(),
					"更新成功！");

		} catch (Exception e) {
			e.printStackTrace();
			resultBean = new ResultBean<Object>(ResponseCode.ERROR.value(),
					"更新失败！");
		}
		return Utils.resultResponseJson(resultBean, jsonpCallback);
	}

	/**
	 * 司机端流水线
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/cash", method = RequestMethod.POST)
	public ResponseEntity<String> cash() {
		ResultBean<?> resultBean = null;
		// 取得参数值
		String jsonpCallback = null;
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			User user = (User) session.getAttribute("CURRENT_USER");
			String driverIdentityToken = user.getIdentityToken();
			// 当日流水
			result.put("cashToday", "0.00");
			// 总接单数
			int totalCount = orderService
					.selectTotalCountByDriverIdentityToken(driverIdentityToken,
							null);
			int cancelCount = orderService
					.selectTotalCountByDriverIdentityToken(driverIdentityToken,
							OrderStatus.CANCEL.value());
			result.put("orderTotalCount", String.valueOf(totalCount));
			result.put("orderSuccessCount",
					String.valueOf(totalCount - cancelCount));
			// 听单
			long onTime = 0;// 毫秒
			DriverLocation driverLocation = driverLocationService
					.selectByIdentityToken(driverIdentityToken);
			if (driverLocation != null) {
				Date startTime = user.getLogintime();
				Date endTime = driverLocation.getLogoutTime();
				if (startTime != null) {
					long startTimeMil = startTime.getTime();
					long endTimeMil = endTime != null ? endTime.getTime()
							: System.currentTimeMillis();
					if (startTimeMil > endTimeMil) {
						endTimeMil = System.currentTimeMillis();
					}
					onTime = endTimeMil - startTimeMil;
				}
				result.put("onTime", String.valueOf(onTime));
				result.put("pushStatus",
						String.valueOf(driverLocation.getIsdel()));
				result.put("preference",
						String.valueOf(driverLocation.getPreference()));
				result.put("scope", String.valueOf(driverLocation.getScope()));
			} else {
				DriverLocation d = new DriverLocation();
				d.setPositiontime(new Date());
				d.setLongitude("0");
				d.setLatitude("0");
				d.setPhone(user.getUserphone());
				d.setIdentityToken(driverIdentityToken);
				d.setLoginTime(new Date());
				driverLocationService.insertSelective(d);
				result.put("onTime", "");
				result.put("pushStatus", "0");
				result.put("preference", "-1");
				result.put("scope", "5000");
			}
			resultBean = new ResultBean<Object>(ResponseCode.SUCCESS.value(),
					ResponseCode.SUCCESS.message(), result);

		} catch (Exception e) {
			e.printStackTrace();
			resultBean = new ResultBean<Object>(ResponseCode.ERROR.value(),
					ResponseCode.ERROR.message());
		}

		return Utils.resultResponseJson(resultBean, jsonpCallback);
	}

}
