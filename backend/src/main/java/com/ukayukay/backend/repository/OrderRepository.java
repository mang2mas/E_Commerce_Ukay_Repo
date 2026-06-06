package com.ukayukay.backend.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ukayukay.backend.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    
}
