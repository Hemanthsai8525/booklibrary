package com.example.book.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.book.dto.OrderRequest;
import com.example.book.model.Order;
import com.example.book.service.OrderService;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService svc;

    public OrderController(OrderService svc) {
        this.svc = svc;
    }
    
    @GetMapping
    public List<Order> all(){
    	return svc.findAll();
    }
    @DeleteMapping("/{orderId}")
    public String deleteOrder(@PathVariable Long orderId) {
    	svc.deleteOrder(orderId);
        return "Order" + orderId+" deleted successfully";
    }

    @PostMapping("/place")
    public Order placeOrder(@RequestBody OrderRequest req) {
        return svc.placeOrder(
                req.getUserId(),
                req.getAddress(),
                req.getPhone()
        );
    }

    
    @GetMapping("/user/{userId}")
    public List<Order> userOrders(@PathVariable Long userId) {
        return svc.findByUserId(userId);
    }
    
    @GetMapping("/{orderId}")
    public Order getOrder(@PathVariable Long orderId) {
        return svc.getOrder(orderId);
    }

    
    
}
