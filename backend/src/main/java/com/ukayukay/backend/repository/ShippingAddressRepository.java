package com.ukayukay.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ukayukay.backend.entity.ShippingAddress;

public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Integer> {
    List<ShippingAddress> findByUser_UserId(Integer userId);
}
