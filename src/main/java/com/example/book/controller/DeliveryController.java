package com.example.book.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.book.model.Order;
import com.example.book.service.OrderService;


@RestController
@RequestMapping("/delivery")
public class DeliveryController {

    private final OrderService orderService;

    public DeliveryController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PutMapping("/{orderId}/status")
    public Order updateDeliveryStatus(@PathVariable Long orderId, @RequestBody String status) {
        return orderService.updateStatus(orderId, status);
    }
}

