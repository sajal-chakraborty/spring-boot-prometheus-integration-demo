package com.example.api.controllers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class OrderController {

    private final Counter orderCounter;

    // Constructor Injection
    public OrderController(MeterRegistry registry) {
        this.orderCounter = Counter.builder("orders_total")
                .tag("region", "india")
                .tag("service", "payment")
                .register(registry);
    }

    @GetMapping("/test")
    public String sayHello() {

        orderCounter.increment();  // ✅ reuse counter

        return "Hello from sayHello : " + new Date();
    }
}