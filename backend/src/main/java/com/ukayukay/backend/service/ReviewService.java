package com.ukayukay.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ukayukay.backend.entity.Product;
import com.ukayukay.backend.entity.Review;
import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.OrderItemRepository;
import com.ukayukay.backend.repository.ProductRepository;
import com.ukayukay.backend.repository.ReviewRepository;
import com.ukayukay.backend.repository.UserRepository;
import com.ukayukay.backend.util.AppConstants;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository,
                         ProductRepository productRepository, OrderItemRepository orderItemRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public Review createReview(Integer buyerId, Integer productId, Integer rating, String comment) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new RuntimeException("Rating must be between 1 and 5.");
        }

        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException(String.format(AppConstants.USER_NOT_FOUND, buyerId)));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        Review review = new Review();
        review.setBuyer(buyer);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(comment);
        review.setIsVerifiedPurchase(orderItemRepository.existsByOrder_Customer_UserIdAndProduct_ProductId(buyerId, productId));
        review.setStatus("PENDING");

        return reviewRepository.save(review);
    }

    public List<Review> getReviewsByBuyer(Integer buyerId) {
        return reviewRepository.findByBuyer_UserIdOrderByCreatedAtDesc(buyerId);
    }

    public List<Review> getReviewsByProduct(Integer productId) {
        return reviewRepository.findByProduct_ProductIdOrderByCreatedAtDesc(productId);
    }
}