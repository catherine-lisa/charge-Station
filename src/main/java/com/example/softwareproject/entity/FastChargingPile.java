package com.example.softwareproject.entity;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.softwareproject.mapper.BillMapper;
import com.example.softwareproject.mapper.CustomerMapper;
import com.example.softwareproject.mapper.DetailMapper;
import com.example.softwareproject.myinterface.ChargingPile;
import com.example.softwareproject.service.MyTime;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Data
public class FastChargingPile implements ChargingPile {


    public long id;
    public int maxChargingNum;
    private int fastPilePower = 30;
    private double basePrice = 0.8;//服务费

    public String state = "关闭"; //充电桩状态

    public Date startTime; //充电桩启动时间

    private List<Car> chargingQueue = new LinkedList<>();
    public Timer timer;
    public Timer timer1;

    public List<Map<String, Object>> checkChargingPileQueue(MyTime myTime, DetailMapper detailMapper, CustomerMapper customerMapper) {
        List<Map<String, Object>> list = new ArrayList<>();
        int size = chargingQueue.size();
        for (int i = 1; i < size; ++i) {
            Map<String, Object> map = new HashMap<>();
            Car car = chargingQueue.get(i);
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("id", car.getId());
            Customer customer = customerMapper.selectOne(queryWrapper);
            map.put("carId", customer.getUsername());
            map.put("carCapacity", car.getCarCapacity());
            map.put("chargingNum", car.getChargingNum());
            queryWrapper.clear();
            queryWrapper.eq("userid", car.getId());
            queryWrapper.isNull("startdate");
            Detail detail = detailMapper.selectOne(queryWrapper);//用户重复充电会出问题
            map.put("queueTime", (myTime.getDate().getTime() - detail.getStartrequesttime().getTime()) / 1000 / 60);
            list.add(map);
        }
        return list;
    }

