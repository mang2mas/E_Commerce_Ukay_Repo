package com.ukayukay.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@CrossOrigin(origins = "*")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping("/order/{orderId}")
    public ResponseEntity<?> createShipment(@PathVariable Integer orderId, @RequestBody Shipment shipment) {
        try {
            return ResponseEntity.ok(shipmentService.createShipment(orderId, shipment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{shipmentId}/status")
    public ResponseEntity<?> updateShipmentStatus(@PathVariable Integer shipmentId, @RequestParam String status) {
        try {
            return ResponseEntity.ok(shipmentService.updateShipmentStatus(shipmentId, status));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{shipmentId}/tracking")
    public ResponseEntity<?> updateTrackingInfo(@PathVariable Integer shipmentId, @RequestBody Shipment shipment) {
        try {
            return ResponseEntity.ok(shipmentService.updateTrackingInfo(shipmentId, shipment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Shipment>> getShipmentsByOrder(@PathVariable Integer orderId) {
        return ResponseEntity.ok(shipmentService.getShipmentsByOrder(orderId));
    }
}
