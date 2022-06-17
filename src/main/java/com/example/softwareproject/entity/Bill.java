package com.example.softwareproject.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;
@Data
public class Bill {
    @TableId(value = "billid")
    private long billid;
    private long userid;
    private long chargingpileid;
    private float chargingnum;
    private Date startdate;
    private Date enddate;
    private float totalfee;

}
