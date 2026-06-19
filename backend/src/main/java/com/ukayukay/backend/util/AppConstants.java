package com.ukayukay.backend.util;

import java.math.BigDecimal;

public class AppConstants {
    
    // Product Status
    public static final String PRODUCT_STATUS_AVAILABLE = "AVAILABLE";
    public static final String PRODUCT_STATUS_SOLD = "SOLD";
    public static final String ORDER_STATUS_PENDING = "PENDING";
    public static final String ORDER_STATUS_SHIPPED = "SHIPPED";
    public static final String ORDER_STATUS_DELIVERED = "DELIVERED";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";
    public static final String ORDER_STATUS_RETURN_REQUESTED = "RETURN_REQUESTED";
    public static final String ORDER_STATUS_RETURNED = "RETURNED";
    
    // Error Messages
    public static final String PRODUCT_NOT_FOUND = "Product not found with ID: %d";
    public static final String USER_NOT_FOUND = "User not found with ID: %d";
    public static final String SELLER_NOT_FOUND = "Seller not found with ID: %d";
    public static final String SHOP_NOT_FOUND = "Shop not found for seller ID: %d";
    public static final String INVALID_SELLER = "Only approved sellers can add products.";
    public static final String SELLER_NOT_APPROVED = "Seller account is not approved yet.";
    public static final String SELLER_INFO_INCOMPLETE = 
        "Seller business name and description must be approved before adding products.";
    public static final String USER_NOT_FOUND_EMAIL = "User not found with this email!";
    public static final String INVALID_PASSWORD = "Invalid password!";
    public static final String EMAIL_EXISTS = "An account with this email already exists!";
    public static final String ITEM_ALREADY_SOLD = "This item has already been sold!";
    public static final String CART_EMPTY = "Your shopping cart is empty!";
    public static final String ADDRESS_REQUIRED = "Billing and Shipping Address information is mandatory!";
    public static final String PAYMENT_METHOD_REQUIRED = "Payment Method selection is mandatory!";
    public static final String ORDER_NOT_FOUND = "Order record matching ID not found";
    
    // Success Messages
    public static final String PRODUCT_DELETED = "Product with ID %d has been successfully deleted.";
    public static final String ITEM_REMOVED_CART = "Item removed from cart successfully.";
    
    // Audit Actions
    public static final String AUDIT_ADMIN_LOGIN = "ADMIN_LOGIN";
    public static final String AUDIT_TABLE_USERS = "users";
    
    // Order Calculation Constants
    public static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.20");
    public static final BigDecimal TAX_RATE = new BigDecimal("0.12");
    public static final BigDecimal SHIPPING_FEE = new BigDecimal("100.00");
    
    private AppConstants() {
        // Prevent instantiation
    }
}
