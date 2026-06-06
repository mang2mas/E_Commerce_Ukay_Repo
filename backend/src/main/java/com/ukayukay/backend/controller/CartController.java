package com.ukayukay.backend.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ukayukay.backend.entity.Cart;
import com.ukayukay.backend.service.CartService;

@RestController 
@RequestMapping("/api/cart") 
public class CartController {
    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(
            @RequestParam Integer userId, 
            @RequestParam Integer productId, 
            @RequestParam Integer quantity) {

        try {
            
            Cart savedCartItem = cartService.addToCart(userId, productId, quantity);
            return ResponseEntity.ok(savedCartItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
