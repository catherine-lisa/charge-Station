package com.example.softwareproject.entity;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.softwareproject.mapper.BillMapper;
import com.example.softwareproject.mapper.DetailMapper;
import com.example.softwareproject.myinterface.ChargingPile;
import com.example.softwareproject.service.MyTime;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Data
public class FastChargingPile implements ChargingPile {

    @Autowired
    MyTime myTime;
    public long id;
    public int maxChargingNum;
    private int fastPilePower = 30;
    private double basePrice = 0.8;//服务费

    public String state = "关闭"; //充电桩状态
    private List<Car> chargingQueue = new LinkedList<>();


    public boolean startCharging(RequestInfo requestInfo,DetailMapper detailMapper,BillMapper billMapper)
    {
        Car car =chargingQueue.get(0);
        if(car.getId()!=requestInfo.getId())
            return false;
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                System.out.println(car.getId()+"充电完成"+myTime.getDate());
                //判断用户是否提前结束充电
                if(car.getId()==chargingQueue.get(0).getId()) {
                    endCharging(requestInfo,detailMapper,billMapper);
                }//结束充电
            }
        };
        Timer timer=new Timer();
        int delay= (int) (requestInfo.getChargingNum()*3600*1000/fastPilePower);
        System.out.println(delay);
        timer.schedule(timerTask,delay);

        return true;
    }

    public static boolean isEffectiveDate(Date nowTime, Date startTime, Date endTime) {
        if (nowTime.getTime() == startTime.getTime()
                || nowTime.getTime() == endTime.getTime()) {
            return true;
        }

        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);

        Calendar begin = Calendar.getInstance();
        begin.setTime(startTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);

        return date.after(begin) && date.before(end);
    }

    public double getChargePrice(Date date) {
        //按照开始和结束中，价高的算
        String strTime1 = "10:00";
        String strTime2 = "15:00";
        String strTime3 = "18:00";
        String strTime4 = "21:00";
        String strTime5 = "23:00";
        String strTime6 = "7:00";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String now = sdf.format(date);
        try {
            Date nowTime = sdf.parse(now);
            Date time1 = sdf.parse(strTime1);
            Date time2 = sdf.parse(strTime2);
            Date time3 = sdf.parse(strTime3);
            Date time4 = sdf.parse(strTime4);
            Date time5 = sdf.parse(strTime5);
            Date time6 = sdf.parse(strTime6);
            if (isEffectiveDate(nowTime, time1, time2) || isEffectiveDate(nowTime, time3, time4))
                return 1.0;
            if (isEffectiveDate(nowTime, time6, time1) || isEffectiveDate(nowTime, time2, time3) || isEffectiveDate(nowTime, time4, time5))
                return 0.7;
            if (isEffectiveDate(nowTime, time5, time6))
                return 0.4;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 1.0;
    }

    public boolean endCharging(RequestInfo requestInfo, DetailMapper detailMapper, BillMapper billMapper) {
        requestInfo.setCarState("chargingDone");
        this.dequeue();
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("userid",requestInfo.getId());
        Bill bill=billMapper.selectOne(queryWrapper);
        Detail detail=detailMapper.selectOne(queryWrapper);
        bill.setEnddate(myTime.getDate());
        //计算费用
        double chargePrice;
        if (getChargePrice(bill.getStartdate()) > getChargePrice(bill.getEnddate()))
            chargePrice = getChargePrice(bill.getStartdate());
        else chargePrice = getChargePrice(bill.getEnddate());
        double totalFee = (basePrice + chargePrice) * fastPilePower * (bill.getEnddate().getTime() - bill.getStartdate().getTime()) / 1000 / 3600;//获取充电度数,再乘以服务费
        bill.setTotalfee((float) totalFee);
        detail.setTotalfee((float) totalFee);
        billMapper.updateById(bill);

        detail.setEnddate(myTime.getDate());
        detailMapper.updateById(detail);
        return true;
    }

    @Override
    public boolean insert(Car car) {
        if (chargingQueue.size() > maxChargingNum)
            return false;
        chargingQueue.add(car);
        chargingQueue.get(0).setCarState("readyCharge");
        return true;
    }

    public Car cancelRequest(RequestInfo requestInfo, DetailMapper detailMapper, BillMapper billMapper) {
        for (int i = 0; i < chargingQueue.size(); ++i)
            if (chargingQueue.get(i).getId() == id) {
                if (i == 0) {
                    endCharging(requestInfo, detailMapper, billMapper);
                    Car car = chargingQueue.remove(i);
                    return null;
                } else {
                    Car car = chargingQueue.remove(i);
                    return car;
                }
            }
        return null;
    }

    public void dequeue() {
        chargingQueue.remove(0);
        if(chargingQueue.size()>0)
        chargingQueue.get(0).setCarState("readyCharge");
    }

    public Car getFirstCar() {
        return chargingQueue.get(0);
    }
}
