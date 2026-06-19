package com.ukayukay.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ukayukay.backend.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByBuyer_UserIdOrderByCreatedAtDesc(Integer buyerId);
    List<Review> findByProduct_ProductIdOrderByCreatedAtDesc(Integer productId);
}
