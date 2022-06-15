package com.example.softwareproject.service;


import com.example.softwareproject.entity.Car;
import com.example.softwareproject.entity.FastChargingPile;
import com.example.softwareproject.entity.RequestInfo;
import com.example.softwareproject.entity.SlowChargingPile;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Data
@Service
public class ChargingStation {
    @Autowired
    WaitingQueue waitingQueue;
    @Autowired
    ChargingField chargingField;
    public String requestRecharge(RequestInfo requestInfo)
    {
//        调用join，让传入的信息加入到等待队列
        return waitingQueue.fastJoin(requestInfo);
    }
    public ChargingStation(){
        //设置定时器，用来不断检测等候区来加入到充电区
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                if(waitingQueue.getFastWaitingQueue().size()>0) {
                    System.out.println("开始调度fast队列");
                    updateWaitingQueue("fast");
//                        System.out.println("调度fast队列成功");
                }
//                else System.out.println("fast等待队列无车辆");
                if(waitingQueue.getSlowWaitingQueue().size()>0) {
                    System.out.println("开始调度slow队列");
                    updateWaitingQueue("slow");
                }
//                else System.out.println("slow等待队列无车辆");
            }
        };
        Timer timer=new Timer();
        timer.schedule(timerTask,1,2000);
    }
    public String updateWaitingQueue(String chargingMode)
    {
        Car car=waitingQueue.updateWaitingQueue(chargingMode);//从等待区队列中对应的充电类型移除第一辆车
        System.out.println(car);
        //获取对应充电类型的所有等待桩队列信息
        //对应匹配充电模式下（快充/慢充），被调度车辆完成充电所需时长（等待时间+自己充电时间）最短。（等待时间=选定充电桩队列中所有车辆完成充电时间之和；自己充电时间=请求充电量/充电桩功率）
        //获取对应充电类型的所有等待桩队列信息，以判断插入哪一桩
        if(chargingMode.equals("fast")){
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
            car.carState="chargingField_fast";//新增，改变汽车的属性
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
            car.carState="chargingField_slow";//新增，改变汽车的属性
            chargingField.getSlowChargingPileById(minPileId).insert(car);//向充电桩中插入Car的信息
        }
        //schedule(),需要实现调度的工作
        //通过调度获取到要插入的目标充电桩，向充电桩中插入Car的信息
        return "success";
    }
    public Car changeChargeMode(long id, String chargingMode)
    {
        return waitingQueue.changeChargeMode(id,chargingMode);
    }

}

