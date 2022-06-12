package com.example.softwareproject.controller;

import com.example.softwareproject.entity.Customer;
import com.example.softwareproject.mapper.CustomerMapper;
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
    public  String requestRecharge()
    {

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
