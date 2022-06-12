package com.example.softwareproject.controller;

import com.example.softwareproject.entity.Customer;
import com.example.softwareproject.entity.RequestInfo;
import com.example.softwareproject.mapper.CustomerMapper;
import com.example.softwareproject.service.ChargingStation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/customer")
public class CustomerController {
    @Resource
    CustomerMapper customerMapper;
    @Autowired
    ChargingStation chargingStation;

    @GetMapping("/register")
    public String register(){
        return "register";
    }
    @PostMapping("/register")
    @ResponseBody
    public  String register(@RequestParam String username, @RequestParam String password)
    {
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
            return "";
        }
        return "logInFailed";
    }
    @PostMapping("/requestRecharge")
    @ResponseBody
    //使用ResponseBody，且返回String先去resource里面找是否存在视图，不存在的话封装为json数据回传给前端
    //可以用于ajax的success函数
    public  String requestRecharge(@ModelAttribute RequestInfo requestInfo)
    {
        chargingStation.requestRecharge(requestInfo);
    }
    @PostMapping("/enterChargeField")
    @ResponseBody
    public  String enterChargeField()
    {

    }
    @PostMapping("/startRecharge")
    @ResponseBody
    public  String startRecharge()
    {

    }
    @PostMapping("/endRecharge")
    @ResponseBody
    public  String endRecharge()
    {

    }
    @PostMapping("/payBill")
    @ResponseBody
    public  String payBill()
    {

    }
    @PostMapping("/changeRequestMode")
    @ResponseBody
    public  String changeRequestMode()
    {

    }
    @PostMapping("/cancelRecharge")
    @ResponseBody
    public  String cancelRecharge()
    {

    }
    @PostMapping("/requestQueue")
    @ResponseBody
    public  String requestQueue()
    {

    }
    @PostMapping("/requestBill")
    @ResponseBody
    public  String requestBill()
    {

    }
}
