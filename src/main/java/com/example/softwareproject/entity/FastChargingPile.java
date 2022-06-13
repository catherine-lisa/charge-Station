package com.example.softwareproject.entity;

import com.example.softwareproject.myinterface.ChargingPile;
import lombok.Data;

import java.util.LinkedList;
import java.util.Queue;

@Data
public class FastChargingPile implements ChargingPile {
    public long id;
    public int maxChargingNum;
    private Queue<Car> chargingQueue=new LinkedList<>();
    @Override
    public boolean insert(Car car)
    {
        if(chargingQueue.size()>maxChargingNum)
            return false;
        chargingQueue.add(car);
        return true;
    }
    public void dequeue()
    {
        chargingQueue.remove();
    }
    public Car getFirstCar(){
        return chargingQueue.peek();
    }
}
