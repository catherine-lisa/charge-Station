package com.example.softwareproject.service;


import com.example.softwareproject.entity.Car;
import com.example.softwareproject.entity.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChargingStation {
    @Autowired
    WaitingQueue waitingQueue;
    public String requestRecharge(RequestInfo requestInfo)
    {
//        调用join，让传入的信息加入到等待队列
        return waitingQueue.join(requestInfo);
    }
    public String updateWaitingQueue()
    {
        Car car=waitingQueue.updateWaitingQueue();
        //schedule(),需要实现调度的工作
        //向充电桩中插入Car的信息
        return "success";
    }
}
