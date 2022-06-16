package com.example.softwareproject.controller;

import com.example.softwareproject.service.ChargingStation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/checkChargingPile/{id}")
    @ResponseBody
    public Map<String, Object> checkChargingPile(@PathVariable int id) {
        return chargingStation.checkChargingPile(id);
    }

    @GetMapping("/checkChargingPileQueue/{id}")
    @ResponseBody
    public List<Map<String, Object>> checkChargingPileQueue(@PathVariable int id) {
        return chargingStation.checkChargingPileQueue(id);
    }
}
