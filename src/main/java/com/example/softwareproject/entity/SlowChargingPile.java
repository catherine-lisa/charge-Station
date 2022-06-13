package com.example.softwareproject.entity;

import com.example.softwareproject.myinterface.ChargingPile;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Data
public class SlowChargingPile implements ChargingPile {
    public long id;
    public int maxChargingNum;

    private List<Car> chargingQueue=new LinkedList<>();
    @Override
    public boolean insert(Car car)
    {
        if(chargingQueue.size()>maxChargingNum)
            return false;
        chargingQueue.add(car);
        return true;
    }
    public Car changeRequest(long id)
    {
        for(int i=0;i<chargingQueue.size();++i)
            if(chargingQueue.get(i).getId()==id)
            {
                if(i==0)
                {
                    //当前车辆正在充电，无法改变充电模式
                    return null;
                }
                else
                {
                    Car car=chargingQueue.remove(i);
                    return car;
                }
            }
        return null;
    }
    public Car getFirstCar(){
        return chargingQueue.get(0);
    }
    public void dequeue()
    {
        chargingQueue.remove(0);
    }
}
