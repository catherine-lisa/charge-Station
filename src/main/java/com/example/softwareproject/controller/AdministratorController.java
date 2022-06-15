package com.example.softwareproject.controller;

import com.example.softwareproject.service.ChargingStation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/administrator")
public class AdministratorController {
    @Autowired
    ChargingStation chargingStation;

    @GetMapping("/startChargeStation")
    public void startChargeStation() {
        chargingStation.startStation();
    }

    @GetMapping("/stopChargeStation")
    public void stopChargeStation() {
        chargingStation.stopStation();
    }


}
