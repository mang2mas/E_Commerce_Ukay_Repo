package com.ukayukay.backend.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ukayukay.backend.entity.Product;
import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.ProductRepository;
import com.ukayukay.backend.repository.UserRepository;

@Component
public class DemoDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
        private final PasswordEncoder passwordEncoder;

        @Value("${app.bootstrap.admin-email:admin@ukayukay.local}")
        private String adminEmail;

        @Value("${app.bootstrap.admin-password:Admin@12345}")
        private String adminPassword;

        @Value("${app.bootstrap.admin-full-name:System Administrator}")
        private String adminFullName;

        public DemoDataLoader(UserRepository userRepository, ProductRepository productRepository,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
                this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
                seedAdminAccount();

        if (productRepository.count() > 0) {
            return;
        }

        User sellerA = createSeller("seller1@example.com", "Vintage Vibes Co.", "A curated seller of premium secondhand tops and denim.");
        User sellerB = createSeller("seller2@example.com", "Retro Racks", "Authentic retro pieces for bold everyday looks.");
        User sellerC = createSeller("seller3@example.com", "Classic Finds", "Timeless ukay staples sourced from trusted suppliers.");

        createProduct(sellerA, "90s Oversized Denim Jacket", "Outerwear", new BigDecimal("1999.00"), 4,
                "Very good condition vintage denim jacket with soft broken-in feel.", "/images/item1.jpg", "L", "USED", LocalDate.now().minusDays(3), "Denim");
        createProduct(sellerA, "Cozy Knit Sweater", "Tops", new BigDecimal("899.00"), 6,
                "Warm knit sweater in earth tones with the perfect retro fit.", "/images/item2.jpg", "M", "USED", LocalDate.now().minusDays(5), "Knit");
        createProduct(sellerA, "Floral Slip Dress", "Tops", new BigDecimal("1099.00"), 3,
                "Delicate vintage floral dress for everyday styling.", "/images/item3.jpg", "S", "USED", LocalDate.now().minusDays(2), "Floral");

        createProduct(sellerB, "Leather Crossbody Bag", "Bags", new BigDecimal("1299.00"), 5,
                "Classic leather crossbody bag with clean hardware details.", "/images/item4.jpg", "One Size", "USED", LocalDate.now().minusDays(7), "Leather");
        createProduct(sellerB, "Retro Graphic Tee", "Tops", new BigDecimal("599.00"), 8,
                "Soft vintage tee with bold graphic logo.", "/images/item5.jpg", "M", "USED", LocalDate.now().minusDays(6), "Graphic");
        createProduct(sellerB, "Statement Bucket Hat", "Accessories", new BigDecimal("399.00"), 10,
                "Essential retro accessory for a finished look.", "/images/item6.jpg", "One Size", "USED", LocalDate.now().minusDays(4), "Hat");

        createProduct(sellerC, "High-Waist Pleated Skirt", "Bottoms", new BigDecimal("999.00"), 5,
                "Chic pleated skirt ideal for both casual and dressy outfits.", "/images/item7.jpg", "M", "USED", LocalDate.now().minusDays(8), "Pleated");
        createProduct(sellerC, "Cropped Blazer", "Outerwear", new BigDecimal("1499.00"), 2,
                "Retro-inspired cropped blazer with sharp tailoring.", "/images/item8.jpg", "S", "USED", LocalDate.now().minusDays(1), "Blazer");
        createProduct(sellerC, "Vintage Chain Necklace", "Accessories", new BigDecimal("499.00"), 12,
                "Timeless chain necklace to layer with any outfit.", "/images/item9.jpg", "One Size", "USED", LocalDate.now().minusDays(9), "Jewelry");

        // Add additional popular sample items for homepage visibility.
        createProduct(sellerA, "Minimalist White Shirt", "Shirts", new BigDecimal("699.00"), 7,
                "Clean, crisp button-up shirt that works with any ensemble.", "/images/item10.jpg", "M", "USED", LocalDate.now().minusDays(10), "Minimalist");
        createProduct(sellerB, "Plaid Wool Scarf", "Accessories", new BigDecimal("349.00"), 9,
                "Cozy plaid scarf for layering and added texture.", "/images/item11.jpg", "One Size", "USED", LocalDate.now().minusDays(11), "Winter");
        createProduct(sellerC, "Distressed Mom Jeans", "Bottoms", new BigDecimal("1199.00"), 4,
                "Relaxed-fit jeans with lived-in character.", "/images/item12.jpg", "L", "USED", LocalDate.now().minusDays(12), "Denim");
        createProduct(sellerB, "Retro Sunglasses", "Accessories", new BigDecimal("299.00"), 15,
                "Vintage shades with timeless appeal.", "/images/item13.jpg", "One Size", "USED", LocalDate.now().minusDays(13), "Sunglasses");
        createProduct(sellerA, "Cropped Graphic Sweatshirt", "Tops", new BigDecimal("799.00"), 6,
                "Soft sweatshirt with statement graphics and a cozy fit.", "/images/item14.jpg", "M", "USED", LocalDate.now().minusDays(14), "Streetwear");

        createProduct(sellerA, "Baggy Pants", "Bottoms", new BigDecimal("899.00"), 5,
                "Comfortable baggy pants for relaxed everyday wear.", "/images/item15.jpg", "L", "USED", LocalDate.now().minusDays(16), "Casual");
        createProduct(sellerB, "Laced Top", "Tops", new BigDecimal("749.00"), 3,
                "Delicate laced top with a flattering fit.", "/images/item16.jpg", "S", "USED", LocalDate.now().minusDays(15), "Lace");
        createProduct(sellerC, "Ripped Jeans", "Bottoms", new BigDecimal("1099.00"), 4,
                "Trendy ripped jeans with a vintage washed look.", "/images/item17.jpg", "M", "USED", LocalDate.now().minusDays(14), "Denim");

        System.out.println("Seeded demo products and sellers for homepage recommendations.");
    }

        private void seedAdminAccount() {
                userRepository.findByEmail(adminEmail).orElseGet(() -> {
                        User admin = new User();
                        admin.setEmail(adminEmail);
                        admin.setPassword(passwordEncoder.encode(adminPassword));
                        admin.setFullName(adminFullName);
                        admin.setRole(User.UserRole.admin);
                        admin.setIsActive(true);
                        admin.setIsShadowBanned(false);
                        admin.setSellerApproved(true);
                        admin.setVerificationStatus(User.VerificationStatus.APPROVED);
                        return userRepository.save(admin);
                });
        }

    private User createSeller(String email, String businessName, String description) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User seller = new User();
            seller.setEmail(email);
            seller.setPassword("seller123");
            seller.setFullName(businessName);
            seller.setRole(User.UserRole.seller);
            seller.setSellerApproved(true);
            seller.setBusinessName(businessName);
                        seller.setBusinessDescription(description);
                        seller.setIsActive(true);
            return userRepository.save(seller);
        });
    }

    private void createProduct(User seller, String itemName, String category, BigDecimal price, Integer stockQuantity,
            String description, String imagePath, String size, String itemCondition, LocalDate dateAdded, String style) {
        Product product = new Product();
        product.setItemName(itemName);
        product.setCategory(category);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setDescription(description);
        product.setImagePath(imagePath);
        product.setSize(size);
        product.setItemCondition(itemCondition);
        product.setStatus("AVAILABLE");
        product.setDateAdded(dateAdded);
        product.setStyle(style);
        product.setSeller(seller);
        productRepository.save(product);
    }
}
