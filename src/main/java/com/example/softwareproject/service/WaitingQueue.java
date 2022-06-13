package com.example.softwareproject.service;

import com.example.softwareproject.entity.Car;
import com.example.softwareproject.entity.RequestInfo;
import org.springframework.stereotype.Service;


import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Service
public class WaitingQueue {
    private int maxWaitingNum;
    List<Car> fastWaitingQueue=new LinkedList<>();
    List<Car> slowWaitingQueue=new LinkedList<>();
    public boolean isAvailabe(){
        if(slowWaitingQueue.size()+fastWaitingQueue.size()<maxWaitingNum)
        {
            return true;
        }
        else return false;
    }
    public Car getCarByUserId(long id,String chargingMode)
    {
        //将对应队列中车辆信息取出放到另一个队列中
        if(chargingMode.equals("fast"))
        {
            for(int i=0;i<fastWaitingQueue.size();++i)
                if(fastWaitingQueue.get(i).getId()==id)
                {
                    Car car=fastWaitingQueue.get(i);
                    fastWaitingQueue.remove(i);
                    return car;
                }
        }
        else
            for(int i=0;i<slowWaitingQueue.size();++i)
                if(slowWaitingQueue.get(i).getId()==id)
                {
                    Car car=slowWaitingQueue.get(i);
                    slowWaitingQueue.remove(i);
                    return car;
                }
        return null;
    }
    public String fastJoin(RequestInfo requestInfo)
    {
        if(isAvailabe())
        {
            Car car=new Car();
            car.setId(requestInfo.getId());
            car.setCarCapacity(requestInfo.getCarCapacity());
            car.setCarType(requestInfo.getCarType());
            car.setLocation(requestInfo.getLocation());
            car.setNowCapacity(requestInfo.getNowCapacity());
            if(requestInfo.getChargingMode().equals("fast"))
            fastWaitingQueue.add(car);
            else slowWaitingQueue.add(car);
        }
        else return "joinFailed";
        return "success";
    }
    public Car updateWaitingQueue(String mode)
    {
        if(mode.equals("fast"))
        return fastWaitingQueue.remove(0);
        else{
            return slowWaitingQueue.remove(0);
        }
    }
}
