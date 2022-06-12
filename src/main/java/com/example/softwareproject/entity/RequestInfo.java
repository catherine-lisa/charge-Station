package com.example.softwareproject.entity;

import lombok.Data;

import java.util.Date;
@Data
public class RequestInfo {
    public long id;
    public String location;
    public String carType;
    public float carCapacity;
    public float nowCapacity;
    public String chargingMode;
    public float chargingNum;
    public Date requestDate;
}
