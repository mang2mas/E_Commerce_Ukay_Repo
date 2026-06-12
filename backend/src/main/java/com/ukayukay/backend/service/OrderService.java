package com.ukayukay.backend.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ukayukay.backend.entity.Cart;
import com.ukayukay.backend.entity.Order;
import com.ukayukay.backend.entity.OrderItem; // Import added
import com.ukayukay.backend.entity.Product;
import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.CartRepository;
import com.ukayukay.backend.repository.OrderItemRepository;
import com.ukayukay.backend.repository.OrderRepository; // Import added
import com.ukayukay.backend.repository.ProductRepository;
import com.ukayukay.backend.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderItemRepository orderItemRepository; // Injected new repository

    @Transactional
    public Order checkout(Integer userId, String shippingAddress, String paymentMethod) {
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            throw new RuntimeException("Billing and Shipping Address information is mandatory!");
        }
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new RuntimeException("Payment Method selection is mandatory!");
        }

        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Customer profile not found"));

        List<Cart> cartItems = cartRepository.findByUser_UserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Your shopping cart is empty!");
        }

        BigDecimal baseAmount = BigDecimal.ZERO;
        for (Cart item : cartItems) {
            if ("SOLD".equalsIgnoreCase(item.getProduct().getStatus())) {
                throw new RuntimeException("Item '" + item.getProduct().getItemName() + "' is already sold!");
            }
            baseAmount = baseAmount.add(item.getProduct().getPrice());
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (customer.getIsDiscountElig() != null && customer.getIsDiscountElig() && 
            ("SENIOR".equalsIgnoreCase(customer.getDiscountType()) || "PWD".equalsIgnoreCase(customer.getDiscountType()))) {
            discountAmount = baseAmount.multiply(new BigDecimal("0.20"));
        }

        BigDecimal discountedBase = baseAmount.subtract(discountAmount);
        BigDecimal taxAmount = discountedBase.multiply(new BigDecimal("0.12"));
        BigDecimal shippingFee = new BigDecimal("100.00");
        BigDecimal totalAmount = discountedBase.add(taxAmount).add(shippingFee);

        Order order = new Order();
        order.setCustomer(customer);
        order.setBaseAmount(baseAmount);
        order.setDiscountAmount(discountAmount);
        order.setTaxAmount(taxAmount);
        order.setShippingFee(shippingFee);
        order.setTotalAmount(totalAmount);
        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod(paymentMethod);
        order.setOrderStatus("PENDING");

        Order savedOrder = orderRepository.save(order);

        // FIX LOGIC: Process inventory changes AND save receipt history entry
        for (Cart item : cartItems) {
            Product prod = item.getProduct();
            
            // 1. Log this item snapshot into the OrderItems history table
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(prod);
            orderItem.setPriceAtPurchase(prod.getPrice()); // Freeze price at this instant
            orderItemRepository.save(orderItem);

            // 2. Flip stock status
            prod.setStatus("SOLD");
            productRepository.save(prod);
            
            // 3. Clear from shopping cart
            cartRepository.delete(item);
        }

        return savedOrder;
    }

    public List<Order> getCustomerOrders(Integer userId) {
        return orderRepository.findByCustomer_UserIdOrderByCreatedAtDesc(userId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order updateOrderStatus(Integer orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order record matching ID not found"));
        order.setOrderStatus(newStatus.toUpperCase());
        return orderRepository.save(order);
    }
}