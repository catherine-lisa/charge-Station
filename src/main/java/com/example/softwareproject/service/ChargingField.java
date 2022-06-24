package com.example.softwareproject.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.softwareproject.entity.*;
import com.example.softwareproject.mapper.BillMapper;
import com.example.softwareproject.mapper.CustomerMapper;
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
    private int maxSlowPileNum = 3;
    private int fastPilePower = 30;
    private int slowPilePower = 10;

    private ArrayList<FastChargingPile> fastChargingPiles = new ArrayList<>();

    private ArrayList<SlowChargingPile> slowChargingPiles = new ArrayList<>();

    @Resource
    BillMapper billMapper;

    @Resource
    DetailMapper detailMapper;

    @Resource
    CustomerMapper customerMapper;

    @Autowired
    MyTime myTime;

    public Map<String, Object> checkChargingPileService(int id) {
        Map<String, Object> map = new HashMap<>();
        Car car;
        int pilePower;
        if (id < maxFastPileNum) {
            FastChargingPile chargingPile = fastChargingPiles.get(id);
            if (chargingPile.getChargingQueue().size() == 0) {
                map.put("carId", -1);
                return map;
            }
            car = chargingPile.getFirstCar();
            pilePower = fastPilePower;
        } else {
            id -= maxFastPileNum;
            SlowChargingPile chargingPile = slowChargingPiles.get(id);
            if (chargingPile.getChargingQueue().size() == 0) {
                map.put("carId", -1);
                return map;
            }
            car = chargingPile.getFirstCar();
            pilePower = slowPilePower;
        }
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("id", car.getId());
        Customer customer = customerMapper.selectOne(queryWrapper);
        map.put("carId", customer.getUsername());
        queryWrapper.clear();
        queryWrapper.eq("userid", car.getId());
        queryWrapper.eq("ispay", false);
        Detail detail = detailMapper.selectOne(queryWrapper);
        int chargingTime = (int) car.getChargingNum() * 60 / pilePower;
        map.put("chargingTime", chargingTime);
        map.put("remainingChargeTime", chargingTime - ((myTime.getDate().getTime() - detail.getStartdate().getTime()) / 1000 / 60));
        return map;
    }

    public Map<String, Object> checkChargingPile(int id) {
        Map<String, Object> map = new HashMap<>();
        if (id < maxFastPileNum) {
            FastChargingPile chargingPile = fastChargingPiles.get(id);
            map.put("state", chargingPile.getState());
            map.put("totalChargeTimes", chargingPile.getTotalChargeTimes(chargingPile.getStartTime(), myTime.getDate(), detailMapper, id));
            map.put("totalChargeTime", chargingPile.getTotalChargeTime(chargingPile.getStartTime(), myTime.getDate(), detailMapper, id));
            map.put("totalChargeVol", chargingPile.getTotalChargeVol(chargingPile.getStartTime(), myTime.getDate(), detailMapper, id));
        } else {
            id -= maxFastPileNum;
            SlowChargingPile chargingPile = slowChargingPiles.get(id);
            map.put("state", chargingPile.getState());
            map.put("totalChargeTimes", chargingPile.getTotalChargeTimes(chargingPile.getStartTime(), myTime.getDate(), detailMapper, id));
            map.put("totalChargeTime", chargingPile.getTotalChargeTime(chargingPile.getStartTime(), myTime.getDate(), detailMapper, id));
            map.put("totalChargeVol", chargingPile.getTotalChargeVol(chargingPile.getStartTime(), myTime.getDate(), detailMapper, id));
        }
        return map;
    }

    public Map<String, Object> createReport(int id, Date startTime, Date endTime) {
        Map<String, Object> map = new HashMap<>();
        if (id < maxFastPileNum) {
            FastChargingPile chargingPile = fastChargingPiles.get(id);
            map.put("state", chargingPile.getState());
            map.put("totalChargeTimes", chargingPile.getTotalChargeTimes(startTime, endTime, detailMapper, id));
            map.put("totalChargeTime", chargingPile.getTotalChargeTime(startTime, endTime, detailMapper, id));
            map.put("totalChargeVol", chargingPile.getTotalChargeVol(startTime, endTime, detailMapper, id));
            map.put("chargeFee", chargingPile.getChargeFee(startTime, endTime, detailMapper, id));
            map.put("serviceFee", chargingPile.getServiceFee(startTime, endTime, detailMapper, id));
            map.put("totalFee", chargingPile.getTotalFee(startTime, endTime, detailMapper, id));
        } else {
            id -= maxFastPileNum;
            SlowChargingPile chargingPile = slowChargingPiles.get(id);
            map.put("state", chargingPile.getState());
            map.put("totalChargeTimes", chargingPile.getTotalChargeTimes(startTime, endTime, detailMapper, id));
            map.put("totalChargeTime", chargingPile.getTotalChargeTime(startTime, endTime, detailMapper, id));
            map.put("totalChargeVol", chargingPile.getTotalChargeVol(startTime, endTime, detailMapper, id));
            map.put("chargeFee", chargingPile.getChargeFee(startTime, endTime, detailMapper, id));
            map.put("serviceFee", chargingPile.getServiceFee(startTime, endTime, detailMapper, id));
            map.put("totalFee", chargingPile.getTotalFee(startTime, endTime, detailMapper, id));
        }
        return map;
    }

    public List<Map<String, Object>> checkChargingPileQueue(int id) {
        if (id < maxFastPileNum) {
            FastChargingPile chargingPile = fastChargingPiles.get(id);
            return chargingPile.checkChargingPileQueue(myTime, detailMapper, customerMapper);
        } else {
            id -= maxFastPileNum;
            SlowChargingPile chargingPile = slowChargingPiles.get(id);
            return chargingPile.checkChargingPileQueue(myTime, detailMapper, customerMapper);
        }
    }

    public void changeChargingPileState(String state) {
        for (int i = 0; i < maxFastPileNum; ++i) {
            FastChargingPile chargingPile = fastChargingPiles.get(i);
            chargingPile.state = state;
            chargingPile.startTime = myTime.getDate();
            fastChargingPiles.set(i, chargingPile);
        }
        for (int i = 0; i < maxSlowPileNum; ++i) {
            SlowChargingPile chargingPile = slowChargingPiles.get(i);
            chargingPile.state = state;
            chargingPile.startTime = myTime.getDate();
            slowChargingPiles.set(i, chargingPile);
        }
    }

    public String changeChargingPileState(int id) {
        String info;
        if (id < maxFastPileNum) {
            FastChargingPile chargingPile = fastChargingPiles.get(id);
            if (chargingPile.state.equals("开启")) info = "关闭";
            else info = "开启";
            chargingPile.state = info;
            chargingPile.startTime = myTime.getDate();
            fastChargingPiles.set(id, chargingPile);
        } else {
            id -= maxFastPileNum;
            SlowChargingPile chargingPile = slowChargingPiles.get(id);
            if (chargingPile.state.equals("开启")) info = "关闭";
            else info = "开启";
            chargingPile.state = info;
            chargingPile.startTime = myTime.getDate();
            slowChargingPiles.set(id, chargingPile);
        }
        return info + "成功";
    }

    public ChargingField() {
        for (int i = 0; i < maxFastPileNum; ++i) {
            FastChargingPile chargingPile = new FastChargingPile();
            chargingPile.setId(i);
            chargingPile.setMaxChargingNum(3);
            fastChargingPiles.add(chargingPile);
        }
        for (int i = 0; i < maxSlowPileNum; ++i) {
            SlowChargingPile chargingPile = new SlowChargingPile();
            chargingPile.setId(i);
            chargingPile.setMaxChargingNum(3);
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
                        requestInfo.setQueue_num(i + 1 + "号快充电桩第" + j + "位");
                        requestInfo.setLocation("充电区");
                        requestInfo.setNowCapacity(cars.get(j).getNowCapacity());
//                        System.out.println(requestInfo);
                        return requestInfo;
                    }
                }
            }
        } else {
            for (int i = 0; i < slowChargingPiles.size(); ++i) {
                List<Car> cars = slowChargingPiles.get(i).getChargingQueue();
                for (int j = 0; j < cars.size(); ++j) {
                    if (requestInfo.getId() == cars.get(j).getId()) {
                        requestInfo.setCarState(cars.get(j).getCarState());
                        requestInfo.setQueue_num(i + 1 + "号慢充电桩第" + j + "位");
                        requestInfo.setLocation("充电区");
                        requestInfo.setNowCapacity(cars.get(j).getNowCapacity());
                        System.out.println(requestInfo);
                        return requestInfo;
                    }
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

    public Car cancelRequest(HttpSession session, RequestInfo requestInfo, DetailMapper detailMapper, BillMapper billMapper) {
        if (requestInfo.getChargingMode().equals("fast")) {
            for (int i = 0; i < fastChargingPiles.size(); ++i) {
                List<Car> cars = fastChargingPiles.get(i).getChargingQueue();
                for (int j = 0; j < cars.size(); ++j) {
                    if (cars.get(j).getId() == requestInfo.getId()) {
                        return fastChargingPiles.get(i).cancelRequest(session, requestInfo, detailMapper, billMapper);

                    }
                }
            }
        } else {
            for (int i = 0; i < slowChargingPiles.size(); ++i) {
                List<Car> cars = slowChargingPiles.get(i).getChargingQueue();
                for (int j = 0; j < cars.size(); ++j) {
                    if (cars.get(j).getId() == requestInfo.getId()) {
                        return slowChargingPiles.get(i).cancelRequest(session, requestInfo, detailMapper, billMapper);

                    }
                }
            }
        }
        return null;
    }

    public long getPileIdByInfo(RequestInfo requestInfo) {
        if (Objects.equals(requestInfo.getChargingMode(), "fast")) {
            for (int i = 0; i < fastChargingPiles.size(); ++i) {
                for (int j = 0; j < fastChargingPiles.get(i).getChargingQueue().size(); ++j) {
                    Car car = fastChargingPiles.get(i).getChargingQueue().get(j);
                    if (car.getId() == requestInfo.getId()) {
                        return fastChargingPiles.get(i).getId();
                    }
                }
            }
        } else {
            for (int i = 0; i < slowChargingPiles.size(); ++i) {
                for (int j = 0; j < slowChargingPiles.get(i).getChargingQueue().size(); ++j) {
                    Car car = slowChargingPiles.get(i).getChargingQueue().get(j);
                    if (car.getId() == requestInfo.getId()) {
                        return slowChargingPiles.get(i).getId();
                    }
                }
            }
        }
        return 0;
    }

}
