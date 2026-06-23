package com.ukayukay.backend.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {

    private Integer reviewId;
    private Integer productId;
    private String productName;
    private Integer buyerId;
    private String buyerName;
    private Integer rating;
    private String comment;
    private Boolean isVerifiedPurchase;
    private Integer helpfulCount;
    private String status;
    private String itemReviewed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
