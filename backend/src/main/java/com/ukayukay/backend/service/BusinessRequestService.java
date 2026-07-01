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
    private final SellerDocumentService sellerDocumentService;
    private final AuditLogService auditLogService;


    public BusinessRequestService(BusinessRequestRepository businessRequestRepository,
                                   UserRepository userRepository,
                                   SellerDocumentService sellerDocumentService,
                                   AuditLogService auditLogService) {
        this.businessRequestRepository = businessRequestRepository;
        this.userRepository = userRepository;
        this.sellerDocumentService = sellerDocumentService;
        this.auditLogService = auditLogService;
    }


    public BusinessRequest submitRequest(Integer userId, String businessName, String businessDescription,
                                        String businessPermit, String notes, List<Integer> documentIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                    String.format("User not found with ID: %d", userId)));

        user.setBusinessName(businessName);
        user.setBusinessDescription(businessDescription);
        user.setVerificationStatus(User.VerificationStatus.PENDING);
        user.setSellerApproved(false);
        userRepository.save(user);

        BusinessRequest req = new BusinessRequest();
        req.setUser(user);
        req.setBusinessName(businessName);
        req.setBusinessDescription(businessDescription);
        req.setBusinessPermit(businessPermit);
        req.setNotes(notes);

        BusinessRequest saved = businessRequestRepository.save(req);
        sellerDocumentService.linkDocumentsToRequest(userId, saved, documentIds);

        return saved;
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
            user.setVerificationStatus(newStatus == BusinessRequest.RequestStatus.APPROVED
                    ? User.VerificationStatus.APPROVED
                    : User.VerificationStatus.REJECTED);
            user.setSellerApproved(newStatus == BusinessRequest.RequestStatus.APPROVED);
            if (newStatus == BusinessRequest.RequestStatus.APPROVED
                    && !User.UserRole.seller.equals(user.getRole())) {
                user.setRole(User.UserRole.seller);
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





