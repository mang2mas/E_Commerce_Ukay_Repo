package com.ukayukay.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ukayukay.backend.entity.BusinessRequest;
import com.ukayukay.backend.service.BusinessRequestService;
import lombok.Data;

@RestController
@RequestMapping("/api/seller")
@CrossOrigin(origins = "*")
public class SellerController {

    private final BusinessRequestService businessRequestService;

    public SellerController(BusinessRequestService businessRequestService) {
        this.businessRequestService = businessRequestService;
    }

    @PostMapping("/request")
    public ResponseEntity<?> submitBusinessRequest(@RequestBody BusinessRequestRequest req) {
        try {
            BusinessRequest saved = businessRequestService.submitRequest(
                req.getUserId(),
                req.getBusinessName(),
                req.getBusinessDescription(),
                req.getBusinessPermit(),
                req.getNotes()
            );
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/requests")
    public ResponseEntity<?> getRequestsForUser(@RequestParam Integer userId) {
        try {
            List<BusinessRequest> list = businessRequestService.getRequestsForUser(userId);
            return ResponseEntity.ok(list);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Data
    public static class BusinessRequestRequest {
        private Integer userId;
        private String businessName;
        private String businessDescription;
        private String businessPermit;
        private String notes;
    }
}
