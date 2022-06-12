package com.example.softwareproject.service;


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
//        waitingQueue.join()
        return "";
    }
}
