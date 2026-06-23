package com.ukayukay.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ukayukay.backend.entity.Order;
import com.ukayukay.backend.entity.OrderItem;
import com.ukayukay.backend.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout/{userId}")
    public ResponseEntity<Order> placeOrder(@PathVariable Integer userId, @RequestBody Map<String, String> request) {
        String address = request.get("shippingAddress");
        String payment = request.get("paymentMethod");
        return ResponseEntity.ok(orderService.checkout(userId, address, payment));
    }

    @GetMapping("/customer/{userId}")
    public List<Order> getCustomerHistory(@PathVariable Integer userId) {
        return orderService.getCustomerOrders(userId);
    }

    @GetMapping("/{orderId}/items")
    public ResponseEntity<List<OrderItem>> getOrderItems(@PathVariable Integer orderId) {
        return ResponseEntity.ok(orderService.getOrderItems(orderId));
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