    public int getTotalChargeTimes(Date startTime, Date endTime, DetailMapper detailMapper, int id) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.between("enddate", startTime, endTime);
        queryWrapper.eq("chargingpileid", id);
        queryWrapper.eq("chargingtype", "fast");
        return detailMapper.selectCount(queryWrapper);
    }

    public double getTotalChargeTime(Date startTime, Date endTime, DetailMapper detailMapper, int id) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.between("enddate", startTime, endTime);
        System.out.println(startTime+" "+endTime);
        queryWrapper.eq("chargingpileid", id);
        queryWrapper.eq("chargingtype", "fast");
        List<Detail> detailList = detailMapper.selectList(queryWrapper);
        System.out.println(detailList);
        double totalChargeTime = 0;
        int size = detailList.size();
        for (int i = 0; i < size; ++i) {
            totalChargeTime += detailList.get(i).getChargingtotaltime();
        }
        return totalChargeTime;
    }

    public float getTotalChargeVol(Date startTime, Date endTime, DetailMapper detailMapper, int id) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.between("enddate", startTime, endTime);
        queryWrapper.eq("chargingpileid", id);
        queryWrapper.eq("chargingtype", "fast");
        List<Detail> detailList = detailMapper.selectList(queryWrapper);
        float totalChargeVol = 0;
        int size = detailList.size();
        for (int i = 0; i < size; ++i) {
            totalChargeVol += detailList.get(i).getChargevol();
        }
        return totalChargeVol;
    }

    public float getChargeFee(Date startTime, Date endTime, DetailMapper detailMapper, int id) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.between("enddate", startTime, endTime);
        queryWrapper.eq("chargingpileid", id);
        queryWrapper.eq("chargingtype", "fast");
        List<Detail> detailList = detailMapper.selectList(queryWrapper);
        float chargeFee = 0;
        int size = detailList.size();
        for (int i = 0; i < size; ++i) {
            chargeFee += detailList.get(i).getChargefee();
        }
        return chargeFee;
    }

    public float getServiceFee(Date startTime, Date endTime, DetailMapper detailMapper, int id) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.between("enddate", startTime, endTime);
        queryWrapper.eq("chargingpileid", id);
        queryWrapper.eq("chargingtype", "fast");
        List<Detail> detailList = detailMapper.selectList(queryWrapper);
        float serviceFee = 0;
        int size = detailList.size();
        for (int i = 0; i < size; ++i) {
            serviceFee += detailList.get(i).getServicefee();
        }
        return serviceFee;
    }

    public float getTotalFee(Date startTime, Date endTime, DetailMapper detailMapper, int id) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.between("enddate", startTime, endTime);
        queryWrapper.eq("chargingpileid", id);
        queryWrapper.eq("chargingtype", "fast");
        List<Detail> detailList = detailMapper.selectList(queryWrapper);
        float totalFee = 0;
        int size = detailList.size();
        for (int i = 0; i < size; ++i) {
            totalFee += detailList.get(i).getTotalfee();
        }
        return totalFee;
    }

    public boolean startCharging(MyTime myTime, HttpSession session, RequestInfo requestInfo, DetailMapper detailMapper, BillMapper billMapper) {
        Car car = chargingQueue.get(0);
        if (car.getId() != requestInfo.getId())
            return false;
        timer = new Timer();
        timer1 = new Timer();

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("billid", requestInfo.getBillid());
        Detail detail = detailMapper.selectOne(queryWrapper);
        detail.setChargingpileid(id);
        detailMapper.updateById(detail);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println(car.getId() + "充电完成");
                if (chargingQueue.size() == 0)
                    return;
                //判断用户是否提前结束充电
                if (car.getId() == chargingQueue.get(0).getId()) {
                    timer1.cancel();
                    endCharging(myTime, session, requestInfo, detailMapper, billMapper);
                }//结束充电
            }
        };
        TimerTask timerTask1 = new TimerTask() {
            @Override
            public void run() {
                if (chargingQueue.size() == 0) {
                    timer1.cancel();
                    return;
                }
                Car nowCar = chargingQueue.get(0);
                if (car.getId() == nowCar.getId()) {
                    chargingQueue.get(0).setNowCapacity(chargingQueue.get(0).getNowCapacity() + ((float) fastPilePower) / 360);
//                    System.out.println(chargingQueue.get(0));
                } else//当前车辆变化
                    timer1.cancel();
            }
        };
        int delay = (int) (requestInfo.getChargingNum() * 360 * 1000 / fastPilePower);
        System.out.println(delay);
        timer.schedule(timerTask, delay);
        timer1.schedule(timerTask1, 0, 1 * 1000);//10s
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

    public boolean endCharging(MyTime myTime, HttpSession session, RequestInfo requestInfo, DetailMapper detailMapper, BillMapper billMapper) {

        if (requestInfo.getCarState() == "chargingDoneByUser") {
            timer.cancel();
            timer1.cancel();
        }
        requestInfo.setCarState("chargingDone");
        this.dequeue();
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("billid", requestInfo.getBillid());
        Bill bill = billMapper.selectOne(queryWrapper);
        Detail detail = detailMapper.selectOne(queryWrapper);
        bill.setEnddate(myTime.getDate());
        //计算费用
        double chargePrice;
        if (getChargePrice(bill.getStartdate()) > getChargePrice(bill.getEnddate()))
            chargePrice = getChargePrice(bill.getStartdate());
        else chargePrice = getChargePrice(bill.getEnddate());
        double totalTime = (double) (bill.getEnddate().getTime() - bill.getStartdate().getTime()) / 1000 / 60;
        String totalTime_str = String.format("%.1f", totalTime); //以字符串形式保留位数，此处保留1位小数
        double totalTime_1 = Double.parseDouble(totalTime_str);
        detail.setChargingtotaltime(totalTime_1);
        detail.setChargevol((float) fastPilePower * (bill.getEnddate().getTime() - bill.getStartdate().getTime()) / 1000 / 3600);
        double serviceFee = basePrice * fastPilePower * (bill.getEnddate().getTime() - bill.getStartdate().getTime()) / 1000 / 3600;
        double chargeFee = chargePrice * fastPilePower * (bill.getEnddate().getTime() - bill.getStartdate().getTime()) / 1000 / 3600;
        double totalFee = serviceFee + chargeFee;//获取充电度数,再乘以服务费
        //System.out.println(detail.getChargevol());
        bill.setChargingnum(detail.getChargevol());
        bill.setTotalfee((float) totalFee);
        detail.setTotalfee((float) totalFee);
        detail.setServicefee((float) serviceFee);
        detail.setChargefee((float) chargeFee);
        billMapper.updateById(bill);
        detail.setEnddate(myTime.getDate());
        detailMapper.updateById(detail);
        session.removeAttribute("requestInfo");
        session.setAttribute("requestInfo", requestInfo);//更新到session中
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

    public Car cancelRequest(HttpSession session, RequestInfo requestInfo, DetailMapper detailMapper, BillMapper billMapper) {
        for (int i = 0; i < chargingQueue.size(); ++i)
            if (chargingQueue.get(i).getId() == requestInfo.getId()) {
                if (i == 0) {
                    if (!Objects.equals(chargingQueue.get(i).getCarState(), "charging"))
                        return chargingQueue.remove(i);
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
        if (chargingQueue.size() > 0)
            chargingQueue.get(0).setCarState("readyCharge");
    }

    public Car getFirstCar() {

        return chargingQueue.get(0);
    }
}
