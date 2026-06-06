package com.ukayukay.backend.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ukayukay.backend.entity.Cart;
import com.ukayukay.backend.entity.Product;
import com.ukayukay.backend.repository.CartRepository;
import com.ukayukay.backend.repository.ProductRepository;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    public Cart addToCart(Integer userId, Integer productId, Integer quantity) {
        

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found!"));


        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Not enough stock for item: " + product.getItemName());
        }

        Cart newCartItem = new Cart();
        newCartItem.setUserId(userId);
        newCartItem.setProductId(productId);
        newCartItem.setQuantity(quantity);

        return cartRepository.save(newCartItem);
    }
}
