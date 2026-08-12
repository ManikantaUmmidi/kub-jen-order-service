package com.app.kub_jen_order_service;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable Long id) {

        return new Order(
                id,
                "Laptop",
                75000
        );
    }

    @GetMapping("/health")
    public String health() {

        return "Order Service is running successfully";
    }

    @DeleteMapping("/{id}")
    public String cancelOrder(@PathVariable Long id) {

        return "Order " + id + " cancelled successfully";
    }
}