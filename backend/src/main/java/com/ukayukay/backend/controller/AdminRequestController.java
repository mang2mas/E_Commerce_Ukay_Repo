package com.ukayukay.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ukayukay.backend.entity.BusinessRequest;
import com.ukayukay.backend.service.BusinessRequestService;

@RestController
@RequestMapping("/api/admin/requests")
@CrossOrigin(origins = "*")
public class AdminRequestController {

    private final BusinessRequestService businessRequestService;

    public AdminRequestController(BusinessRequestService businessRequestService) {
        this.businessRequestService = businessRequestService;
    }

    @RequestMapping("/")
    public ResponseEntity<List<BusinessRequest>> listAll() {
        return ResponseEntity.ok(businessRequestService.getAllRequests());
    }

    @PutMapping("/{requestId}/decide")
    public ResponseEntity<?> decideRequest(
            @PathVariable Integer requestId,
            @RequestParam BusinessRequest.RequestStatus status,
            @RequestParam Integer adminId) {
        try {
            BusinessRequest updated = businessRequestService.decideRequest(requestId, status, adminId);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
