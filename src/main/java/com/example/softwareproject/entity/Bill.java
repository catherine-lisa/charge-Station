package com.example.softwareproject.entity;

import lombok.Data;

import java.util.Date;
@Data
public class Bill {
    private long userid;
    private Date startDate;
    private Date endDate;
    private float totalFee;

}
