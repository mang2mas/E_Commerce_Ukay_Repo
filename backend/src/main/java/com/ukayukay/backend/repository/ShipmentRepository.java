package com.ukayukay.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ukayukay.backend.entity.Shipment;

public interface ShipmentRepository extends JpaRepository<Shipment, Integer> {
    List<Shipment> findByOrder_OrderId(Integer orderId);
}
