package com.lhcx.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lhcx.dao.DriverInfoMapper;
import com.lhcx.model.DriverInfo;
import com.lhcx.service.IDriverInfoService;

@Transactional(rollbackFor=Exception.class)
@Service
public class DriverInfoImpl implements IDriverInfoService {
	
	@Autowired
	private DriverInfoMapper driverInfoMapper;
	
	public DriverInfo selectByPhone(String phone) {
		return driverInfoMapper.selectByPhone(phone);
	}

}
