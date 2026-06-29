package com.ukayukay.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ukayukay.backend.entity.OrderItem;
import com.ukayukay.backend.repository.OrderItemRepository;
import com.ukayukay.backend.repository.ProductRepository;

@Service
public class RevenueService {

    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public RevenueService(OrderItemRepository orderItemRepository,
                          ProductRepository productRepository) {
        this.orderItemRepository = orderItemRepository;
        this.productRepository   = productRepository;
    }

    /** Returns order items for a seller that are NOT cancelled/returned */
    private List<OrderItem> getCompletedItems(Integer sellerId) {
        return orderItemRepository.findByProduct_Seller_UserId(sellerId).stream()
            .filter(oi -> {
                String s = oi.getOrder().getOrderStatus();
                return !"CANCELLED".equalsIgnoreCase(s)
                    && !"RETURN_REQUESTED".equalsIgnoreCase(s)
                    && !"RETURNED".equalsIgnoreCase(s);
            })
            .collect(Collectors.toList());
    }

    public Map<String, Object> getRevenueStats(Integer sellerId) {
        List<OrderItem> items = getCompletedItems(sellerId);
        int itemsSold = items.stream().mapToInt(oi -> qty(oi)).sum();
        BigDecimal totalRevenue = items.stream()
            .map(oi -> oi.getPriceAtPurchase().multiply(BigDecimal.valueOf(qty(oi))))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgSale = itemsSold > 0
            ? totalRevenue.divide(BigDecimal.valueOf(itemsSold), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        // This-month revenue
        YearMonth now = YearMonth.now();
        BigDecimal thisMonthRevenue = items.stream()
            .filter(oi -> oi.getOrder().getCreatedAt() != null
                && YearMonth.from(oi.getOrder().getCreatedAt()).equals(now))
            .map(oi -> oi.getPriceAtPurchase().multiply(BigDecimal.valueOf(qty(oi))))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalProducts = productRepository.countBySeller_UserId(sellerId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRevenue",     totalRevenue);
        stats.put("thisMonthRevenue", thisMonthRevenue);
        stats.put("itemsSold",        itemsSold);
        stats.put("averageSale",      avgSale);
        stats.put("totalProducts",    totalProducts);
        return stats;
    }

    public Map<String, BigDecimal> getMonthlySales(Integer sellerId) {
        List<OrderItem> items = getCompletedItems(sellerId);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, BigDecimal> monthly = new TreeMap<>();
        for (OrderItem oi : items) {
            if (oi.getOrder().getCreatedAt() != null) {
                String month = oi.getOrder().getCreatedAt().format(fmt);
                BigDecimal line = oi.getPriceAtPurchase().multiply(BigDecimal.valueOf(qty(oi)));
                monthly.merge(month, line, BigDecimal::add);
            }
        }
        return monthly;
    }

    public List<Map<String, Object>> getCategoryBreakdown(Integer sellerId) {
        List<OrderItem> items = getCompletedItems(sellerId);
        Map<String, BigDecimal> catRevenue = new HashMap<>();
        Map<String, Integer>    catSold    = new HashMap<>();
        for (OrderItem oi : items) {
            String cat = oi.getProduct().getCategory() != null ? oi.getProduct().getCategory() : "Other";
            int q = qty(oi);
            catRevenue.merge(cat, oi.getPriceAtPurchase().multiply(BigDecimal.valueOf(q)), BigDecimal::add);
            catSold.merge(cat, q, Integer::sum);
        }
        BigDecimal grand = catRevenue.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        List<Map<String, Object>> result = new ArrayList<>();
        for (String cat : catRevenue.keySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("category",  cat);
            row.put("itemsSold", catSold.get(cat));
            row.put("revenue",   catRevenue.get(cat));
            BigDecimal pct = grand.compareTo(BigDecimal.ZERO) > 0
                ? catRevenue.get(cat).multiply(BigDecimal.valueOf(100))
                             .divide(grand, 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            row.put("percentageOfTotal", pct);
            result.add(row);
        }
        result.sort((a, b) -> ((BigDecimal) b.get("revenue")).compareTo((BigDecimal) a.get("revenue")));
        return result;
    }

    private int qty(OrderItem oi) {
        return oi.getQuantity() != null ? oi.getQuantity() : 1;
    }
}
