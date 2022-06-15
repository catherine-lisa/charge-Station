package com.example.softwareproject.entity;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.softwareproject.mapper.BillMapper;
import com.example.softwareproject.mapper.DetailMapper;
import com.example.softwareproject.myinterface.ChargingPile;
import lombok.Data;

import javax.annotation.Resource;
import java.util.*;

@Data
public class SlowChargingPile implements ChargingPile {

    @Resource
    BillMapper billMapper;

    @Resource
    DetailMapper detailMapper;
    public long id;
    public int maxChargingNum;
    private int slowPilePower=30;
    public boolean startCharging(RequestInfo requestInfo,DetailMapper detailMapper,BillMapper billMapper)
    {
        Car car =chargingQueue.get(0);
        if(car.getId()!=requestInfo.getId())
            return false;
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                System.out.println(car.getId()+"充电完成"+new Date());
                if(car.getId()==chargingQueue.get(0).getId())
                    endCharging(requestInfo,detailMapper,billMapper);//结束充电
            }
        };
        Timer timer=new Timer();
        int delay= (int) (requestInfo.getChargingNum()*3600/slowPilePower);
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
        //计算费用
        bill.setEnddate(new Date());
        billMapper.updateById(bill);
        Detail detail=detailMapper.selectOne(queryWrapper);
        detail.setEnddate(new Date());
        detailMapper.updateById(detail);
        return true;
    }
    private List<Car> chargingQueue=new LinkedList<>();
    @Override
    public boolean insert(Car car)
    {
        if(chargingQueue.size()>maxChargingNum)
            return false;
        chargingQueue.add(car);
        return true;
    }
    public Car cancelRequest(RequestInfo requestInfo, DetailMapper detailMapper, BillMapper billMapper)
    {
        for(int i=0;i<chargingQueue.size();++i)
            if(chargingQueue.get(i).getId()==id)
            {
                if(i==0)
                {
                    //当前车辆正在充电，无法改变充电模式
                    endCharging(requestInfo,detailMapper,billMapper);
                    Car car=chargingQueue.remove(i);
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
