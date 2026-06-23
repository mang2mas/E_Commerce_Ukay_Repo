package com.ukayukay.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ukayukay.backend.dto.ShopDashboardDTO;
import com.ukayukay.backend.entity.Shop;
import com.ukayukay.backend.entity.ShopRating;
import com.ukayukay.backend.service.ShopService;

import lombok.Data;

@RestController
@RequestMapping("/api/shops")
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<Shop> getShopProfile(@PathVariable Integer sellerId) {
        return ResponseEntity.ok(shopService.getShopBySellerId(sellerId));
    }

    @PutMapping("/seller/{sellerId}")
    public ResponseEntity<Shop> createOrUpdateShopProfile(@PathVariable Integer sellerId,
            @RequestBody Shop shop) {
        return ResponseEntity.ok(shopService.createOrUpdateShopProfile(sellerId, shop));
    }

    @GetMapping("/seller/{sellerId}/dashboard")
    public ResponseEntity<ShopDashboardDTO> getShopDashboard(@PathVariable Integer sellerId) {
        return ResponseEntity.ok(shopService.getShopDashboard(sellerId));
    }

    @GetMapping("/seller/{sellerId}/ratings")
    public ResponseEntity<List<ShopRating>> getShopRatings(@PathVariable Integer sellerId) {
        return ResponseEntity.ok(shopService.getShopRatings(sellerId));
    }

    @PostMapping("/seller/{sellerId}/ratings")
    public ResponseEntity<ShopRating> addShopRating(@PathVariable Integer sellerId,
            @RequestBody ShopRatingRequest request) {
        return ResponseEntity.ok(shopService.addShopRating(sellerId, request.getBuyerId(), request.toShopRating()));
    }

    @Data
    public static class ShopRatingRequest {
        private Integer buyerId;
        private Integer rating;
        private String comment;

        public ShopRating toShopRating() {
            ShopRating rating = new ShopRating();
            rating.setRating(this.rating);
            rating.setComment(this.comment);
            return rating;
        }
    }
}
