package com.example.softwareproject.service;


import com.example.softwareproject.entity.Car;
import com.example.softwareproject.entity.FastChargingPile;
import com.example.softwareproject.entity.RequestInfo;
import lombok.Data;
import com.example.softwareproject.entity.SlowChargingPile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Data
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
        Car car=waitingQueue.updateWaitingQueue(requestInfo.getChargingMode());//从等待区队列中对应的充电类型移除第一辆车
        //获取对应充电类型的所有等待桩队列信息
        if(requestInfo.getChargingMode().equals("fast")){
            for(int i=0;i<chargingField.getMaxFastPileNum();++i){

            }
        }
        else{
            for(int i=chargingField.getMaxFastPileNum();i<chargingField.getMaxChargingNum();++i){

            }

        }

        //schedule(),需要实现调度的工作
        //通过调度获取到要插入的目标充电桩，向充电桩中插入Car的信息
        return "success";
    }
    public Car getCarByUserId(long id,String chargingMode)
    {
       return waitingQueue.getCarByUserId(id,chargingMode);
    }

}
