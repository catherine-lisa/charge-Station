package com.example.softwareproject.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String login_state = String.valueOf(request.getSession().getAttribute("login_state"));
        System.out.println(login_state);
        if (login_state.equals("null")) {
            System.out.println("yes");
            response.sendRedirect("/customer/logIn");
            return false;
        }
        return true;
    }
}