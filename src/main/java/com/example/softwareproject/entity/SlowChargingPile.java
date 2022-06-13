package com.example.softwareproject.entity;

import com.example.softwareproject.myinterface.ChargingPile;
import lombok.Data;

@Data
public class SlowChargingPile implements ChargingPile {
    public long id;
    public int maxChargingNum;
}
