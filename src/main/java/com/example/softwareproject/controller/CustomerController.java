package com.example.softwareproject.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

    @GetMapping("/logOut")
    public String logOut(HttpSession session)
    {
        session.removeAttribute("login_state");
        session.removeAttribute("username");
        session.removeAttribute("userid");
        return "log_in";
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
        session.removeAttribute("requestInfo");
        requestInfo.setId((Long) session.getAttribute("userid"));
        Detail detail =new Detail();
        detail.setUserid(requestInfo.getId());
        detail.setStartrequesttime(myTime.getDate());
        detail.setChargingtype(requestInfo.chargingMode);
        detailMapper.insert(detail);
        System.out.println(detail);
        requestInfo.setBillid(detail.getBillid());
        model.addAttribute(requestInfo);
        session.setAttribute("requestInfo",requestInfo);
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
    public RequestInfo checkCarState(HttpSession session)
    {
//        System.out.println("test"+session.getAttribute("requestInfo"));
        RequestInfo requestInfo=(RequestInfo) session.getAttribute("requestInfo");
        if(Objects.equals(requestInfo.getCarState(), "chargingDone"))//充电完成
        {
            return requestInfo;
        }
//        System.out.println(requestInfo);
        Car car = chargingStation.getWaitingQueue().getCarByInfo(requestInfo);
        if(car!=null)//还在等候区
        {

            if(requestInfo.getChargingMode().equals("fast"))
                requestInfo.setQueue_num("快充队列"+chargingStation.getWaitingQueue().getFastWaitingQueue().size());
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
        queryWrapper.eq("billid",requestInfo.getBillid());
        Detail detail = detailMapper.selectOne(queryWrapper);
        System.out.println(detail);
        int chargingPileId=(int)chargingField.getPileIdByInfo(requestInfo);
        detail.setChargingpileid(chargingPileId);
        if(requestInfo.getChargingMode().equals("fast")) {
            FastChargingPile fastChargingPile = chargingField.getFastChargingPileById(chargingPileId);
            car=fastChargingPile.getFirstCar();
            fastChargingPile.startCharging(myTime,session,requestInfo,detailMapper,billMapper);
        }
        else
        {
            SlowChargingPile slowChargingPile=chargingField.getSlowChargingPileById(chargingPileId);
            car=slowChargingPile.getFirstCar();
            slowChargingPile.startCharging(myTime,session,requestInfo,detailMapper,billMapper);
        }
        car.setCarState("charging");
        Bill bill=new Bill();
        bill.setStartdate(myTime.getDate());
        bill.setUserid(car.getId());
        bill.setChargingpileid(chargingPileId);
        bill.setChargingnum(detail.getChargevol());
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
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("billid",requestInfo.getBillid());
        Detail detail = detailMapper.selectOne(queryWrapper);
        int chargingPileId=(int)detail.getChargingpileid();
        String chargingType=requestInfo.getChargingMode();
        Bill bill=billMapper.selectOne(queryWrapper);
        bill.setEnddate(myTime.getDate());
        billMapper.updateById(bill);
        detail.setEnddate(myTime.getDate());
        detailMapper.updateById(detail);
        if(requestInfo.getCarState()=="chargingDone")
        {
            Date now=myTime.getDate();

            double timeout=(now.getTime()-detail.getEnddate().getTime())/1000/60;
            double timeoutFee=timeout*1;//设置超时费
            detail.setTimeoutfee((float) timeoutFee);
            detail.setTotalfee((float) (detail.getTotalfee()+timeoutFee));
            detailMapper.updateById(detail);
            bill.setTotalfee((float) (detail.getTotalfee()+timeoutFee));
            billMapper.updateById(bill);
            return "success";
        }
        if(chargingType.equals("fast")) {
            FastChargingPile fastChargingPile = chargingField.getFastChargingPileById(chargingPileId);
            car=fastChargingPile.getFirstCar();
            requestInfo.setCarState("chargingDoneByUser");
            fastChargingPile.endCharging(myTime,session,requestInfo,detailMapper,billMapper);
        }
        else
        {
            SlowChargingPile slowChargingPile=chargingField.getSlowChargingPileById(chargingPileId);
            car=slowChargingPile.getFirstCar();
            requestInfo.setCarState("chargingDoneByUser");
            slowChargingPile.endCharging(myTime,session,requestInfo,detailMapper,billMapper);
        }

        car.setCarState("endcharging");



        System.out.println("success");
        //结束充电函数
        return "success";
    }
    @GetMapping("/payBill")
    public String getBill(HttpSession session,Model model){
        RequestInfo requestInfo=(RequestInfo) session.getAttribute("requestInfo");
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("billid",requestInfo.getBillid());
        Detail detail=detailMapper.selectOne(queryWrapper);
        detail.setTimeout(myTime.getDate().getTime()-detail.getEnddate().getTime());
        detailMapper.updateById(detail);
        model.addAttribute("detail",detail);
        return "Bill";
    }
    @PostMapping("/payBill")
    @ResponseBody
    public String payBill(HttpSession session)
    {
        RequestInfo requestInfo=(RequestInfo) session.getAttribute("requestInfo");
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("billid",requestInfo.getBillid());
        Detail detail=detailMapper.selectOne(queryWrapper);
        detail.setIspay(true);
        detailMapper.updateById(detail);
        session.removeAttribute("requestInfo");
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
        System.out.println(newChargingNum);
        System.out.println(newMode);
        Car car=chargingStation.getWaitingQueue().changeChargingNum(requestInfo.getId(),newChargingNum,requestInfo.getChargingMode());
        if(car==null)
        {
            return "failed";
        }

        if(!Objects.equals(newMode, requestInfo.getChargingMode()))
        {
            car=chargingStation.removeCarByIdAndMode(requestInfo.getId(),requestInfo.getChargingMode());
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
        Car car=chargingStation.removeCarByIdAndMode(requestInfo.getId(),requestInfo.getChargingMode());
        //不在则说明在充电区
        if(car.equals(null))
        {
            return "changeRequestModeFailed";
        }
        requestInfo.setChargingMode(newMode);
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("billid",requestInfo.getBillid());
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
        Car car=chargingStation.removeCarByIdAndMode(requestInfo.getId(),requestInfo.getChargingMode());

        if(car==null)//在充电区，不能是充电状态
        {
            if(chargingField.cancelRequest(session,requestInfo,detailMapper,billMapper)==null)
            {
                return "cancelFailed";
            }
        }
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("billid",requestInfo.getBillid());
        billMapper.delete(queryWrapper);
        detailMapper.delete(queryWrapper);

        return "success";
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
    @GetMapping("/requestBillList")
    public String requestBillList()
    {
        return "userHistoryBill";
    }
    @PostMapping("/requestBillList")
    @ResponseBody
    public  List<Bill> requestBillList(@ModelAttribute RequestInfo requestInfo)
    {
        //需要配合前端界面
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("userid",requestInfo.getId());
        List<Bill> bills= billMapper.selectList(queryWrapper);
        return bills;
    }
}
