package com.example.softwareproject.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.softwareproject.entity.*;
import com.example.softwareproject.mapper.BillMapper;
import com.example.softwareproject.mapper.DetailMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.*;

@Service
@Data
public class ChargingField {
    private int maxChargingNum = 5;
    private int maxFastPileNum = 2;
    private int maxSlowPileNum = 2;
    private int fastPilePower = 30;
    private int slowPilePower = 10;
    private ArrayList<FastChargingPile> fastChargingPiles = new ArrayList<>();
    private ArrayList<SlowChargingPile> slowChargingPiles = new ArrayList<>();

    @Resource
    BillMapper billMapper;

    @Autowired
    MyTime myTime;

    public Map<String, Object> checkChargingPileService(int id) {
        Map<String, Object> map = new HashMap<>();
        Car car;
        int pilePower;
        if (id < maxFastPileNum) {
            FastChargingPile chargingPile = fastChargingPiles.get(id);
            car = chargingPile.getFirstCar();
            pilePower = fastPilePower;
        } else {
            SlowChargingPile chargingPile = slowChargingPiles.get(id);
            car = chargingPile.getFirstCar();
            pilePower = slowPilePower;
        }
        map.put("carId", car.getId());
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userid", car.getId());
        queryWrapper.ge("enddate", myTime.getDate());
        Bill bill = billMapper.selectOne(queryWrapper);
        int chargingTime = (int) car.getChargingNum() * 60 / pilePower;
        map.put("chargingTime", chargingTime + "分钟");
        map.put("remainingChargeTime", chargingTime - ((myTime.getDate().getTime() - bill.getStartdate().getTime()) / 1000 / 60) + "分钟");
        return map;
    }

    public Map<String, Object> checkChargingPile(int id) {
        Map<String, Object> map = new HashMap<>();
        if (id < maxFastPileNum) {
            FastChargingPile chargingPile = fastChargingPiles.get(id);
            map.put("state", chargingPile.getState());
            map.put("totalChargeTimes", chargingPile.getTotalChargeTimes(chargingPile.getStartTime(), myTime.getDate()));
            map.put("totalChargeTime", chargingPile.getTotalChargeTime(chargingPile.getStartTime(), myTime.getDate()));
            map.put("totalChargeVol", chargingPile.getTotalChargeVol(chargingPile.getStartTime(), myTime.getDate()));
        } else {
            SlowChargingPile chargingPile = slowChargingPiles.get(id);
            map.put("state", chargingPile.getState());
            map.put("totalChargeTimes", chargingPile.getTotalChargeTimes(chargingPile.getStartTime(), myTime.getDate()));
            map.put("totalChargeTime", chargingPile.getTotalChargeTime(chargingPile.getStartTime(), myTime.getDate()));
            map.put("totalChargeVol", chargingPile.getTotalChargeVol(chargingPile.getStartTime(), myTime.getDate()));
        }
        return map;
    }

    public Map<String,Object> createReport(int id, Date startTime, Date endTime) {
        Map<String, Object> map = new HashMap<>();
        if (id < maxFastPileNum) {
            FastChargingPile chargingPile = fastChargingPiles.get(id);
            map.put("state", chargingPile.getState());
            map.put("totalChargeTimes", chargingPile.getTotalChargeTimes(startTime, endTime));
            map.put("totalChargeTime", chargingPile.getTotalChargeTime(startTime, endTime));
            map.put("totalChargeVol", chargingPile.getTotalChargeVol(startTime, endTime));
        } else {
            SlowChargingPile chargingPile = slowChargingPiles.get(id);
            map.put("state", chargingPile.getState());
            map.put("totalChargeTimes", chargingPile.getTotalChargeTimes(startTime, endTime));
            map.put("totalChargeTime", chargingPile.getTotalChargeTime(startTime, endTime));
            map.put("totalChargeVol", chargingPile.getTotalChargeVol(startTime, endTime));
        }
        return map;
    }

    public List<Map<String, Object>> checkChargingPileQueue(int id) {
        if (id < maxFastPileNum) {
            FastChargingPile chargingPile = fastChargingPiles.get(id);
            return chargingPile.checkChargingPileQueue();
        } else {
            SlowChargingPile chargingPile = slowChargingPiles.get(id);
            return chargingPile.checkChargingPileQueue();
        }
    }

