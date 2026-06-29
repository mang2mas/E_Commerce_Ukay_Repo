package com.ukayukay.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ukayukay.backend.entity.BusinessRequest;
import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.BusinessRequestRepository;
import com.ukayukay.backend.repository.UserRepository;

@Service
public class BusinessRequestService {

    private final BusinessRequestRepository businessRequestRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public BusinessRequestService(BusinessRequestRepository businessRequestRepository,
                                   UserRepository userRepository,
                                   AuditLogService auditLogService) {
        this.businessRequestRepository = businessRequestRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public BusinessRequest submitRequest(Integer userId, String businessName, String businessDescription,
                                        String businessPermit, String notes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                    String.format("User not found with ID: %d", userId)));

        if (user.getRole() == User.UserRole.admin) {
            throw new RuntimeException("Administrator accounts cannot submit seller requests.");
        }

        if (Boolean.TRUE.equals(user.getSellerApproved()) && User.UserRole.seller.equals(user.getRole())) {
            throw new RuntimeException("Your seller account is already approved.");
        }

        String normalizedBusinessName = businessName == null ? "" : businessName.trim();
        String normalizedBusinessDescription = businessDescription == null ? "" : businessDescription.trim();
        String normalizedBusinessPermit = businessPermit == null ? "" : businessPermit.trim();
        String normalizedNotes = notes == null ? "" : notes.trim();

        if (normalizedBusinessName.isBlank()) {
            throw new RuntimeException("Business name is required.");
        }
        if (normalizedBusinessDescription.isBlank()) {
            throw new RuntimeException("Business description is required.");
        }

        List<BusinessRequest> existingRequests = businessRequestRepository.findByUser(user);
        boolean hasPendingRequest = existingRequests.stream()
            .anyMatch(request -> request.getStatus() == BusinessRequest.RequestStatus.PENDING);
        if (hasPendingRequest) {
            throw new RuntimeException("You already have a pending seller request.");
        }

        BusinessRequest req = new BusinessRequest();
        req.setUser(user);
        req.setBusinessName(normalizedBusinessName);
        req.setBusinessDescription(normalizedBusinessDescription);
        req.setBusinessPermit(normalizedBusinessPermit);
        req.setNotes(normalizedNotes);

        user.setBusinessName(normalizedBusinessName);
        user.setBusinessDescription(normalizedBusinessDescription);
        user.setBusinessPermitUrl(normalizedBusinessPermit.isBlank() ? null : normalizedBusinessPermit);
        user.setVerificationStatus(User.VerificationStatus.PENDING);
        user.setSellerApproved(false);
        userRepository.save(user);

        return businessRequestRepository.save(req);
    }

    public List<BusinessRequest> getRequestsForUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                    String.format("User not found with ID: %d", userId)));
        return businessRequestRepository.findByUser(user);
    }

    public List<BusinessRequest> getAllRequests() {
        return businessRequestRepository.findAll();
    }

    public BusinessRequest decideRequest(Integer requestId, BusinessRequest.RequestStatus newStatus, Integer adminId) {
        BusinessRequest req = businessRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException(String.format("BusinessRequest not found: %d", requestId)));

        req.setStatus(newStatus);
        BusinessRequest saved = businessRequestRepository.save(req);

        // Update linked user verification state
        User user = req.getUser();
        if (user != null) {
            if (newStatus == BusinessRequest.RequestStatus.APPROVED) {
                user.setVerificationStatus(User.VerificationStatus.APPROVED);
                user.setSellerApproved(true);
                user.setRole(User.UserRole.seller);
            } else if (newStatus == BusinessRequest.RequestStatus.REJECTED) {
                user.setVerificationStatus(User.VerificationStatus.REJECTED);
                user.setSellerApproved(false);
                if (user.getRole() != User.UserRole.admin) {
                    user.setRole(User.UserRole.buyer);
                }
            }
            userRepository.save(user);
        }

        // Audit
        try {
            auditLogService.logAction(adminId, "BUSINESS_REQUEST_" + newStatus, "business_requests");
        } catch (Exception e) {
            System.err.println("Failed to log audit for business request: " + e.getMessage());
        }

        return saved;
    }
}
