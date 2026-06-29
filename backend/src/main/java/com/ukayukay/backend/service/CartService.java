package com.ukayukay.backend.service;


import java.util.List;

import org.springframework.stereotype.Service;

import com.ukayukay.backend.entity.Cart;
import com.ukayukay.backend.entity.Product;
import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.CartRepository;
import com.ukayukay.backend.repository.ProductRepository;
import com.ukayukay.backend.repository.UserRepository;
import com.ukayukay.backend.util.AppConstants;


@Service
public class CartService {


    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;


    public CartService(CartRepository cartRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }


    public List<Cart> getCartByUserId(Integer userId) {
        return cartRepository.findByUser_UserId(userId);
    }


    public Cart addToCart(Integer userId, Integer productId) {
        if (cartRepository.existsByUser_UserIdAndProduct_ProductId(userId, productId)) {
            throw new RuntimeException("This item is already in your cart!");
        }


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                    String.format(AppConstants.USER_NOT_FOUND, userId)));
       
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException(
                    String.format(AppConstants.PRODUCT_NOT_FOUND, productId)));


        if ("SOLD".equalsIgnoreCase(product.getStatus())) {
            throw new RuntimeException("This item has already been sold!");
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



