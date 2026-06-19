package com.ukayukay.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ukayukay.backend.entity.Order;
import com.ukayukay.backend.entity.Payment;
import com.ukayukay.backend.repository.OrderRepository;
import com.ukayukay.backend.repository.PaymentRepository;
import com.ukayukay.backend.util.AppConstants;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    public Payment processPayment(Integer orderId, Payment payment) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException(AppConstants.ORDER_NOT_FOUND));

        if (payment.getPaymentMethod() == null || payment.getPaymentMethod().trim().isEmpty()) {
            throw new RuntimeException(AppConstants.PAYMENT_METHOD_REQUIRED);
        }

        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(payment.getStatus() == null ? "PENDING" : payment.getStatus().toUpperCase());
        payment.setTransactionId(payment.getTransactionId());
        payment.setReferenceNumber(payment.getReferenceNumber());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    public Payment updatePaymentStatus(Integer paymentId, String status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));

        payment.setStatus(status.toUpperCase());
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    public List<Payment> getPaymentsByOrder(Integer orderId) {
        return paymentRepository.findByOrder_OrderId(orderId);
    }
}
