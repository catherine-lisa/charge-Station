package com.example.softwareproject.entity;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class Car {
    public long id;
    public String location;
    public String carType;
    public float carCapacity;
    public float nowCapacity;
    public String carState;//标注是否正处于充电状态
    public float chargingNum;//当前车辆的预计充电情况
}
