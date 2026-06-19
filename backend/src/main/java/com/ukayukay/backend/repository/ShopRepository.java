package com.ukayukay.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ukayukay.backend.entity.Shop;

public interface ShopRepository extends JpaRepository<Shop, Integer> {
    Optional<Shop> findBySeller_UserId(Integer sellerId);
}
