package com.ukayukay.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ukayukay.backend.entity.Product;
import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.ProductRepository;
import com.ukayukay.backend.repository.UserRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Product> getAvailableProducts() {
        return productRepository.findByStatus("AVAILABLE");
    }

    public List<Product> getAvailableProductsByCategory(String category) {
        return productRepository.findByCategoryAndStatus(category, "AVAILABLE");
    }

    public Product getProductById(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
    }

    public Product createProductForSeller(Integer sellerId, Product product) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found with ID: " + sellerId));

        if (seller.getRole() == null || !User.UserRole.seller.equals(seller.getRole())) {
            throw new RuntimeException("Only approved sellers can add products.");
        }
        if (!Boolean.TRUE.equals(seller.getSellerApproved())) {
            throw new RuntimeException("Seller account is not approved yet.");
        }
        if (seller.getBusinessName() == null || seller.getBusinessName().isBlank()
                || seller.getBusinessDescription() == null || seller.getBusinessDescription().isBlank()) {
            throw new RuntimeException("Seller business name and description must be approved before adding products.");
        }

        product.setStatus("AVAILABLE");
        product.setSeller(seller);
        return productRepository.save(product);
    }

    public Product createProduct(Product product) {
        product.setStatus("AVAILABLE"); // New items default to available
        return productRepository.save(product);
    }

    public Product updateProduct(Integer id, Product updatedProduct) {
        Product existing = getProductById(id);
        existing.setItemName(updatedProduct.getItemName());
        existing.setCategory(updatedProduct.getCategory());
        existing.setPrice(updatedProduct.getPrice());
        existing.setStockQuantity(updatedProduct.getStockQuantity());
        existing.setDescription(updatedProduct.getDescription());
        existing.setImagePath(updatedProduct.getImagePath());
        existing.setSize(updatedProduct.getSize());
        existing.setItemCondition(updatedProduct.getItemCondition());
        existing.setStatus(updatedProduct.getStatus());
        return productRepository.save(existing);
    }

    public void deleteProduct(Integer id) {
        productRepository.deleteById(id);
    }
}