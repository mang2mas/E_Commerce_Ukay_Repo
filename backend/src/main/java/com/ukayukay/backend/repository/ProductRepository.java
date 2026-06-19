package com.ukayukay.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ukayukay.backend.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByStatus(String status);
    
    List<Product> findByCategoryAndStatus(String category, String status);

    List<Product> findBySeller_UserId(Integer sellerId);
}