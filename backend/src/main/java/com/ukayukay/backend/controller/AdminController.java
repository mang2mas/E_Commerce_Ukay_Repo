package com.ukayukay.backend.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ukayukay.backend.entity.Product;
import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.security.JwtUtil;
import com.ukayukay.backend.service.ProductService;
import com.ukayukay.backend.service.UserService;

import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;


@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {


    private final UserService userService;
    private final ProductService productService;
    private final JwtUtil jwtUtil;


    public AdminController(UserService userService, ProductService productService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.productService = productService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> listUsers(
            @RequestParam(required = false) String role) {
        User.UserRole roleFilter = null;
        if (role != null && !role.isBlank()) {
            try {
                roleFilter = User.UserRole.valueOf(role.trim().toLowerCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid role. Use buyer, seller, or omit role.");
            }
        }
        return ResponseEntity.ok(userService.getManageableUsers(roleFilter));
    }


    @PutMapping("/sellers/{sellerId}/verify")
    public ResponseEntity<User> verifySellerPermit(
            @PathVariable Integer sellerId,
            @RequestParam User.VerificationStatus status,
            @RequestHeader("Authorization") String authHeader) {
        Integer adminId = extractAdminId(authHeader);
        return ResponseEntity.ok(userService.verifySeller(sellerId, status, adminId));
    }


    @PutMapping("/users/{userId}/status")
    public ResponseEntity<User> changeAccountStatus(
            @PathVariable Integer userId,
            @RequestParam boolean isActive,
            @RequestHeader("Authorization") String authHeader) {
        Integer adminId = extractAdminId(authHeader);
        return ResponseEntity.ok(userService.toggleAccountStatus(userId, isActive, adminId));
    }


    @PutMapping("/users/{userId}/shadow-ban")
    public ResponseEntity<User> shadowBanUser(
            @PathVariable Integer userId,
            @RequestParam boolean shadowBanned,
            @RequestHeader("Authorization") String authHeader) {
        Integer adminId = extractAdminId(authHeader);
        return ResponseEntity.ok(userService.toggleShadowBan(userId, shadowBanned, adminId));
    }


    @PutMapping("/users/{userId}/discount-status")
    public ResponseEntity<User> decideDiscountApplication(
            @PathVariable Integer userId,
            @RequestParam User.DiscountStatus status,
            @RequestHeader("Authorization") String authHeader) {
        Integer adminId = extractAdminId(authHeader);
        return ResponseEntity.ok(userService.decideDiscountApplication(userId, status, adminId));
    }


    @PutMapping("/products/{productId}/moderate")
    public ResponseEntity<Product> moderateProductListing(
            @PathVariable Integer productId,
            @RequestParam String status,
            @RequestHeader("Authorization") String authHeader) {
        Integer adminId = extractAdminId(authHeader);
        return ResponseEntity.ok(productService.moderateProduct(productId, status, adminId));
    }


    private Integer extractAdminId(String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        return jwtUtil.extractUserId(token);
    }
}



