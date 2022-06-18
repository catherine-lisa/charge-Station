package com.example.softwareproject.controller;

import com.example.softwareproject.service.ChargingStation;
import com.example.softwareproject.service.MyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    @GetMapping("/manage")
    public String manage() {
        return "Management";
    }

    @GetMapping("/showInfo")
    public String showChargerInfo() {
        return "showChargerInfo";
    }

    @GetMapping("/showBillList")
    public String showBillList() {
        return "HistoryBill";
    }

    @GetMapping("/startChargeStation")
    @ResponseBody
    public String startChargeStation() {
        chargingStation.startStation();
        return "success";
    }

    @GetMapping("/stopChargeStation")
    @ResponseBody
    public String stopChargeStation() {
        chargingStation.stopStation();
        return "success";
    }

    @GetMapping("/checkChargingPile/service/{id}")
    @ResponseBody
    public Map<String, Object> checkChargingPileService(@PathVariable int id) {
        return chargingStation.checkChargingPileService(id - 1);
    }

    @GetMapping("/checkChargingPile/{id}")
    @ResponseBody
    public Map<String, Object> checkChargingPile(@PathVariable int id) {
        return chargingStation.checkChargingPile(id - 1);
    }

    @GetMapping("/checkChargingPileQueue/{id}")
    @ResponseBody
    public List<Map<String, Object>> checkChargingPileQueue(@PathVariable int id) {
        return chargingStation.checkChargingPileQueue(id - 1);
    }

    @PostMapping("/createReport")
    @ResponseBody
    public Map<String, Object> createReport(@RequestParam int id, @RequestParam String startTimeStr, @RequestParam String endTimeStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startTime = sdf.parse(startTimeStr);
        Date endTime = sdf.parse(endTimeStr);
        return chargingStation.createReport(id - 1, startTime, endTime);
    }

    @GetMapping("/changeChargePileState/{id}")
    @ResponseBody
    public String changeChargePileState(@PathVariable int id) {
        return chargingStation.changeChargePileState(id - 1);
    }
}
