package com.example.softwareproject.service;


import com.example.softwareproject.entity.Car;
import com.example.softwareproject.entity.FastChargingPile;
import com.example.softwareproject.entity.RequestInfo;
import com.example.softwareproject.entity.SlowChargingPile;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Data
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
        Car car=waitingQueue.updateWaitingQueue(requestInfo.getChargingMode());//从等待区队列中对应的充电类型移除第一辆车
        //获取对应充电类型的所有等待桩队列信息
        //对应匹配充电模式下（快充/慢充），被调度车辆完成充电所需时长（等待时间+自己充电时间）最短。（等待时间=选定充电桩队列中所有车辆完成充电时间之和；自己充电时间=请求充电量/充电桩功率）
        //获取对应充电类型的所有等待桩队列信息，以判断插入哪一桩
        if(requestInfo.getChargingMode().equals("fast")){
            int minPileId=-1;//加入的桩
            float minTime=10000;//按小时记
            //遍历所有同类型桩
            for(int i=0;i<chargingField.getMaxFastPileNum();++i){

                float sumTime=0;
                FastChargingPile pileTemp=chargingField.getFastChargingPileById(i);
                List<Car> chargingQueue=pileTemp.getChargingQueue();
                //当前桩有空位
                if(chargingQueue.size()<pileTemp.maxChargingNum) {
                    //计算本桩已有车辆充电时间之和
                    for (int carNumber = 0; carNumber < chargingQueue.size(); ++carNumber) {
                        sumTime += chargingQueue.get(carNumber).chargingNum / chargingField.getFastPilePower();
                    }
                    //此桩等待时间更短，则加入此桩。更新minPileId和minTime
                    if (sumTime < minTime) {
                        minPileId = i;
                        minTime = sumTime;
                    }
                }
            }
            //要加入的桩即为minPileId对应的桩。将此用户car加入该桩的队列尾
            //保证加入的桩的充电队列一定有空位。
            if(minPileId==-1)
                return "failed";//没有桩有空位。异常请求
            chargingField.getFastChargingPileById(minPileId).insert(car);//向充电桩中插入Car的信息
        }
        else{
            int minPileId=-1;
            float minTime=0;
            for(int i=chargingField.getMaxFastPileNum();i<chargingField.getMaxChargingNum();++i){

                float sumTime=0;
                SlowChargingPile pileTemp=chargingField.getSlowChargingPileById(i);
                List<Car> chargingQueue=pileTemp.getChargingQueue();
                //当前桩有空位
                if(chargingQueue.size()<pileTemp.maxChargingNum) {
                    //计算本桩已有车辆充电时间之和
                    for (int carNumber = 0; carNumber < chargingQueue.size(); ++carNumber) {
                        sumTime += chargingQueue.get(carNumber).chargingNum / chargingField.getSlowPilePower();
                    }
                    //此桩等待时间更短，则加入此桩。更新minPileId和minTime
                    if (sumTime < minTime) {
                        minPileId = i;
                        minTime = sumTime;
                    }
                }
            }
            //要加入的桩即为minPileId对应的桩。将此用户car加入该桩的队列尾
            //保证加入的桩的充电队列一定有空位。
            if(minPileId==-1)
                return "failed";//没有桩有空位。异常请求
            chargingField.getSlowChargingPileById(minPileId).insert(car);//向充电桩中插入Car的信息
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

