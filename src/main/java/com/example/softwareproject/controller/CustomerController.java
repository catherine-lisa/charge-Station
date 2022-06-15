package com.example.softwareproject.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.softwareproject.entity.*;
import com.example.softwareproject.mapper.BillMapper;
import com.example.softwareproject.mapper.CustomerMapper;
import com.example.softwareproject.mapper.DetailMapper;
import com.example.softwareproject.service.ChargingField;
import com.example.softwareproject.service.ChargingStation;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableMBeanExport;
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
    @Resource
    DetailMapper detailMapper;
    @Resource
    BillMapper billMapper;
    @Autowired
    ChargingStation chargingStation;
    @Autowired
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


    @GetMapping("/logIn")
    public String logIn()
    {
        return "log_in";
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
        return "log_in_failed";
    }

    @GetMapping("/requestRecharge")
    public String requestRecharge(){
        return "submitRequest";
    }
    @PostMapping("/requestRecharge")
    @ResponseBody
    //使用ResponseBody，且返回String先去resource里面找是否存在视图，不存在的话封装为json数据回传给前端
    //可以用于ajax的success函数
    public String requestRecharge(@ModelAttribute RequestInfo requestInfo)
    {
        Detail detail =new Detail();
        detail.setUserid(requestInfo.getId());
        detail.setStartrequesttime(new Date());
        detail.setChargingtype(requestInfo.chargingMode);
        detailMapper.insert(detail);
        System.out.println(requestInfo);
        return chargingStation.requestRecharge(requestInfo);
    }

    @GetMapping("/enterQueue")
    public String enterQueue(){
        return "waitingQueue";
    }

    @Operation(summary = "前端周期性问询，直到更新为readyCharge")
    @PostMapping("/checkCarState")
    @ResponseBody
    public  RequestInfo checkCarState(@ModelAttribute RequestInfo requestInfo)
    {
        Car car = chargingStation.getWaitingQueue().getCarByInfo(requestInfo);
        if(car.equals(null)==false)//还在等候区
            return requestInfo;
        else return requestInfo;
    }
    @PostMapping("/startRecharge")
    @ResponseBody
    public  String startRecharge(@ModelAttribute RequestInfo requestInfo, @RequestParam int chargingPileId)
    {
        Car car;
        if(requestInfo.getChargingMode().equals("fast")) {
            FastChargingPile fastChargingPile = chargingField.getFastChargingPileById(chargingPileId);
            car=fastChargingPile.getFirstCar();
            fastChargingPile.startCharging(requestInfo,detailMapper,billMapper);
        }
        else
        {
            SlowChargingPile slowChargingPile=chargingField.getSlowChargingPileById(chargingPileId);
            car=slowChargingPile.getFirstCar();
            slowChargingPile.startCharging(requestInfo,detailMapper,billMapper);
        }
        car.setCarState("charging");
        Bill bill=new Bill();
        bill.setStartdate(new Date());
        bill.setUserid(car.getId());
        billMapper.insert(bill);
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("userid",car.getId());
        Detail detail = detailMapper.selectOne(queryWrapper);
        detail.setStartdate(new Date());
        detailMapper.updateById(detail);
        return "success";
    }
    //主动结束充电
    @PostMapping("/endRecharge")
    @ResponseBody
    public  String endRecharge(@ModelAttribute RequestInfo requestInfo,@PathVariable int chargingPileId,@PathVariable String chargingType)
    {
        Car car;
        if(chargingType.equals("fast")) {
            FastChargingPile fastChargingPile = chargingField.getFastChargingPileById(chargingPileId);
            car=fastChargingPile.getFirstCar();
            fastChargingPile.endCharging(requestInfo,detailMapper,billMapper);
        }
        else
        {
            SlowChargingPile slowChargingPile=chargingField.getSlowChargingPileById(chargingPileId);
            car=slowChargingPile.getFirstCar();
            slowChargingPile.endCharging(requestInfo,detailMapper,billMapper);
        }
        car.setCarState("endcharging");
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("userid",car.getId());
        Bill bill=billMapper.selectOne(queryWrapper);
        bill.setEnddate(new Date());
        billMapper.updateById(bill);
        Detail detail=detailMapper.selectOne(queryWrapper);
        detail.setEnddate(new Date());
        detailMapper.updateById(detail);
        chargingField.endRecharge(chargingPileId,chargingType);

        //结束充电函数
        return "success";
    }
    @PostMapping("/payBill")
    public  String payBill(@ModelAttribute RequestInfo requestInfo)
    {
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("userid",requestInfo.getId());
        Detail detail=detailMapper.selectOne(queryWrapper);
        detail.setIspay(true);
        detailMapper.updateById(detail);
        return "secondpages";
    }
    @PostMapping("/changeRequestMode")
    @ResponseBody
    public  String changeRequestMode(@ModelAttribute RequestInfo requestInfo,@PathVariable String newMode)
    {
        //如果car不是null，说明在等候区
        Car car=chargingStation.changeChargeMode(requestInfo.getId(),requestInfo.getChargingMode());
        //不在则说明在充电区
        if(car.equals(null))
        {
            return "changeRequestModeFailed";
        }
        requestInfo.setChargingMode(newMode);
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("userid",requestInfo.getId());
        Detail detail = detailMapper.selectOne(queryWrapper);
        detail.setChargingtype(newMode);
        chargingStation.requestRecharge(requestInfo);
        return "success";
    }
    @PostMapping("/cancelRecharge")
    @ResponseBody
    public  String cancelRecharge(@ModelAttribute RequestInfo requestInfo)
    {
        //需要判断是从等候区还是从充电区取消，以及取消订单和详单
        Car car=chargingStation.changeChargeMode(requestInfo.getId(),requestInfo.getChargingMode());
        if(car.equals(null))
        {
            if(chargingField.cancelRequest(requestInfo,detailMapper,billMapper)==false)
            {
                car.setCarState("endcharging");
                QueryWrapper queryWrapper=new QueryWrapper();
                queryWrapper.eq("userid",car.getId());
                Bill bill=billMapper.selectOne(queryWrapper);
                bill.setEnddate(new Date());
                billMapper.updateById(bill);
                Detail detail=detailMapper.selectOne(queryWrapper);
                detail.setEnddate(new Date());
                detailMapper.updateById(detail);
                return "cancelRechargeSuccessAndCreatedBill";
            }
        }
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("userid",requestInfo.getId());
        billMapper.delete(queryWrapper);
        detailMapper.delete(queryWrapper);

        return "submitRequest";
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
    public  List<Bill> requestBill(@ModelAttribute RequestInfo requestInfo)
    {
        //需要配合前端界面
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("userid",requestInfo.getId());
        List<Bill> bills= billMapper.selectList(queryWrapper);
        return bills;
    }
}
