package com.example.softwareproject.entity;

import lombok.Data;

import java.util.Date;
@Data
public class DetailBill {
    private long billId;
    private long userId;
    private boolean isPay;
    private String chargingType;
    private Date startRquestTime;
    private Date startDate;
    private Date endDate;
    private float totalFee;
}
