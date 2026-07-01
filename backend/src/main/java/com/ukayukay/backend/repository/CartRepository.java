package com.ukayukay.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ukayukay.backend.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    List<Cart> findByUser_UserId(Integer userId);

    Optional<Cart> findByUser_UserIdAndProduct_ProductId(Integer userId, Integer productId);
    
    boolean existsByUser_UserIdAndProduct_ProductId(Integer userId, Integer productId);
}