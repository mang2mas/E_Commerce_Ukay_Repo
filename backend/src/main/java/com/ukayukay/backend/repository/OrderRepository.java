package com.ukayukay.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ukayukay.backend.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByCustomer_UserIdOrderByCreatedAtDesc(Integer userId);
}