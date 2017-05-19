package com.lhcx.service.impl;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.lhcx.dao.OrderMapper;
import com.lhcx.model.Order;
import com.lhcx.service.IOrderService;
import com.lhcx.utils.MD5Kit;

@Transactional(rollbackFor=Exception.class)
@Service
public class OrderServiceImpl implements IOrderService {

	@Autowired
	private OrderMapper orderMapper;
	
	public int insertSelective(Order order) {
		return orderMapper.insertSelective(order);
	}
	
	public int updateByPrimaryKeySelective(Order order) {
		return orderMapper.updateByPrimaryKeySelective(order);
	}
	
	public Order selectByOrderId(String orderId) {
		return orderMapper.selectByOrderId(orderId);
	}
	
	public boolean create(JSONObject jsonRequest) throws ParseException {
		boolean result = false;
		Order order = new Order(jsonRequest);
		order.setOrderid(MD5Kit.encode(String.valueOf(System.currentTimeMillis()) ));
		if (insertSelective(order) > 0) {
			result = true;
		}
		
		return result;
	}
	
	public Map<String,Object> match(JSONObject jsonRequest) {
		Map<String,Object> result = new HashMap<String, Object>();
		
		return result;
	}
}