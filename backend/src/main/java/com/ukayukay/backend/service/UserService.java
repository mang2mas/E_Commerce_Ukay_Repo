package com.ukayukay.backend.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.ukayukay.backend.entity.BusinessRequest;
import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.BusinessRequestRepository;
import com.ukayukay.backend.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BusinessRequestRepository businessRequestRepository;
    private final AuditLogService auditLogService; // Inject this to track admin actions

    public UserService(UserRepository userRepository,
                       BusinessRequestRepository businessRequestRepository,
                       AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.businessRequestRepository = businessRequestRepository;
        this.auditLogService = auditLogService;
    }

    // 1. Function to Approve or Reject Business Permits
    public User verifySeller(Integer sellerId, User.VerificationStatus status, Integer adminId) {
        Integer safeSellerId = Objects.requireNonNull(sellerId, "sellerId must not be null");
        User seller = userRepository.findById(safeSellerId)
            .orElseThrow(() -> new RuntimeException("Seller account not found with ID: " + sellerId));
            
        seller.setVerificationStatus(status);
        seller.setSellerApproved(status == User.VerificationStatus.APPROVED);
        if (status == User.VerificationStatus.APPROVED) {
            seller.setRole(User.UserRole.seller);
        } else if (seller.getRole() != User.UserRole.admin) {
            seller.setRole(User.UserRole.buyer);
        }

        User updatedSeller = userRepository.save(seller);

        List<BusinessRequest> requests = businessRequestRepository.findByUser(seller);
        for (BusinessRequest request : requests) {
            request.setStatus(status == User.VerificationStatus.APPROVED
                    ? BusinessRequest.RequestStatus.APPROVED
                    : BusinessRequest.RequestStatus.REJECTED);
        }
        if (!requests.isEmpty()) {
            Iterable<BusinessRequest> requestsToSave = Objects.requireNonNull(requests, "requests must not be null");
            businessRequestRepository.saveAll(requestsToSave);
        }
        
        auditLogService.logAction(adminId, "SELLER_VERIFICATION_" + status, "users");
        
        return updatedSeller;
    }

    public User toggleAccountStatus(Integer userId, boolean activate, Integer adminId) {
        Integer safeUserId = Objects.requireNonNull(userId, "userId must not be null");
        User user = userRepository.findById(safeUserId)
            .orElseThrow(() -> new RuntimeException("User account not found with ID: " + userId));
            
        user.setIsActive(activate);
        User updatedUser = userRepository.save(user);
        
        String actionType = activate ? "ACCOUNT_REACTIVATED" : "ACCOUNT_SUSPENDED";
        auditLogService.logAction(adminId, actionType, "users");
        
        return updatedUser;
    }

    public User toggleShadowBan(Integer userId, boolean shadowBanned, Integer adminId) {
        Integer safeUserId = Objects.requireNonNull(userId, "userId must not be null");
        User user = userRepository.findById(safeUserId)
            .orElseThrow(() -> new RuntimeException("User account not found with ID: " + userId));

        user.setIsShadowBanned(shadowBanned);
        User updatedUser = userRepository.save(user);

        String actionType = shadowBanned ? "ACCOUNT_SHADOW_BANNED" : "ACCOUNT_SHADOW_BAN_REMOVED";
        auditLogService.logAction(adminId, actionType, "users");

        return updatedUser;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(String role) {
        try {
            User.UserRole userRole = User.UserRole.valueOf(role.toLowerCase());
            return userRepository.findByRole(userRole);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + role + ". Valid values: buyer, seller, admin");
        }
    }
}