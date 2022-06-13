package com.example.softwareproject.service;


import com.example.softwareproject.entity.FastChargingPile;
import com.example.softwareproject.entity.SlowChargingPile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class ChargingField {
    private int maxChargingNum;
    private int maxFastPileNum;
    private int maxSlowPileNum;
    ArrayList<FastChargingPile> fastChargingPiles;
    ArrayList<SlowChargingPile> slowChargingPiles;
    public ChargingField(){
        maxChargingNum=5;
        maxFastPileNum=2;
        maxSlowPileNum=2;
        fastChargingPiles=new ArrayList<>();
        slowChargingPiles=new ArrayList<>();
        for(int i=0;i<maxFastPileNum;++i)
        {
            FastChargingPile chargingPile=new FastChargingPile();
            chargingPile.setId(i);
            chargingPile.setMaxChargingNum(maxFastPileNum);
            fastChargingPiles.add(chargingPile);
        }
        for(int i=maxFastPileNum;i<maxChargingNum;++i)
        {
            SlowChargingPile chargingPile=new SlowChargingPile();
            chargingPile.setId(i);
            chargingPile.setMaxChargingNum(maxSlowPileNum);
            slowChargingPiles.add(chargingPile);
        }
    }

}
