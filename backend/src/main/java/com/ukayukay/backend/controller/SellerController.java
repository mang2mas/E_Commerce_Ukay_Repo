package com.ukayukay.backend.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.ukayukay.backend.entity.BusinessRequest;
import com.ukayukay.backend.entity.SellerDocument;
import com.ukayukay.backend.security.JwtUtil;
import com.ukayukay.backend.service.BusinessRequestService;
import com.ukayukay.backend.service.SellerDocumentService;
import lombok.Data;

@RestController
@RequestMapping("/api/seller")
public class SellerController {

    private final BusinessRequestService businessRequestService;
    private final SellerDocumentService sellerDocumentService;
    private final JwtUtil jwtUtil;

    public SellerController(BusinessRequestService businessRequestService,
            SellerDocumentService sellerDocumentService,
            JwtUtil jwtUtil) {
        this.businessRequestService = businessRequestService;
        this.sellerDocumentService = sellerDocumentService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/request")
    public ResponseEntity<BusinessRequest> submitBusinessRequest(@RequestBody BusinessRequestRequest req) {
        BusinessRequest saved = businessRequestService.submitRequest(
            req.getUserId(),
            req.getBusinessName(),
            req.getBusinessDescription(),
            req.getBusinessPermit(),
            req.getNotes(),
            req.getDocumentIds() == null ? Collections.emptyList() : req.getDocumentIds()
        );
        return ResponseEntity.ok(saved);
    }

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SellerDocument> uploadDocument(
            @RequestParam(required = false) Integer userId,
            @RequestParam SellerDocument.DocumentType documentType,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestPart("file") MultipartFile file) throws IOException {
        Integer targetUserId = userId;

        if (authHeader != null && !authHeader.isBlank()) {
            try {
                Integer tokenUserId = extractUserId(authHeader);
                boolean admin = isAdmin(authHeader);
                if (admin && userId != null) {
                    targetUserId = userId;
                } else {
                    // For non-admin authenticated users, always use token identity.
                    targetUserId = tokenUserId;
                }
            } catch (Exception ex) {
                if (targetUserId == null) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                            "Invalid or expired session. Please login again.");
                }
            }
        }

        if (targetUserId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "User ID is required for document upload.");
        }

        return ResponseEntity.ok(sellerDocumentService.uploadDocument(targetUserId, documentType, file));
    }

    @GetMapping("/documents")
    public ResponseEntity<List<SellerDocument>> getDocuments(
            @RequestParam(required = false) Integer userId,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Integer requestId) {
        if (requestId != null) {
            if (!isAdmin(authHeader)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can view request-level documents.");
            }
            return ResponseEntity.ok(sellerDocumentService.getDocumentsForRequest(requestId));
        }

        Integer tokenUserId = extractUserId(authHeader);
        boolean admin = isAdmin(authHeader);
        if (userId == null) {
            userId = tokenUserId;
        }
        if (!tokenUserId.equals(userId) && !admin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can view documents only for your own account.");
        }

        return ResponseEntity.ok(sellerDocumentService.getDocumentsForUser(userId));
    }

    private Integer extractUserId(String authHeader) {
        String token = authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        return jwtUtil.extractUserId(token);
    }

    private boolean isAdmin(String authHeader) {
        String token = authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        String role = jwtUtil.extractRole(token);
        return "ADMIN".equalsIgnoreCase(role);
    }

    @GetMapping("/requests")
    public ResponseEntity<List<BusinessRequest>> getRequestsForUser(@RequestParam Integer userId) {
        return ResponseEntity.ok(businessRequestService.getRequestsForUser(userId));
    }

    @Data
    public static class BusinessRequestRequest {
        private Integer userId;
        private String businessName;
        private String businessDescription;
        private String businessPermit;
        private String notes;
        private List<Integer> documentIds;
    }
}

