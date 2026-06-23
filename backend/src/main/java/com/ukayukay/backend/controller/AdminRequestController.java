package com.ukayukay.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ukayukay.backend.entity.BusinessRequest;
import com.ukayukay.backend.security.JwtUtil;
import com.ukayukay.backend.service.BusinessRequestService;

@RestController
@RequestMapping("/api/admin/requests")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRequestController {

    private final BusinessRequestService businessRequestService;
    private final JwtUtil jwtUtil;

    public AdminRequestController(BusinessRequestService businessRequestService, JwtUtil jwtUtil) {
        this.businessRequestService = businessRequestService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<List<BusinessRequest>> listAll() {
        return ResponseEntity.ok(businessRequestService.getAllRequests());
    }

    @PutMapping("/{requestId}/decide")
    public ResponseEntity<BusinessRequest> decideRequest(
            @PathVariable Integer requestId,
            @RequestParam BusinessRequest.RequestStatus status,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        Integer adminId = jwtUtil.extractUserId(token);
        return ResponseEntity.ok(businessRequestService.decideRequest(requestId, status, adminId));
    }
}

