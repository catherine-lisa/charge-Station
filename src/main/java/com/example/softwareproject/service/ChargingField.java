package com.example.softwareproject.service;


import com.example.softwareproject.entity.Car;
import com.example.softwareproject.entity.FastChargingPile;
import com.example.softwareproject.entity.RequestInfo;
import com.example.softwareproject.entity.SlowChargingPile;
import com.example.softwareproject.mapper.BillMapper;
import com.example.softwareproject.mapper.DetailMapper;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public void changeChargingPileState(String state) {
        for (int i = 0; i < maxFastPileNum; ++i) {
            FastChargingPile chargingPile = fastChargingPiles.get(i);
            chargingPile.state = state;
            fastChargingPiles.set(i, chargingPile);
        }
        for (int i = maxFastPileNum; i < maxChargingNum; ++i) {
            SlowChargingPile chargingPile = slowChargingPiles.get(i);
            chargingPile.state = state;
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
    public RequestInfo findTargetCarState(RequestInfo requestInfo)
    {
        if(requestInfo.getChargingMode().equals("fast"))
        {
            for(int i=0;i<fastChargingPiles.size();++i)
            {
                List<Car>cars=fastChargingPiles.get(i).getChargingQueue();
                for(int j=0;j<cars.size();++j)
                {
                    if(requestInfo.getId()==cars.get(j).getId())
                    {
                        requestInfo.setCarState(cars.get(j).getCarState());
                        requestInfo.setQueue_num("Fast"+j);
                        requestInfo.setLocation("充电区");
                        requestInfo.setNowCapacity(cars.get(j).getNowCapacity());
                        return requestInfo;
                    }
                }
            }
        }
        else
            for(int i=0;i<slowChargingPiles.size();++i)
            {
                List<Car>cars=slowChargingPiles.get(i).getChargingQueue();
                for(int j=0;j<cars.size();++j)
                {
                    if(requestInfo.getId()==cars.get(j).getId())
                    {
                        requestInfo.setCarState(cars.get(j).getCarState());
                        requestInfo.setQueue_num("Slow"+j);
                        requestInfo.setLocation("充电区");
                        requestInfo.setNowCapacity(cars.get(j).getNowCapacity());
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

    public boolean cancelRequest(RequestInfo requestInfo, DetailMapper detailMapper, BillMapper billMapper) {
        if (requestInfo.getChargingMode().equals("fast")) {
            for (int i = 0; i < fastChargingPiles.size(); ++i) {
                List<Car> cars = fastChargingPiles.get(i).getChargingQueue();
                for (int j = 0; j < cars.size(); ++j) {
                    if (cars.get(j).getId() == requestInfo.getId()) {
                        Car car = fastChargingPiles.get(i).cancelRequest(requestInfo, detailMapper, billMapper);
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
                        Car car = slowChargingPiles.get(i).cancelRequest(requestInfo, detailMapper, billMapper);
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