    public void changeChargingPileState(String state) {
        for (int i = 0; i < maxFastPileNum; ++i) {
            FastChargingPile chargingPile = fastChargingPiles.get(i);
            chargingPile.state = state;
            chargingPile.startTime = myTime.getDate();
            fastChargingPiles.set(i, chargingPile);
        }
        for (int i = maxFastPileNum; i < maxChargingNum; ++i) {
            SlowChargingPile chargingPile = slowChargingPiles.get(i);
            chargingPile.state = state;
            chargingPile.startTime = myTime.getDate();
            slowChargingPiles.set(i, chargingPile);
        }
    }

    public ChargingField() {
        for (int i = 0; i < maxFastPileNum; ++i) {
            FastChargingPile chargingPile = new FastChargingPile();
            chargingPile.setId(i);
            chargingPile.setMaxChargingNum(maxFastPileNum);
            fastChargingPiles.add(chargingPile);
        }
        for (int i = maxFastPileNum; i < maxChargingNum; ++i) {
            SlowChargingPile chargingPile = new SlowChargingPile();
            chargingPile.setId(i);
            chargingPile.setMaxChargingNum(maxSlowPileNum);
            slowChargingPiles.add(chargingPile);
        }
    }

    public RequestInfo findTargetCarState(RequestInfo requestInfo) {
        if (requestInfo.getChargingMode().equals("fast")) {
            for (int i = 0; i < fastChargingPiles.size(); ++i) {
                List<Car> cars = fastChargingPiles.get(i).getChargingQueue();
                for (int j = 0; j < cars.size(); ++j) {
                    if (requestInfo.getId() == cars.get(j).getId()) {
                        requestInfo.setCarState(cars.get(j).getCarState());
                        requestInfo.setQueue_num("快充电桩第" + i + "第" + j);
                        requestInfo.setLocation("充电区");
                        requestInfo.setNowCapacity(cars.get(j).getNowCapacity());
                        System.out.println(requestInfo);
                        return requestInfo;
                    }
                }
            }
        } else
            for (int i = 0; i < slowChargingPiles.size(); ++i) {
                List<Car> cars = slowChargingPiles.get(i).getChargingQueue();
                for (int j = 0; j < cars.size(); ++j) {
                    if (requestInfo.getId() == cars.get(j).getId()) {
                        requestInfo.setCarState(cars.get(j).getCarState());
                        requestInfo.setQueue_num("慢充电桩第" + i + "第" + j);
                        requestInfo.setLocation("充电区");
                        requestInfo.setNowCapacity(cars.get(j).getNowCapacity());
                        System.out.println(requestInfo);
                        return requestInfo;
                    }
                }
            }
        return null;
    }

    public FastChargingPile getFastChargingPileById(int id) {
        return fastChargingPiles.get(id);
    }

    public SlowChargingPile getSlowChargingPileById(int id) {
        return slowChargingPiles.get(id);
    }

    public void endRecharge(int chargingPileId, String chargingType) {
        if (chargingType.equals("fast")) {
            FastChargingPile fastChargingPile = fastChargingPiles.get(chargingPileId);
            fastChargingPile.dequeue();
        } else {
            SlowChargingPile slowChargingPile = slowChargingPiles.get(chargingPileId);
            slowChargingPile.dequeue();
        }
    }

    public boolean cancelRequest(HttpSession session, RequestInfo requestInfo, DetailMapper detailMapper, BillMapper billMapper) {
        if (requestInfo.getChargingMode().equals("fast")) {
            for (int i = 0; i < fastChargingPiles.size(); ++i) {
                List<Car> cars = fastChargingPiles.get(i).getChargingQueue();
                for (int j = 0; j < cars.size(); ++j) {
                    if (cars.get(j).getId() == requestInfo.getId()) {
                        Car car = fastChargingPiles.get(i).cancelRequest(session, requestInfo, detailMapper, billMapper);
                        if (car.equals(null)) {
                            return false;
                        } else
                            return true;
                    }
                }
            }
        } else {
            for (int i = 0; i < slowChargingPiles.size(); ++i) {
                List<Car> cars = slowChargingPiles.get(i).getChargingQueue();
                for (int j = 0; j < cars.size(); ++j) {
                    if (cars.get(j).getId() == requestInfo.getId()) {
                        Car car = slowChargingPiles.get(i).cancelRequest(session, requestInfo, detailMapper, billMapper);
                        if (car.equals(null)) {
                            return false;
                        } else
                            return true;
                    }
                }
            }
        }
        return false;
    }

}
