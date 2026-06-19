package com.ukayukay.backend.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ShopDashboardDTO {
    private Integer shopId;
    private Integer sellerId;
    private String shopName;
    private String shopDescription;
    private BigDecimal averageRating;
    private Integer totalRatings;
    private Integer totalProducts;
    private Integer totalSales;
    private BigDecimal totalRevenue;
    private Integer pendingOrders;
    private Integer shippedOrders;
    private Integer deliveredOrders;
    private Integer cancelledOrders;
    private Integer returnRequests;
    private BigDecimal responseRate;
    private BigDecimal returnRate;
}
