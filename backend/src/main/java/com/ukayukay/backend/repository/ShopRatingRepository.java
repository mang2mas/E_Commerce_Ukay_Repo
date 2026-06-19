package com.ukayukay.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ukayukay.backend.entity.ShopRating;

public interface ShopRatingRepository extends JpaRepository<ShopRating, Integer> {
    List<ShopRating> findBySeller_UserId(Integer sellerId);
    List<ShopRating> findByBuyer_UserId(Integer buyerId);
}
