package com.ukayukay.backend.controller;

import java.util.List;

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

import com.ukayukay.backend.entity.Payment;
import com.ukayukay.backend.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/order/{orderId}")
    public ResponseEntity<?> processPayment(@PathVariable Integer orderId, @RequestBody Payment payment) {
        try {
            return ResponseEntity.ok(paymentService.processPayment(orderId, payment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{paymentId}/status")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable Integer paymentId, @RequestParam String status) {
        try {
            return ResponseEntity.ok(paymentService.updatePaymentStatus(paymentId, status));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Payment>> getPaymentsByOrder(@PathVariable Integer orderId) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrder(orderId));
    }
}
