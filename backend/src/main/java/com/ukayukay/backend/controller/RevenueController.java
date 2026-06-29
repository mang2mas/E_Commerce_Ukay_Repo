package com.ukayukay.backend.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ukayukay.backend.service.RevenueService;

@RestController
@RequestMapping("/api/revenue")
public class RevenueController {

    private final RevenueService revenueService;

    public RevenueController(RevenueService revenueService) {
        this.revenueService = revenueService;
    }

    /** GET /api/revenue/seller/{sellerId}/stats */
    @GetMapping("/seller/{sellerId}/stats")
    public ResponseEntity<Map<String, Object>> getStats(@PathVariable Integer sellerId) {
        return ResponseEntity.ok(revenueService.getRevenueStats(sellerId));
    }

    /** GET /api/revenue/seller/{sellerId}/monthly */
    @GetMapping("/seller/{sellerId}/monthly")
    public ResponseEntity<Map<String, BigDecimal>> getMonthly(@PathVariable Integer sellerId) {
        return ResponseEntity.ok(revenueService.getMonthlySales(sellerId));
    }

    /** GET /api/revenue/seller/{sellerId}/categories */
    @GetMapping("/seller/{sellerId}/categories")
    public ResponseEntity<List<Map<String, Object>>> getCategories(@PathVariable Integer sellerId) {
        return ResponseEntity.ok(revenueService.getCategoryBreakdown(sellerId));
    }
}
