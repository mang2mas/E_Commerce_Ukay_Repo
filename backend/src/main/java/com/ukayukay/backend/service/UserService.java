package com.ukayukay.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ukayukay.backend.entity.BusinessRequest;
import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.BusinessRequestRepository;
import com.ukayukay.backend.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRequestRepository businessRequestRepository;

    @Autowired
    private AuditLogService auditLogService; // Inject this to track admin actions

    // 1. Function to Approve or Reject Business Permits
    public User verifySeller(Integer sellerId, User.VerificationStatus status, Integer adminId) {
        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new RuntimeException("Seller account not found with ID: " + sellerId));
            
        seller.setVerificationStatus(status);
        seller.setSellerApproved(status == User.VerificationStatus.APPROVED);

        User updatedSeller = userRepository.save(seller);

        List<BusinessRequest> requests = businessRequestRepository.findByUser(seller);
        for (BusinessRequest request : requests) {
            request.setStatus(status == User.VerificationStatus.APPROVED
                    ? BusinessRequest.RequestStatus.APPROVED
                    : BusinessRequest.RequestStatus.REJECTED);
        }
        businessRequestRepository.saveAll(requests);
        
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

    public User toggleShadowBan(Integer userId, boolean shadowBanned, Integer adminId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User account not found with ID: " + userId));

        user.setIsShadowBanned(shadowBanned);
        User updatedUser = userRepository.save(user);

        String actionType = shadowBanned ? "ACCOUNT_SHADOW_BANNED" : "ACCOUNT_SHADOW_BAN_REMOVED";
        auditLogService.logAction(adminId, actionType, "users");

        return updatedUser;
    }
}