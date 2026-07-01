package com.ukayukay.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Cart addToCart(Integer userId, Integer productId, Integer quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(String.format(AppConstants.USER_NOT_FOUND, userId)));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException(String.format(AppConstants.PRODUCT_NOT_FOUND, productId)));

        if ("SOLD".equalsIgnoreCase(product.getStatus())) {
            throw new RuntimeException("This unique vintage piece has already been sold!");
        }

        int amtToAdd = (quantity == null || quantity < 1) ? 1 : quantity;

        Cart existingItem = cartRepository.findByUser_UserIdAndProduct_ProductId(userId, productId)
                .orElse(null);
        if (existingItem != null) {
            int existingQty = existingItem.getQuantity() == null ? 0 : existingItem.getQuantity();
            existingItem.setQuantity(existingQty + amtToAdd);
            return cartRepository.saveAndFlush(existingItem);
        }

        Cart newCartItem = new Cart();
        newCartItem.setUser(user);
        newCartItem.setProduct(product);
        newCartItem.setQuantity(amtToAdd);

        return cartRepository.saveAndFlush(newCartItem);
    }

    public List<Cart> getUserCart(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException(String.format(AppConstants.USER_NOT_FOUND, userId));
        }
        return cartRepository.findByUser_UserId(userId);
    }

    @Transactional
    public void removeFromCart(Integer cartId) {
        if (!cartRepository.existsById(cartId)) {
            throw new RuntimeException("Cart item not found");
        }
        cartRepository.deleteById(cartId);
        cartRepository.flush();
    }

    @Transactional
    public void clearUserCart(Integer userId) {
        List<Cart> items = cartRepository.findByUser_UserId(userId);
        if (!items.isEmpty()) {
            cartRepository.deleteAll(items);
            cartRepository.flush();
        }
    }
}