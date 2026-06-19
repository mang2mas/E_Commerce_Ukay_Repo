package com.ukayukay.backend.controller;

import com.ukayukay.backend.dto.PromoCodeDTO;
import com.ukayukay.backend.service.PromoCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promo-codes")
@Tag(name = "Promo Codes", description = "Promo code management and validation - Create, validate, and apply discount codes during checkout")
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    public PromoCodeController(PromoCodeService promoCodeService) {
        this.promoCodeService = promoCodeService;
    }

    /**
     * Validate promo code and calculate discount for a cart total
     */
    @PostMapping("/validate")
    @Operation(summary = "Validate promo code", description = "Validate a promo code for a given cart total and calculate the discount amount")
    @ApiResponse(responseCode = "200", description = "Promo code is valid and discount calculated")
    @ApiResponse(responseCode = "400", description = "Invalid promo code or minimum purchase not met")
    public ResponseEntity<?> validatePromoCode(
            @RequestParam String code,
            @RequestParam BigDecimal cartTotal) {
        
        BigDecimal discount = promoCodeService.validateAndCalculateDiscount(code, cartTotal);
        
        if (discount == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Invalid or expired promo code, or minimum purchase not met")
            );
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("cartTotal", cartTotal);
        response.put("discount", discount);
        response.put("finalTotal", cartTotal.subtract(discount));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Apply promo code (used after checkout confirmation)
     */
    @PostMapping("/apply")
    @Operation(summary = "Apply promo code", description = "Apply a promo code after checkout and increment its usage count")
    @ApiResponse(responseCode = "200", description = "Promo code applied successfully")
    @ApiResponse(responseCode = "400", description = "Failed to apply promo code")
    public ResponseEntity<?> applyPromoCode(@RequestParam String code) {
        try {
            promoCodeService.applyPromoCode(code);
            return ResponseEntity.ok(Map.of("message", "Promo code applied successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to apply promo code"));
        }
    }

    /**
     * Get all active promo codes
     */
    @GetMapping("/active")
    @Operation(summary = "Get active promo codes", description = "Retrieve all currently active and valid promo codes")
    @ApiResponse(responseCode = "200", description = "List of active promo codes")
    public ResponseEntity<List<PromoCodeDTO>> getAllActivePromoCodes() {
        List<PromoCodeDTO> codes = promoCodeService.getAllActivePromoCodes();
        return ResponseEntity.ok(codes);
    }

    /**
     * Get promo code by id
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get promo code by ID", description = "Retrieve details of a specific promo code")
    @ApiResponse(responseCode = "200", description = "Promo code details retrieved")
    @ApiResponse(responseCode = "404", description = "Promo code not found")
    public ResponseEntity<PromoCodeDTO> getPromoCodeById(@PathVariable Integer id) {
        PromoCodeDTO code = promoCodeService.getPromoCodeById(id);
        if (code == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(code);
    }

    /**
     * Create new promo code (admin only)
     */
    @PostMapping
    @Operation(summary = "Create new promo code", description = "Create a new promo code (Admin only)")
    @ApiResponse(responseCode = "200", description = "Promo code created successfully")
    public ResponseEntity<PromoCodeDTO> createPromoCode(@RequestBody PromoCodeDTO dto) {
        PromoCodeDTO created = promoCodeService.createPromoCode(dto);
        return ResponseEntity.ok(created);
    }

    /**
     * Deactivate promo code (admin only)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate promo code", description = "Deactivate a promo code (Admin only)")
    @ApiResponse(responseCode = "200", description = "Promo code deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Promo code not found")
    public ResponseEntity<PromoCodeDTO> deactivatePromoCode(@PathVariable Integer id) {
        PromoCodeDTO deactivated = promoCodeService.deactivatePromoCode(id);
        if (deactivated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(deactivated);
    }
}
