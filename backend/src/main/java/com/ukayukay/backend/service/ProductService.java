package com.ukayukay.backend.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ukayukay.backend.entity.OrderItem;
import com.ukayukay.backend.entity.Product;
import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.OrderItemRepository;
import com.ukayukay.backend.repository.ProductRepository;
import com.ukayukay.backend.repository.UserRepository;
import com.ukayukay.backend.util.AppConstants;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final AuditLogService auditLogService;

    public ProductService(ProductRepository productRepository, UserRepository userRepository,
            OrderItemRepository orderItemRepository, AuditLogService auditLogService) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
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

    public List<Product> getRecommendedProducts(Integer userId, int limit) {
        List<Product> availableProducts = productRepository.findByStatus(AppConstants.PRODUCT_STATUS_AVAILABLE);
        if (availableProducts.isEmpty()) {
            return availableProducts;
        }

        List<Product> recommended = new ArrayList<>();
        Set<Integer> excludedProductIds = new HashSet<>();

        if (userId != null) {
            List<OrderItem> pastItems = orderItemRepository.findByOrder_Customer_UserId(userId);
            if (pastItems != null && !pastItems.isEmpty()) {
                Map<String, Long> categoryCounts = pastItems.stream()
                    .map(item -> item.getProduct() != null ? item.getProduct().getCategory() : null)
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

                Map<Integer, Long> sellerCounts = pastItems.stream()
                    .map(OrderItem::getProduct)
                    .filter(Objects::nonNull)
                    .map(Product::getSeller)
                    .filter(Objects::nonNull)
                    .map(User::getUserId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

                pastItems.stream()
                    .map(OrderItem::getProduct)
                    .filter(Objects::nonNull)
                    .map(Product::getProductId)
                    .filter(Objects::nonNull)
                    .forEach(excludedProductIds::add);

                List<String> favoriteCategories = categoryCounts.entrySet().stream()
                    .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

                List<Integer> favoriteSellerIds = sellerCounts.entrySet().stream()
                    .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

                for (String category : favoriteCategories) {
                    if (recommended.size() >= limit) {
                        break;
                    }

                    List<Product> candidates = productRepository.findTop9ByCategoryAndStatusAndProductIdNotIn(
                        category, AppConstants.PRODUCT_STATUS_AVAILABLE, new ArrayList<>(excludedProductIds));

                    for (Product candidate : candidates) {
                        if (recommended.size() >= limit) {
                            break;
                        }
                        if (!excludedProductIds.contains(candidate.getProductId())) {
                            recommended.add(candidate);
                            excludedProductIds.add(candidate.getProductId());
                        }
                    }
                }

                if (recommended.size() < limit) {
                    for (Integer sellerId : favoriteSellerIds) {
                        if (recommended.size() >= limit) {
                            break;
                        }

                        List<Product> sellerCandidates = productRepository.findTop9BySeller_UserIdAndStatusAndProductIdNotIn(
                            sellerId, AppConstants.PRODUCT_STATUS_AVAILABLE, new ArrayList<>(excludedProductIds));

                        for (Product candidate : sellerCandidates) {
                            if (recommended.size() >= limit) {
                                break;
                            }
                            if (!excludedProductIds.contains(candidate.getProductId())) {
                                recommended.add(candidate);
                                excludedProductIds.add(candidate.getProductId());
                            }
                        }
                    }
                }
            }
        }

        if (recommended.size() < limit) {
            List<Product> fallback = productRepository.findTop12ByStatusOrderByDateAddedDesc(AppConstants.PRODUCT_STATUS_AVAILABLE);
            for (Product product : fallback) {
                if (recommended.size() >= limit) {
                    break;
                }
                if (!excludedProductIds.contains(product.getProductId())) {
                    recommended.add(product);
                    excludedProductIds.add(product.getProductId());
                }
            }
        }

        if (recommended.isEmpty()) {
            Collections.shuffle(availableProducts);
            return availableProducts.stream().limit(limit).collect(Collectors.toList());
        }

        return recommended.stream().limit(limit).collect(Collectors.toList());
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

        if (product.getStatus() == null || product.getStatus().isBlank()) {
            product.setStatus(AppConstants.PRODUCT_STATUS_AVAILABLE);
        }
        product.setSeller(seller);
        return productRepository.save(product);
    }

    public Product createProduct(Product product) {
        if (product.getStatus() == null || product.getStatus().isBlank()) {
            product.setStatus("PENDING_APPROVAL");
        }
        return productRepository.save(product);
    }

    public Product moderateProduct(Integer productId, String newStatus, Integer adminId) {
        Product product = getProductById(productId);
        product.setStatus(newStatus.toUpperCase()); 
        Product updatedProduct = productRepository.save(product);
        auditLogService.logAction(adminId, "PRODUCT_MODERATION_" + newStatus.toUpperCase(), "products");
        return updatedProduct;
    }

    public Product updateProduct(Integer id, Product updatedProduct) {
        Product existing = getProductById(id);

        if (updatedProduct.getItemName() != null) {
            existing.setItemName(updatedProduct.getItemName());
        }
        if (updatedProduct.getCategory() != null) {
            existing.setCategory(updatedProduct.getCategory());
        }
        if (updatedProduct.getPrice() != null) {
            existing.setPrice(updatedProduct.getPrice());
        }
        if (updatedProduct.getStockQuantity() != null) {
            existing.setStockQuantity(updatedProduct.getStockQuantity());
        }
        if (updatedProduct.getDescription() != null) {
            existing.setDescription(updatedProduct.getDescription());
        }
        if (updatedProduct.getImagePath() != null) {
            existing.setImagePath(updatedProduct.getImagePath());
        }
        if (updatedProduct.getSize() != null) {
            existing.setSize(updatedProduct.getSize());
        }
        if (updatedProduct.getItemCondition() != null) {
            existing.setItemCondition(updatedProduct.getItemCondition());
        }
        if (updatedProduct.getStatus() != null) {
            existing.setStatus(updatedProduct.getStatus());
        }

        return productRepository.save(existing);
    }

    public void deleteProduct(Integer id) {
        productRepository.deleteById(id);
    }
}