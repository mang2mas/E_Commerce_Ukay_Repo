package com.ukayukay.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ukayukay.backend.entity.Payment;
import com.ukayukay.backend.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/order/{orderId}")
    public ResponseEntity<Payment> processPayment(@PathVariable Integer orderId, @RequestBody Payment payment) {
        return ResponseEntity.ok(paymentService.processPayment(orderId, payment));
    }

    @PutMapping("/{paymentId}/status")
    public ResponseEntity<Payment> updatePaymentStatus(@PathVariable Integer paymentId, @RequestParam String status) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(paymentId, status));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Payment>> getPaymentsByOrder(@PathVariable Integer orderId) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrder(orderId));
    }
}

