package com.example.softwareproject.entity;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.Date;
@Data
public class RequestInfo {
    public long id;
    public long billid;
    public String location;
    public String carType;
    public float carCapacity;
    public float nowCapacity;
    public String chargingMode;
    public float chargingNum;
    public Date requestDate;
    public String carState;//标注是否正处于充电状态
    public String queue_num;
}
