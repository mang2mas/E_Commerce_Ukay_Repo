package com.ukayukay.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ukayukay.backend.entity.Product;
import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.service.ProductService;
import com.ukayukay.backend.service.UserService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserService userService;

    @PutMapping("/sellers/{sellerId}/verify")
    public ResponseEntity<?> verifySellerPermit(
            @PathVariable Integer sellerId,
            @RequestParam User.VerificationStatus status,
            @RequestParam Integer adminId) {
        try {
            User updatedSeller = userService.verifySeller(sellerId, status, adminId);
            return ResponseEntity.ok(updatedSeller);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<?> changeAccountStatus(
            @PathVariable Integer userId,
            @RequestParam boolean isActive,
            @RequestParam Integer adminId) {
        try {
            User updatedUser = userService.toggleAccountStatus(userId, isActive, adminId);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Autowired
private ProductService productService;

@PutMapping("/products/{productId}/moderate")
public ResponseEntity<?> moderateProductListing(
        @PathVariable Integer productId,
        @RequestParam String status,
        @RequestParam Integer adminId) {
    try {
        Product moderatedProduct = productService.moderateProduct(productId, status, adminId);
        return ResponseEntity.ok(moderatedProduct);
    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}

}