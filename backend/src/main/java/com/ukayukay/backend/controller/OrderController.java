package com.ukayukay.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ukayukay.backend.entity.Order;
import com.ukayukay.backend.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired private OrderService orderService;

    @PostMapping("/checkout/{userId}")
    public ResponseEntity<?> placeOrder(@PathVariable Integer userId, @RequestBody Map<String, String> request) {
        try {
            String address = request.get("shippingAddress");
            String payment = request.get("paymentMethod");
            Order completedOrder = orderService.checkout(userId, address, payment);
            return ResponseEntity.ok(completedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/customer/{userId}")
    public List<Order> getCustomerHistory(@PathVariable Integer userId) {
        return orderService.getCustomerOrders(userId);
    }

    @GetMapping
    public List<Order> getAllOrdersForAdmin() {
        return orderService.getAllOrders();
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> changeStatus(@PathVariable Integer orderId, @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }
}