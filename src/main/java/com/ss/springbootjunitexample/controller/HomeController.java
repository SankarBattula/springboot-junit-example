package com.ss.springbootjunitexample.controller;

import com.ss.springbootjunitexample.service.GreetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @Autowired
    GreetingService service;

    @RequestMapping("/")
    public @ResponseBody
    String greet() {
        return "{\"message\" : \"Hello, World\"}";
    }

    @RequestMapping("/greeting")
    public @ResponseBody String greeting() {
        return service.greet();
    }
}
