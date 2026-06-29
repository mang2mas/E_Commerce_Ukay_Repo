package com.ukayukay.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ukayukay.backend.entity.Product;
import com.ukayukay.backend.service.InventoryService;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /** GET /api/inventory/seller/{sellerId}/stats */
    @GetMapping("/seller/{sellerId}/stats")
    public ResponseEntity<Map<String, Object>> getStats(@PathVariable Integer sellerId) {
        return ResponseEntity.ok(inventoryService.getInventoryStats(sellerId));
    }

    /** GET /api/inventory/seller/{sellerId}/products */
    @GetMapping("/seller/{sellerId}/products")
    public ResponseEntity<List<Product>> getSellerProducts(@PathVariable Integer sellerId) {
        return ResponseEntity.ok(inventoryService.getSellerProducts(sellerId));
    }

    /** PATCH /api/inventory/{productId}/status?status=ARCHIVED&sellerId=1 */
    @PatchMapping("/{productId}/status")
    public ResponseEntity<Product> updateStatus(
            @PathVariable Integer productId,
            @RequestParam String status,
            @RequestParam Integer sellerId) {
        return ResponseEntity.ok(inventoryService.updateProductStatus(productId, status, sellerId));
    }
}
