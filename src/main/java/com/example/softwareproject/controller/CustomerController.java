package com.example.softwareproject.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.example.softwareproject.entity.*;
import com.example.softwareproject.mapper.BillMapper;
import com.example.softwareproject.mapper.CustomerMapper;
import com.example.softwareproject.mapper.DetailMapper;
import com.example.softwareproject.service.ChargingField;
import com.example.softwareproject.service.ChargingStation;
import com.example.softwareproject.service.MyTime;
import com.example.softwareproject.service.WaitingQueue;
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
import java.util.Objects;

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
    WaitingQueue waitingQueue;
    @Autowired
    ChargingField chargingField;
    @Autowired
    ChargingStation chargingStation;

    @Autowired
    MyTime myTime;
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
    public  String logIn( @RequestParam String username, @RequestParam String password,
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

    @GetMapping("/user")
    public String user()
    {
        return "user";
    }



    @GetMapping("/requestRecharge")
    public String requestRecharge(){
        return "submitRequest";
    }
    @PostMapping("/requestRecharge")
    @ResponseBody
    //使用ResponseBody，且返回String先去resource里面找是否存在视图，不存在的话封装为json数据回传给前端
    //可以用于ajax的success函数
    public String requestRecharge(Model model,@ModelAttribute RequestInfo requestInfo,HttpSession session)
    {
        model.addAttribute(requestInfo);
        session.setAttribute("requestInfo",requestInfo);
        Detail detail =new Detail();
        detail.setUserid(requestInfo.getId());
        detail.setStartrequesttime(myTime.getDate());
        detail.setChargingtype(requestInfo.chargingMode);
        detailMapper.insert(detail);
        System.out.println(requestInfo);
        return chargingStation.requestRecharge(requestInfo);
    }

    @GetMapping("/enterQueue")
    public String enterQueue(HttpSession session,Model model){
//        System.out.println("enterQUe"+session.getAttribute("requestInfo"));
        model.addAttribute(session.getAttribute("requestInfo"));
        return "waitingQueue";
    }

    @Operation(summary = "前端周期性问询，直到更新为readyCharge")
    @PostMapping("/checkCarState")
    @ResponseBody
    public  RequestInfo checkCarState(HttpSession session)
    {
        //System.out.println("test"+session.getAttribute("requestInfo"));
        RequestInfo requestInfo=(RequestInfo) session.getAttribute("requestInfo");
        if(Objects.equals(requestInfo.getCarState(), "chargingDone"))
        {
            return requestInfo;
        }
        System.out.println(requestInfo);
        Car car = chargingStation.getWaitingQueue().getCarByInfo(requestInfo);
        if(car!=null)//还在等候区
        {

            if(requestInfo.getChargingMode().equals("fast"))
            {
                requestInfo.setQueue_num("快充队列"+chargingStation.getWaitingQueue().getFastWaitingQueue().size());
            }
            else
                requestInfo.setQueue_num("慢充队列"+chargingStation.getWaitingQueue().getSlowWaitingQueue().size());
            return requestInfo;
        }
        else return chargingField.findTargetCarState(requestInfo);//查看request中的carState来变化前端
    }

    @GetMapping("/startRecharge")
    public String startRecharge(){
        return "Charge";
    }
    @PostMapping("/startRecharge")
    @ResponseBody
    public String startRecharge(HttpSession session)
    {
        Car car;
        RequestInfo requestInfo=(RequestInfo) session.getAttribute("requestInfo");
        System.out.println("Start"+requestInfo);
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("userid",requestInfo.getId());
        Detail detail = detailMapper.selectOne(queryWrapper);
        int chargingPileId=(int)detail.getChargingpileid();

        if(requestInfo.getChargingMode().equals("fast")) {
            FastChargingPile fastChargingPile = chargingField.getFastChargingPileById(chargingPileId);
            car=fastChargingPile.getFirstCar();
            fastChargingPile.startCharging(session,requestInfo,detailMapper,billMapper);
        }
        else
        {
            SlowChargingPile slowChargingPile=chargingField.getSlowChargingPileById(chargingPileId);
            car=slowChargingPile.getFirstCar();
            slowChargingPile.startCharging(session,requestInfo,detailMapper,billMapper);
        }
        car.setCarState("charging");
        Bill bill=new Bill();
        bill.setStartdate(myTime.getDate());
        bill.setUserid(car.getId());
        billMapper.insert(bill);
//        QueryWrapper queryWrapper=new QueryWrapper();
//        queryWrapper.eq("userid",car.getId());
//        Detail detail = detailMapper.selectOne(queryWrapper);
        detail.setStartdate(myTime.getDate());
        detailMapper.updateById(detail);
        return "success";
    }
    //主动结束充电
    @PostMapping("/endRecharge")
    @ResponseBody
    public  String endRecharge(HttpSession session)
    {
        Car car;
        RequestInfo requestInfo=(RequestInfo) session.getAttribute("requestInfo");
        System.out.println(requestInfo.getCarState()+"startController");
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("userid",requestInfo.getId());
        Detail detail = detailMapper.selectOne(queryWrapper);
        int chargingPileId=(int)detail.getChargingpileid();
        String chargingType=requestInfo.getChargingMode();
        if(requestInfo.getCarState()=="chargingDone")
        {
            Date now=myTime.getDate();

            double timeout=(now.getTime()-detail.getEnddate().getTime())/1000/60;
            double timeoutFee=timeout*1;//设置超时费
            detail.setTimeoutfee((float) timeoutFee);
            detailMapper.updateById(detail);
            return "success";
        }
        if(chargingType.equals("fast")) {
            FastChargingPile fastChargingPile = chargingField.getFastChargingPileById(chargingPileId);
            car=fastChargingPile.getFirstCar();
            fastChargingPile.endCharging(session,requestInfo,detailMapper,billMapper);
        }
        else
        {
            SlowChargingPile slowChargingPile=chargingField.getSlowChargingPileById(chargingPileId);
            car=slowChargingPile.getFirstCar();
            slowChargingPile.endCharging(session,requestInfo,detailMapper,billMapper);
        }
        car.setCarState("endcharging");

        Bill bill=billMapper.selectOne(queryWrapper);
        bill.setEnddate(myTime.getDate());
        billMapper.updateById(bill);
        detail.setEnddate(myTime.getDate());
        detailMapper.updateById(detail);
        chargingField.endRecharge(chargingPileId,chargingType);
        //结束充电函数
        return "success";
    }
    @GetMapping("/payBill")
    public String getBill(HttpSession session,Model model){
        RequestInfo requestInfo=(RequestInfo) session.getAttribute("requestInfo");
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("userid",requestInfo.getId());
        Detail detail=detailMapper.selectOne(queryWrapper);
        model.addAttribute("detail",detail);
        return "Bill";
    }
    @PostMapping("/payBill")
    public  String payBill(HttpSession session)
    {
        RequestInfo requestInfo=(RequestInfo) session.getAttribute("requestInfo");
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("userid",requestInfo.getId());
        Detail detail=detailMapper.selectOne(queryWrapper);
        detail.setIspay(true);
        detailMapper.updateById(detail);
        return "success";
    }
    @PostMapping("/changeChargingNum")
    @ResponseBody
    public String changChargingNum(HttpSession session,@RequestParam float newChargingNum){
        RequestInfo requestInfo=(RequestInfo)session.getAttribute("requestInfo");
        Car car=chargingStation.getWaitingQueue().changeChargingNum(requestInfo.getId(),newChargingNum,requestInfo.getChargingMode());
        if(car==null)
        {
            return "failed";
        }
        else return "success";
    }
    @GetMapping("/changeRequest")
    public String changChargingNum(){
        return "changeRequest";
    }

    @PostMapping("/changeRequest")
    @ResponseBody
    public String changChargingNum(HttpSession session,@RequestParam float newChargingNum,@RequestParam String newMode){
        RequestInfo requestInfo=(RequestInfo)session.getAttribute("requestInfo");
        Car car=chargingStation.getWaitingQueue().changeChargingNum(requestInfo.getId(),newChargingNum,requestInfo.getChargingMode());
        if(car==null)
        {
            return "failed";
        }

        if(!Objects.equals(newMode, requestInfo.getChargingMode()))
        {
            car=chargingStation.changeChargeMode(requestInfo.getId(),requestInfo.getChargingMode());
        }
        requestInfo.setChargingNum(newChargingNum);
        requestInfo.setChargingMode(newMode);
        session.removeAttribute("requestInfo");
        session.setAttribute("requestInfo",requestInfo);
        return "success";
    }
    @PostMapping("/changeRequestMode")
    @ResponseBody
    public  String changeRequestMode(HttpSession session,@RequestParam String newMode)
    {
        RequestInfo requestInfo=(RequestInfo)session.getAttribute("requestInfo");
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
    public  String cancelRecharge(HttpSession session)
    {
        RequestInfo requestInfo=(RequestInfo) session.getAttribute("requestInfo");
        //需要判断是从等候区还是从充电区取消，以及取消订单和详单
        Car car=chargingStation.changeChargeMode(requestInfo.getId(),requestInfo.getChargingMode());

        if(car==null)
        {
            if(chargingField.cancelRequest(session,requestInfo,detailMapper,billMapper)==false)
            {
                requestInfo.setCarState("endcharging");
                QueryWrapper queryWrapper=new QueryWrapper();
                queryWrapper.eq("userid",car.getId());
                Bill bill=billMapper.selectOne(queryWrapper);
                bill.setEnddate(myTime.getDate());
                billMapper.updateById(bill);
                Detail detail=detailMapper.selectOne(queryWrapper);
                detail.setEnddate(myTime.getDate());
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
    public List<Car> requestQueue(HttpSession session)
    {
        RequestInfo requestInfo=(RequestInfo) session.getAttribute("requestInfo");
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
        return "Bill";
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
