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

    public BusinessRequestService(BusinessRequestRepository businessRequestRepository,
                                   UserRepository userRepository) {
        this.businessRequestRepository = businessRequestRepository;
        this.userRepository = userRepository;
    }

    public BusinessRequest submitRequest(Integer userId, String businessName, String businessDescription,
                                        String businessPermit, String notes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                    String.format("User not found with ID: %d", userId)));

        if (!User.UserRole.seller.equals(user.getRole())) {
            throw new RuntimeException("Only users with role 'seller' can submit business requests");
        }

        BusinessRequest req = new BusinessRequest();
        req.setUser(user);
        req.setBusinessName(businessName);
        req.setBusinessDescription(businessDescription);
        req.setBusinessPermit(businessPermit);
        req.setNotes(notes);

        return businessRequestRepository.save(req);
    }

    public List<BusinessRequest> getRequestsForUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                    String.format("User not found with ID: %d", userId)));
        return businessRequestRepository.findByUser(user);
    }
}
