package com.ukayukay.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogService auditLogService;

    public User verifySeller(Integer sellerId, User.VerificationStatus status, Integer adminId) {
        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new RuntimeException("Seller account not found with ID: " + sellerId));
            
        seller.setVerificationStatus(status);
        User updatedSeller = userRepository.save(seller);
        
        auditLogService.logAction(adminId, "SELLER_VERIFICATION_" + status, "users");
        
        return updatedSeller;
    }

    public User toggleAccountStatus(Integer userId, boolean activate, Integer adminId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User account not found with ID: " + userId));
            
        user.setIsActive(activate);
        User updatedUser = userRepository.save(user);
        
        String actionType = activate ? "ACCOUNT_REACTIVATED" : "ACCOUNT_SUSPENDED";
        auditLogService.logAction(adminId, actionType, "users");
        
        return updatedUser;
    }
}