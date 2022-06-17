package com.example.softwareproject.config;

import com.example.softwareproject.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LoginConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration registration = registry.addInterceptor(new LoginInterceptor());
        registration.addPathPatterns("/**");
        registration.excludePathPatterns("/customer/*");
        registration.excludePathPatterns("/administrator/*");
//        registration.excludePathPatterns("/*.jpg");
//        registration.excludePathPatterns("/*.png");
        registration.excludePathPatterns("/**/*.jpeg");
        registration.excludePathPatterns("/**/*.css");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/customer/logIn");
    }
}
