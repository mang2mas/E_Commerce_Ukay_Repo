package com.ukayukay.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ukayukay.backend.entity.Cart;
import com.ukayukay.backend.service.CartService;
import com.ukayukay.backend.util.AppConstants;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/user/{userId}")
    public List<Cart> getCart(@PathVariable Integer userId) {
        return cartService.getCartByUserId(userId);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addItemToCart(@RequestParam Integer userId, @RequestParam Integer productId) {
        try {
            Cart item = cartService.addToCart(userId, productId);
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<String> removeFromCart(@PathVariable Integer cartId) {
        cartService.removeFromCart(cartId);
        return ResponseEntity.ok(AppConstants.ITEM_REMOVED_CART);
    }
}