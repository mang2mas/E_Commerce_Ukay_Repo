package com.ukayukay.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ukayukay.backend.entity.Product;
import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.ProductRepository;
import com.ukayukay.backend.repository.UserRepository;
import com.ukayukay.backend.util.AppConstants;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public ProductService(ProductRepository productRepository, UserRepository userRepository,
            AuditLogService auditLogService) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public List<Product> getAvailableProducts() {
        return productRepository.findByStatus(AppConstants.PRODUCT_STATUS_AVAILABLE);
    }

    public List<Product> getAvailableProductsByCategory(String category) {
        return productRepository.findByCategoryAndStatus(category, AppConstants.PRODUCT_STATUS_AVAILABLE);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                    String.format(AppConstants.PRODUCT_NOT_FOUND, id)));
    }

    public List<Product> getProductsBySeller(Integer sellerId) {
        if (!userRepository.existsById(sellerId)) {
            throw new RuntimeException(String.format(AppConstants.SELLER_NOT_FOUND, sellerId));
        }
        return productRepository.findBySeller_UserId(sellerId);
    }

    public Product createProductForSeller(Integer sellerId, Product product) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException(
                    String.format(AppConstants.SELLER_NOT_FOUND, sellerId)));

        if (seller.getRole() == null || !User.UserRole.seller.equals(seller.getRole())) {
            throw new RuntimeException(AppConstants.INVALID_SELLER);
        }
        if (!Boolean.TRUE.equals(seller.getSellerApproved())) {
            throw new RuntimeException(AppConstants.SELLER_NOT_APPROVED);
        }
        if (seller.getBusinessName() == null || seller.getBusinessName().isBlank()
                || seller.getBusinessDescription() == null || seller.getBusinessDescription().isBlank()) {
            throw new RuntimeException(AppConstants.SELLER_INFO_INCOMPLETE);
        }

        product.setStatus(AppConstants.PRODUCT_STATUS_AVAILABLE);
        product.setSeller(seller);
        return productRepository.save(product);
    }

    public Product createProduct(Product product) {
        // New products added directly to the catalog are held for moderation.
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