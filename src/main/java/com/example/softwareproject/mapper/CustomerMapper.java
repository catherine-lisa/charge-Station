package com.example.softwareproject.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.softwareproject.entity.Customer;
import org.springframework.stereotype.Service;

@Service
public interface CustomerMapper extends BaseMapper<Customer> {
}
