package com.ukayukay.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ukayukay.backend.entity.Shipment;
import com.ukayukay.backend.service.ShipmentService;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping("/order/{orderId}")
    public ResponseEntity<Shipment> createShipment(@PathVariable Integer orderId, @RequestBody Shipment shipment) {
        return ResponseEntity.ok(shipmentService.createShipment(orderId, shipment));
    }

    @PutMapping("/{shipmentId}/status")
    public ResponseEntity<Shipment> updateShipmentStatus(@PathVariable Integer shipmentId, @RequestParam String status) {
        return ResponseEntity.ok(shipmentService.updateShipmentStatus(shipmentId, status));
    }

    @PutMapping("/{shipmentId}/tracking")
    public ResponseEntity<Shipment> updateTrackingInfo(@PathVariable Integer shipmentId, @RequestBody Shipment shipment) {
        return ResponseEntity.ok(shipmentService.updateTrackingInfo(shipmentId, shipment));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Shipment>> getShipmentsByOrder(@PathVariable Integer orderId) {
        return ResponseEntity.ok(shipmentService.getShipmentsByOrder(orderId));
    }
}

