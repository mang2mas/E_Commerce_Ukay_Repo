package com.ukayukay.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ukayukay.backend.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByOrder_OrderId(Integer orderId);
}
