package com.ukayukay.backend.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ukayukay.backend.dto.ShopDashboardDTO;
import com.ukayukay.backend.entity.OrderItem;
import com.ukayukay.backend.entity.Product;
import com.ukayukay.backend.entity.Shop;
import com.ukayukay.backend.entity.ShopRating;
import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.OrderItemRepository;
import com.ukayukay.backend.repository.ProductRepository;
import com.ukayukay.backend.repository.ShopRatingRepository;
import com.ukayukay.backend.repository.ShopRepository;
import com.ukayukay.backend.repository.UserRepository;
import com.ukayukay.backend.util.AppConstants;

@Service
public class ShopService {

    private final ShopRepository shopRepository;
    private final ShopRatingRepository shopRatingRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    public ShopService(ShopRepository shopRepository,
                       ShopRatingRepository shopRatingRepository,
                       UserRepository userRepository,
                       ProductRepository productRepository,
                       OrderItemRepository orderItemRepository) {
        this.shopRepository = shopRepository;
        this.shopRatingRepository = shopRatingRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public Shop getShopBySellerId(Integer sellerId) {
        return shopRepository.findBySeller_UserId(sellerId)
                .orElseThrow(() -> new RuntimeException(
                        String.format(AppConstants.SHOP_NOT_FOUND, sellerId)));
    }

    public Shop createOrUpdateShopProfile(Integer sellerId, Shop shopPayload) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException(
                        String.format(AppConstants.SELLER_NOT_FOUND, sellerId)));

        if (seller.getRole() == null || !User.UserRole.seller.equals(seller.getRole())) {
            throw new RuntimeException(AppConstants.INVALID_SELLER);
        }

        Shop shop = shopRepository.findBySeller_UserId(sellerId).orElse(new Shop());
        shop.setSeller(seller);
        shop.setShopName(shopPayload.getShopName());
        shop.setShopDescription(shopPayload.getShopDescription());
        shop.setShopLogoUrl(shopPayload.getShopLogoUrl());
        shop.setShopBannerUrl(shopPayload.getShopBannerUrl());
        shop.setResponseRate(shopPayload.getResponseRate() != null ? shopPayload.getResponseRate() : BigDecimal.ZERO);
        shop.setReturnRate(shopPayload.getReturnRate() != null ? shopPayload.getReturnRate() : BigDecimal.ZERO);
        shop.setIsActive(shopPayload.getIsActive() != null ? shopPayload.getIsActive() : Boolean.TRUE);
        shop.setTotalProducts(productRepository.countBySeller_UserId(sellerId).intValue());
        List<ShopRating> ratings = shopRatingRepository.findBySeller_UserId(sellerId);
        shop.setTotalRatings(ratings.size());
        shop.setAverageRating(calculateAverageRating(ratings));
        shop.setTotalSales(orderItemRepository.findByProduct_Seller_UserId(sellerId).size());

        return shopRepository.save(shop);
    }

    public List<ShopRating> getShopRatings(Integer sellerId) {
        ensureSellerExists(sellerId);
        return shopRatingRepository.findBySeller_UserId(sellerId);
    }

    public ShopRating addShopRating(Integer sellerId, Integer buyerId, ShopRating ratingPayload) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException(
                        String.format(AppConstants.SELLER_NOT_FOUND, sellerId)));

        if (seller.getRole() == null || !User.UserRole.seller.equals(seller.getRole())) {
            throw new RuntimeException(AppConstants.INVALID_SELLER);
        }

        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException(
                        String.format(AppConstants.USER_NOT_FOUND, buyerId)));

        Shop shop = shopRepository.findBySeller_UserId(sellerId)
                .orElseThrow(() -> new RuntimeException(
                        String.format(AppConstants.SHOP_NOT_FOUND, sellerId)));

        ratingPayload.setSeller(seller);
        ratingPayload.setBuyer(buyer);
        ratingPayload.setRating(ratingPayload.getRating() == null ? 0 : ratingPayload.getRating());

        ShopRating savedRating = shopRatingRepository.save(ratingPayload);
        updateShopRatingMetrics(shop);

        return savedRating;
    }

    public ShopDashboardDTO getShopDashboard(Integer sellerId) {
        Shop shop = getShopBySellerId(sellerId);
        List<OrderItem> orderItems = orderItemRepository.findByProduct_Seller_UserId(sellerId);

        ShopDashboardDTO dashboard = new ShopDashboardDTO();
        dashboard.setShopId(shop.getShopId());
        dashboard.setSellerId(sellerId);
        dashboard.setShopName(shop.getShopName());
        dashboard.setShopDescription(shop.getShopDescription());
        dashboard.setAverageRating(shop.getAverageRating());
        dashboard.setTotalRatings(shop.getTotalRatings());
        dashboard.setTotalProducts(productRepository.countBySeller_UserId(sellerId).intValue());
        dashboard.setTotalSales(orderItems.size());
        dashboard.setTotalRevenue(calculateTotalRevenue(orderItems));
        dashboard.setPendingOrders(countOrdersByStatus(orderItems, AppConstants.ORDER_STATUS_PENDING));
        dashboard.setShippedOrders(countOrdersByStatus(orderItems, AppConstants.ORDER_STATUS_SHIPPED));
        dashboard.setDeliveredOrders(countOrdersByStatus(orderItems, AppConstants.ORDER_STATUS_DELIVERED));
        dashboard.setCancelledOrders(countOrdersByStatus(orderItems, AppConstants.ORDER_STATUS_CANCELLED));
        dashboard.setReturnRequests(countOrdersByStatus(orderItems, AppConstants.ORDER_STATUS_RETURN_REQUESTED));
        dashboard.setResponseRate(shop.getResponseRate());
        dashboard.setReturnRate(shop.getReturnRate());

        return dashboard;
    }

    private void ensureSellerExists(Integer sellerId) {
        if (!userRepository.existsById(sellerId)) {
            throw new RuntimeException(String.format(AppConstants.SELLER_NOT_FOUND, sellerId));
        }
    }

    private BigDecimal calculateAverageRating(List<ShopRating> ratings) {
        if (ratings.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (ShopRating rating : ratings) {
            total = total.add(BigDecimal.valueOf(rating.getRating()));
        }
        return total.divide(BigDecimal.valueOf(ratings.size()), 2, BigDecimal.ROUND_HALF_UP);
    }

    private void updateShopRatingMetrics(Shop shop) {
        List<ShopRating> ratings = shopRatingRepository.findBySeller_UserId(shop.getSeller().getUserId());
        shop.setTotalRatings(ratings.size());
        shop.setAverageRating(calculateAverageRating(ratings));
        shopRepository.save(shop);
    }

    private BigDecimal calculateTotalRevenue(List<OrderItem> orderItems) {
        BigDecimal revenue = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            if (item.getPriceAtPurchase() != null && item.getQuantity() != null) {
                revenue = revenue.add(item.getPriceAtPurchase()
                        .multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }
        return revenue;
    }

    private Integer countOrdersByStatus(List<OrderItem> orderItems, String orderStatus) {
        if (orderItems == null || orderItems.isEmpty()) {
            return 0;
        }
        return (int) orderItems.stream()
                .map(OrderItem::getOrder)
                .filter(order -> orderStatus.equalsIgnoreCase(order.getOrderStatus()))
                .distinct()
                .count();
    }
}
