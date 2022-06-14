package com.example.softwareproject.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;
@Data
public class Detail {
    @TableId
    private long billid;
    private long userid;
    private boolean ispay;
    private String chargingtype;
    private Date startrequesttime;
    private Date startdate;
    private Date enddate;
    private float totalfee;

}
