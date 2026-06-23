package com.ukayukay.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ukayukay.backend.entity.Review;
import com.ukayukay.backend.service.ReviewService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<Review> submitReview(@RequestBody ReviewPayload payload) {
        return ResponseEntity.ok(reviewService.createReview(payload.getBuyerId(), payload.getProductId(), payload.getRating(), payload.getComment()));
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<Review>> getReviewsByBuyer(@PathVariable Integer buyerId) {
        return ResponseEntity.ok(reviewService.getReviewsByBuyer(buyerId));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getReviewsByProduct(@PathVariable Integer productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }

    public static class ReviewPayload {
        private Integer buyerId;
        private Integer productId;
        private Integer rating;
        private String comment;

        public Integer getBuyerId() { return buyerId; }
        public void setBuyerId(Integer buyerId) { this.buyerId = buyerId; }
        public Integer getProductId() { return productId; }
        public void setProductId(Integer productId) { this.productId = productId; }
        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }
}

