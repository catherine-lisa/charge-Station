package com.example.softwareproject.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Customer {
    private long id;
    private String username;
    private String password;
    private int jurisdiction;
}
