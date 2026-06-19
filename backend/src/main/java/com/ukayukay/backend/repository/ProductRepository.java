package com.ukayukay.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import com.ukayukay.backend.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByStatus(String status);
    
    List<Product> findByCategoryAndStatus(String category, String status);

    List<Product> findBySeller_UserId(Integer sellerId);

    Long countBySeller_UserId(Integer sellerId);

    @Query("SELECT p FROM Product p WHERE p.status = :status " +
           "AND (:category IS NULL OR p.category = :category) " +
           "AND (:size IS NULL OR p.size = :size) " +
           "AND (:itemCondition IS NULL OR p.itemCondition = :itemCondition) " +
           "AND (:sellerId IS NULL OR p.seller.userId = :sellerId)")
    Page<Product> searchAvailableProducts(
            @Param("status") String status,
            @Param("category") String category,
            @Param("size") String size,
            @Param("itemCondition") String itemCondition,
            @Param("sellerId") Integer sellerId,
            Pageable pageable);
}