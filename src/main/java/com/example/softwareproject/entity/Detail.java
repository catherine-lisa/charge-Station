package com.example.softwareproject.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;
@Data
public class Detail {
    @TableId(value = "billid",type = IdType.AUTO)
    private long billid;
    private long userid;
    private long chargingpileid; //充电桩id
    private boolean ispay;
    private String chargingtype;
    private Date startrequesttime;
    private Date startdate;
    private Date enddate;
    private double timeout;//超时总时间
    private double chargingtotaltime;//充电总时间
    private float chargevol; //充电量
    private float chargefee; //充电费用
    private float servicefee; //服务费用
    private float timeoutfee; //超时费
    private float totalfee;

}
