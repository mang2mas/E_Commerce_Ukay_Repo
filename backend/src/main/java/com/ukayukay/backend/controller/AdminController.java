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

    // Route to verify business permits (APPROVE / REJECT)
    // URL Example: PUT http://localhost:8080/api/admin/sellers/5/verify?status=APPROVED&adminId=1
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

    // Route to suspend or reactivate an account (true = active, false = suspended)
    // URL Example: PUT http://localhost:8080/api/admin/users/3/status?isActive=false&adminId=1
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

// Route to approve or reject a thrift product listing
// URL Example: PUT http://localhost:8080/api/admin/products/10/moderate?status=AVAILABLE&adminId=1
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