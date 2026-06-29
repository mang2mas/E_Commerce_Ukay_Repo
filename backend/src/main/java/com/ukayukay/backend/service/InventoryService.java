package com.ukayukay.backend.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ukayukay.backend.entity.Product;
import com.ukayukay.backend.repository.ProductRepository;
import com.ukayukay.backend.util.AppConstants;

@Service
public class InventoryService {

    private final ProductRepository productRepository;

    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Map<String, Object> getInventoryStats(Integer sellerId) {
        List<Product> all = productRepository.findBySeller_UserId(sellerId);
        long total     = all.size();
        long available = all.stream().filter(p -> "AVAILABLE".equalsIgnoreCase(p.getStatus())).count();
        long sold      = all.stream().filter(p -> "SOLD".equalsIgnoreCase(p.getStatus())).count();
        long archived  = all.stream().filter(p -> "ARCHIVED".equalsIgnoreCase(p.getStatus())).count();
        long pending   = all.stream().filter(p -> "PENDING_APPROVAL".equalsIgnoreCase(p.getStatus())).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalItems", total);
        stats.put("available", available);
        stats.put("sold", sold);
        stats.put("archived", archived);
        stats.put("pendingApproval", pending);
        return stats;
    }

    public List<Product> getSellerProducts(Integer sellerId) {
        return productRepository.findBySeller_UserId(sellerId);
    }

    public Product updateProductStatus(Integer productId, String status, Integer sellerId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException(
                String.format(AppConstants.PRODUCT_NOT_FOUND, productId)));

        if (!product.getSeller().getUserId().equals(sellerId)) {
            throw new RuntimeException("You are not authorised to modify this product.");
        }

        String normalised = status.toUpperCase();
        if (!List.of("AVAILABLE", "ARCHIVED", "PENDING_APPROVAL", "REJECTED").contains(normalised)) {
            throw new RuntimeException("Invalid status value: " + status);
        }

        product.setStatus(normalised);
        return productRepository.save(product);
    }
}
