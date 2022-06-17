package com.example.softwareproject.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class Customer {
    @TableId(value = "id",type = IdType.AUTO)
    private long id;
    private String username;
    private String password;
    private int jurisdiction;
}
