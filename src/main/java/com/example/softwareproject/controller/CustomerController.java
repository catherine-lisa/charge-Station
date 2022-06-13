package com.example.softwareproject.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.softwareproject.entity.*;
import com.example.softwareproject.mapper.BillMapper;
import com.example.softwareproject.mapper.CustomerMapper;
import com.example.softwareproject.mapper.DetailBillMapper;
import com.example.softwareproject.service.ChargingField;
import com.example.softwareproject.service.ChargingStation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Date;

@Controller
@RequestMapping("/customer")
public class CustomerController {
    @Resource
    CustomerMapper customerMapper;
    DetailBillMapper detailBillMapper;
    BillMapper billMapper;
    @Autowired
    ChargingStation chargingStation;
    ChargingField chargingField;


    @GetMapping("/register")
    public String register(){
        return "register";
    }
    @PostMapping("/register")
    @ResponseBody
    public  String register(@RequestParam("username") String username, @RequestParam("password") String password)
    {
        System.out.println(username);
        long id=username.hashCode();
        if(customerMapper.selectById(id)!=null)
            return "userexist";
        Customer newCustomer=new Customer();
        newCustomer.setId(id);
        newCustomer.setUsername(username);
        newCustomer.setPassword(password);
        newCustomer.setJurisdiction(1);
        customerMapper.insert(newCustomer);
        return "success";
    }
    @PostMapping("/logIn")
    @ResponseBody
    public  String logIn(Model model, @RequestParam String username, @RequestParam String password,
                         HttpSession session) {

        int id = username.hashCode();
        Customer targetCustomer =customerMapper.selectById(id);
        if (targetCustomer == null) {
            return "usernotexist";
        }
        if (targetCustomer.getPassword().equals(password)) {
            session.setAttribute("login_state", "yes");
            session.setAttribute("username", username);
            session.setAttribute("userid",targetCustomer.getId());
            return "success";
        }
        return "logInFailed";
    }
    @PostMapping("/requestRecharge")
    @ResponseBody
    //使用ResponseBody，且返回String先去resource里面找是否存在视图，不存在的话封装为json数据回传给前端
    //可以用于ajax的success函数
    public  String requestRecharge(@ModelAttribute RequestInfo requestInfo)
    {
        DetailBill detailBill=new DetailBill();
        detailBill.setUserId(requestInfo.getId());
        detailBill.setStartRquestTime(new Date());
        detailBill.setChargingType(requestInfo.chargingMode);

        return chargingStation.requestRecharge(requestInfo);
    }
    @PostMapping("/enterChargeField")
    @ResponseBody
    public  String enterChargeField(@ModelAttribute RequestInfo requestInfo)
    {

        return chargingStation.updateWaitingQueue(requestInfo.getChargingMode());
    }
    @PostMapping("/startRecharge")
    @ResponseBody
    public  String startRecharge(@PathVariable int chargingPileId,@PathVariable String chargingType)
    {
        Car car;
        if(chargingType.equals("fast")) {
            FastChargingPile fastChargingPile = chargingField.getFastChargingPileById(chargingPileId);
            car=fastChargingPile.getFirstCar();
        }
        else
        {
            SlowChargingPile slowChargingPile=chargingField.getSlowChargingPileById(chargingPileId);
            car=slowChargingPile.getFirstCar();
        }
        car.setCarState("charging");
        Bill bill=new Bill();
        bill.setStartDate(new Date());
        bill.setUserid(car.getId());
        //还需要插入到数据库中进行保存
        billMapper.insert(bill);
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("userId",car.getId());
        DetailBill detailBill=detailBillMapper.selectOne(queryWrapper);
        detailBill.setStartDate(new Date());
        detailBillMapper.updateById(detailBill);

        //开始充电函数
        return "success";
    }
    @PostMapping("/endRecharge")
    @ResponseBody
    public  String endRecharge(@PathVariable int chargingPileId,@PathVariable String chargingType)
    {
        Car car;
        if(chargingType.equals("fast")) {
            FastChargingPile fastChargingPile = chargingField.getFastChargingPileById(chargingPileId);
            car=fastChargingPile.getFirstCar();
        }
        else
        {
            SlowChargingPile slowChargingPile=chargingField.getSlowChargingPileById(chargingPileId);
            car=slowChargingPile.getFirstCar();
        }
        car.setCarState("endcharging");
        Bill bill=billMapper.selectById(car.getId());
        bill.setEndDate(new Date());
        chargingField.endRecharge(chargingPileId,chargingType);

        //结束充电函数
        return "success";
    }
    @PostMapping("/payBill")
    public  String payBill(@ModelAttribute RequestInfo requestInfo)
    {
        Bill bill=billMapper.selectById(requestInfo.getId());

        return "secondpages";
    }
    @PostMapping("/changeRequestMode")
    @ResponseBody
    public  String changeRequestMode(@ModelAttribute RequestInfo requestInfo,@PathVariable String newMode)
    {
        requestInfo.setChargingMode(newMode);

        return "success";
    }
    @PostMapping("/cancelRecharge")
    @ResponseBody
    public  String cancelRecharge()
    {
        return "secondpages";
    }
    @PostMapping("/requestQueue")
    @ResponseBody
    public  String requestQueue()
    {
        return "thirdpages";
    }
    @PostMapping("/requestBill")
    @ResponseBody
    public  String requestBill()
    {
        return "billpages";
    }
}
