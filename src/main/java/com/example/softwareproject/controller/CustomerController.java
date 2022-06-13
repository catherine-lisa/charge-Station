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
import java.util.List;

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
        return chargingStation.updateWaitingQueue(requestInfo);
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
        //如果car不是null，说明在等候区
        Car car=chargingStation.getCarByUserId(requestInfo.getId(),requestInfo.getChargingMode());
        //不在则说明在充电区
        if(car.equals(null))
        {
                return "changeRequestModeFailed";
        }
        requestInfo.setChargingMode(newMode);
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("userId",requestInfo.getId());
        DetailBill detailBill=detailBillMapper.selectOne(queryWrapper);
        detailBill.setChargingType(newMode);
        chargingStation.requestRecharge(requestInfo);
        return "success";
    }
    @PostMapping("/cancelRecharge")
    @ResponseBody
    public  String cancelRecharge(@ModelAttribute RequestInfo requestInfo)
    {
        //需要判断是从等候区还是从充电区取消，以及取消订单和详单
        Car car=chargingStation.getCarByUserId(requestInfo.getId(),requestInfo.getChargingMode());
        if(car.equals(null))
        {
            if(chargingField.cancelRequest(requestInfo.getId(),requestInfo.getChargingMode())==false)
            {
                return "cancelRequestModeFailed";
            }
        }
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("userId",requestInfo.getId());
        billMapper.delete(queryWrapper);
        detailBillMapper.delete(queryWrapper);

        return "secondpages";
    }
    @GetMapping("/requestQueue")
    public String requestQueuePages()
    {
        return "thirdpages";
    }
    @PostMapping("/requestQueue")
    @ResponseBody
    public List<Car> requestQueue(@ModelAttribute RequestInfo requestInfo)
    {
        //需要配合前端界面
        if(requestInfo.getCarState().equals("waiting"))
        {
            if(requestInfo.getChargingMode().equals("fast"))
            return chargingStation.getWaitingQueue().getFastWaitingQueue();
            else
                return chargingStation.getWaitingQueue().getSlowWaitingQueue();
        }
        else
        {
            return null;
        }
    }
    @GetMapping("/requestBill")
    public String requestBillPage()
    {
        return "billpages";
    }
    @PostMapping("/requestBill")
    @ResponseBody
    public  Bill requestBill(@ModelAttribute RequestInfo requestInfo)
    {
        //需要配合前端界面
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("userId",requestInfo.getId());
        return billMapper.selectOne(queryWrapper);
    }
}
