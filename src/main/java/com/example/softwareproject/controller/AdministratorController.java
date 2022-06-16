package com.example.softwareproject.controller;

import com.example.softwareproject.service.ChargingStation;
import com.example.softwareproject.service.MyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/administrator")
public class AdministratorController {
    @Autowired
    ChargingStation chargingStation;
    @Autowired
    MyTime myTime;
    @GetMapping("/startChargeStation")
    public void startChargeStation() {
        chargingStation.startStation();
    }

    @GetMapping("/stopChargeStation")
    public void stopChargeStation() {
        chargingStation.stopStation();
    }

    @GetMapping("/checkChargingPile/service/{id}")
    @ResponseBody
    public Map<String, Object> checkChargingPileService(@PathVariable int id) {
        return chargingStation.checkChargingPileService(id);
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

    @PostMapping("/createReport")
    @ResponseBody
    public Map<String, Object> createReport(@RequestParam int id, @RequestParam Date startTime, @RequestParam Date endTime) {
        return chargingStation.createReport(id, startTime, endTime);
    }
}
