package com.lhcx.dao;

import java.util.List;

import com.lhcx.model.DriverLocation;

public interface DriverLocationMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(DriverLocation record);

    int insertSelective(DriverLocation record);

    DriverLocation selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(DriverLocation record);

    int updateByPrimaryKey(DriverLocation record);
    
    List<DriverLocation> selectList(DriverLocation driverLocation);
}