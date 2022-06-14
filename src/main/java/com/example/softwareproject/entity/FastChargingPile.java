package com.example.softwareproject.entity;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.softwareproject.mapper.BillMapper;
import com.example.softwareproject.mapper.DetailMapper;
import com.example.softwareproject.myinterface.ChargingPile;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.*;

@Data
public class FastChargingPile implements ChargingPile {

    public long id;
    public int maxChargingNum;
    private int fastPilePower=30;
    private double price=0.5;
    private List<Car> chargingQueue=new LinkedList<>();
    public boolean startCharging(RequestInfo requestInfo,DetailMapper detailMapper,BillMapper billMapper)
    {
        Car car =chargingQueue.get(0);
        if(car.getId()!=requestInfo.getId())
            return false;
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                System.out.println(car.getId()+"充电完成"+new Date());
                if(car.getId()==chargingQueue.get(0).getId()) {
                    endCharging(requestInfo,detailMapper,billMapper);

                }//结束充电
            }
        };
        Timer timer=new Timer();
        int delay= (int) (requestInfo.getChargingNum()*1000/fastPilePower);
        timer.schedule(timerTask,delay);

        return true;
    }
    public boolean endCharging(RequestInfo requestInfo,DetailMapper detailMapper,BillMapper billMapper)
    {
        requestInfo.setCarState("chargingDone");
        this.dequeue();
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("userid",requestInfo.getId());
        Bill bill=billMapper.selectOne(queryWrapper);
        bill.setEnddate(new Date());
        //计算费用
        billMapper.updateById(bill);
        Detail detail=detailMapper.selectOne(queryWrapper);
        detail.setEnddate(new Date());
        detailMapper.updateById(detail);
        return true;
    }
    @Override
    public boolean insert(Car car)
    {
        if(chargingQueue.size()>maxChargingNum)
            return false;
        chargingQueue.add(car);
        return true;
    }
    public Car cancelRequest(long id)
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
    public void dequeue()
    {
        chargingQueue.remove(0);
    }
    public Car getFirstCar(){
        return chargingQueue.get(0);
    }
}
