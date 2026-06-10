package com.ukayukay.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ukayukay.backend.entity.Cart;
import com.ukayukay.backend.entity.Product;
import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.CartRepository;
import com.ukayukay.backend.repository.ProductRepository;
import com.ukayukay.backend.repository.UserRepository;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<Cart> getCartByUserId(Integer userId) {
        return cartRepository.findByUser_UserId(userId);
    }

    public Cart addToCart(Integer userId, Integer productId) {
        // 1. Verify if the item is already in this user's cart
        if (cartRepository.existsByUser_UserIdAndProduct_ProductId(userId, productId)) {
            throw new RuntimeException("This unique thrift item is already in your cart!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if ("SOLD".equalsIgnoreCase(product.getStatus())) {
            throw new RuntimeException("Sorry, this item has already been sold!");
        }

        Cart cartItem = new Cart();
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(1);

        return cartRepository.save(cartItem);
    }

    public void removeFromCart(Integer cartId) {
        cartRepository.deleteById(cartId);
    }
}