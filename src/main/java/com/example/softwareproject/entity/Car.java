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
}
