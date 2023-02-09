package com.ss.springbootjunitexample.service;

import org.springframework.stereotype.Service;

@Service
public class GreetingService {
    public String greet() {
        return "{\"message\":\"Greeting of the day..!\"}";
    }
}