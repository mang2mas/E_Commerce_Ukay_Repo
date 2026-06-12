package com.ukayukay.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ukayukay.backend.entity.Product;
import com.ukayukay.backend.repository.ProductRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AuditLogService auditLogService; // 🆕 Injected to support moderation tracing

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

    public Product createProduct(Product product) {
        // 🔄 CHANGED: New thrift items now hold for moderation instead of posting live instantly
        product.setStatus("PENDING_APPROVAL"); 
        return productRepository.save(product);
    }

    // 🆕 ADDED: Moderation method to handle Admin approvals/rejections
    public Product moderateProduct(Integer productId, String newStatus, Integer adminId) {
        Product product = getProductById(productId);
        
        // Update the operational state (e.g., "AVAILABLE" or "REJECTED")
        product.setStatus(newStatus.toUpperCase());
        Product updatedProduct = productRepository.save(product);
        
        // Push a fresh log footprint into your audit table
        auditLogService.logAction(adminId, "PRODUCT_MODERATION_" + newStatus.toUpperCase(), "products");
        
        return updatedProduct;
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