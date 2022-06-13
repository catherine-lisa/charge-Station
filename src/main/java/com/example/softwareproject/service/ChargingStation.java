package com.example.softwareproject.service;


import com.example.softwareproject.entity.Car;
import com.example.softwareproject.entity.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChargingStation {
    @Autowired
    WaitingQueue waitingQueue;
    ChargingField chargingField;
    public String requestRecharge(RequestInfo requestInfo)
    {
//        调用join，让传入的信息加入到等待队列
        return waitingQueue.fastJoin(requestInfo);
    }
    public String updateWaitingQueue(RequestInfo requestInfo)
    {
        Car car=waitingQueue.updateWaitingQueue(requestInfo.getChargingMode());
        //schedule(),需要实现调度的工作
        //通过调度获取到要插入的目标充电桩，向充电桩中插入Car的信息
        return "success";
    }
    public Car getCarByUserId(long id,String chargingMode)
    {
       return waitingQueue.getCarByUserId(id,chargingMode);
    }
}
