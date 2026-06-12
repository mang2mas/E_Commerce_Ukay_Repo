package com.ukayukay.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.UserRepository;

// @Component - Disabled: Using real database instead of demo data
public class DemoDataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) {
        String sellerEmail = "seller@example.com";
        if (userRepository.findByEmail(sellerEmail).isEmpty()) {
            User seller = new User();
            seller.setEmail(sellerEmail);
            seller.setPassword("seller123");
            seller.setFullName("Demo Seller");
            seller.setRole(User.UserRole.seller);
            seller.setSellerApproved(true);
            seller.setBusinessName("Verified Demo Seller");
            seller.setBusinessDescription("Approved demo seller account for UI testing.");
            userRepository.save(seller);
            System.out.println("Demo seller account created: " + sellerEmail);
        }
    }
}
