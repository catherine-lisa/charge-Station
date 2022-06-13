package com.example.softwareproject.service;

import com.example.softwareproject.entity.Car;
import com.example.softwareproject.entity.RequestInfo;
import org.springframework.stereotype.Service;


import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

@Service
public class WaitingQueue {
    private int maxWaitingNum;
    Queue<Car> fastWaitingQueue=new LinkedList<>();
    Queue<Car> slowWaitingQueue=new LinkedList<>();
    public boolean isAvailabe(){
        if(slowWaitingQueue.size()+fastWaitingQueue.size()<maxWaitingNum)
        {
            return true;
        }
        else return false;
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
        return fastWaitingQueue.remove();
        else{
            return slowWaitingQueue.remove();
        }
    }
}
