package com.example.softwareproject;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.softwareproject.mapper")
public class SoftwareProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoftwareProjectApplication.class, args);
    }

}
