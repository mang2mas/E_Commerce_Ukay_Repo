package com.ukayukay.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ukayukay.backend.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    // Easily find receipt detail rows for a specific order card
    List<OrderItem> findByOrder_OrderId(Integer orderId);
}
