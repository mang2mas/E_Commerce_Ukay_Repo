package com.ukayukay.backend.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import com.ukayukay.backend.entity.Cart;
import com.ukayukay.backend.entity.Order;
import com.ukayukay.backend.entity.OrderItem;
import com.ukayukay.backend.entity.Product;
import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.CartRepository;
import com.ukayukay.backend.repository.OrderItemRepository;
import com.ukayukay.backend.repository.OrderRepository;
import com.ukayukay.backend.repository.ProductRepository;
import com.ukayukay.backend.repository.UserRepository;
import com.ukayukay.backend.util.AppConstants;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository,
                        UserRepository userRepository, ProductRepository productRepository,
                        OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public Order checkout(Integer userId, String shippingAddress, String paymentMethod) {
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            throw new RuntimeException(AppConstants.ADDRESS_REQUIRED);
        }
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new RuntimeException(AppConstants.PAYMENT_METHOD_REQUIRED);
        }

        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(String.format(AppConstants.USER_NOT_FOUND, userId)));

        List<Cart> cartItems = cartRepository.findByUser_UserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException(AppConstants.CART_EMPTY);
        }

        BigDecimal baseAmount = calculateBaseAmount(cartItems);
        BigDecimal discountAmount = calculateDiscount(customer, baseAmount);
        BigDecimal discountedBase = baseAmount.subtract(discountAmount);
        
        BigDecimal taxAmount = discountedBase.multiply(AppConstants.TAX_RATE);
        BigDecimal totalAmount = discountedBase.add(taxAmount).add(AppConstants.SHIPPING_FEE);

        Order order = createOrder(customer, shippingAddress, paymentMethod, 
                                   baseAmount, discountAmount, taxAmount, totalAmount);
        Order savedOrder = orderRepository.save(order);

        processCartItems(cartItems, savedOrder);

        return savedOrder;
    }

    public List<Order> getCustomerOrders(Integer userId) {
        return orderRepository.findByCustomer_UserIdOrderByCreatedAtDesc(userId);
    }

    public List<OrderItem> getOrderItems(Integer orderId) {
        return orderItemRepository.findByOrder_OrderId(orderId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order updateOrderStatus(Integer orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException(AppConstants.ORDER_NOT_FOUND));

        String current = order.getOrderStatus();
        String next = newStatus.toUpperCase();
        validateOrderTransition(current, next);

        order.setOrderStatus(next);
        return orderRepository.save(order);
    }

    private void validateOrderTransition(String current, String next) {
        boolean valid = switch (current) {
            case "PENDING" -> "SHIPPED".equals(next) || "CANCELLED".equals(next);
            case "SHIPPED" -> "DELIVERED".equals(next) || "RETURN_REQUESTED".equals(next);
            case "DELIVERED" -> "RETURN_REQUESTED".equals(next);
            case "RETURN_REQUESTED" -> "RETURNED".equals(next) || "DELIVERED".equals(next);
            default -> false;
        };
        if (!valid) {
            throw new IllegalStateException(
                String.format("Invalid order status transition: %s → %s", current, next));
        }
    }

    private BigDecimal calculateBaseAmount(List<Cart> cartItems) {
        BigDecimal baseAmount = BigDecimal.ZERO;
        for (Cart item : cartItems) {
            if (AppConstants.PRODUCT_STATUS_SOLD.equalsIgnoreCase(item.getProduct().getStatus())) {
                throw new RuntimeException(
                    String.format("Item '%s' is already sold!", item.getProduct().getItemName()));
            }
            Integer quantityValue = item.getQuantity();
            int quantity = quantityValue == null ? 1 : quantityValue;
            BigDecimal itemTotal = item.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(quantity));
            baseAmount = baseAmount.add(itemTotal);
        }
        return baseAmount;
    }

    private BigDecimal calculateDiscount(User customer, BigDecimal baseAmount) {
        if (customer.getIsDiscountElig() != null && customer.getIsDiscountElig() && 
            isEligibleForDiscount(customer.getDiscountType())) {
            return baseAmount.multiply(AppConstants.DISCOUNT_RATE);
        }
        return BigDecimal.ZERO;
    }

    private boolean isEligibleForDiscount(String discountType) {
        return "SENIOR".equalsIgnoreCase(discountType) || "PWD".equalsIgnoreCase(discountType);
    }

    private Order createOrder(User customer, String shippingAddress, String paymentMethod,
                             BigDecimal baseAmount, BigDecimal discountAmount, 
                             BigDecimal taxAmount, BigDecimal totalAmount) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setBaseAmount(baseAmount);
        order.setDiscountAmount(discountAmount);
        order.setTaxAmount(taxAmount);
        order.setShippingFee(AppConstants.SHIPPING_FEE);
        order.setTotalAmount(totalAmount);
        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod(paymentMethod);
        order.setOrderStatus(AppConstants.ORDER_STATUS_PENDING);
        return order;
    }

    private void processCartItems(List<Cart> cartItems, Order savedOrder) {
        for (Cart item : cartItems) {
            Product prod = item.getProduct();
            Integer quantityValue = item.getQuantity();
            int quantity = quantityValue == null ? 1 : quantityValue;

            createOrderItem(savedOrder, prod, quantity);
            markProductAsSold(prod);
            removeFromCart(item);
        }
    }

    private void createOrderItem(Order order, Product product, int quantity) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setPriceAtPurchase(product.getPrice());
        orderItem.setQuantity(quantity);
        orderItemRepository.save(orderItem);
    }

    private void markProductAsSold(Product product) {
        product.setStatus(AppConstants.PRODUCT_STATUS_SOLD);
        productRepository.save(product);
    }

    private void removeFromCart(Cart item) {
        cartRepository.delete(item);
    }
}