package com.ukayukay.backend.controller;

import java.util.Map;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ukayukay.backend.dto.AuthRequest;
import com.ukayukay.backend.dto.AuthResponse;
import com.ukayukay.backend.dto.RegisterRequest;
import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.service.AuditLogService;
import com.ukayukay.backend.service.AuthService;
import com.ukayukay.backend.util.AppConstants;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuditLogService auditLogService;

    public AuthController(AuthService authService, AuditLogService auditLogService) {
        this.authService = authService;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request.getEmail(), request.getPassword());

        if ("admin".equalsIgnoreCase(response.getRole())) {
            auditLogService.logAction(
                response.getUserId(),
                AppConstants.AUDIT_ADMIN_LOGIN,
                AppConstants.AUDIT_TABLE_USERS
            );
        }

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/forgot-password
     * Body: { "email": "user@example.com" }
     * Returns the reset code in the response (dev convenience).
     * Replace with email delivery in production.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> body) {
        String code = authService.forgotPassword(body.get("email"));
        return ResponseEntity.ok(Map.of(
            "message", "A 6-digit reset code has been generated (15-minute TTL).",
            "code",    code   // Remove this line and send via email in production
        ));
    }

    /**
     * POST /api/auth/reset-password
     * Body: { "email": "...", "code": "123456", "newPassword": "..." }
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> body) {
        authService.resetPassword(body.get("email"), body.get("code"), body.get("newPassword"));
        return ResponseEntity.ok(Map.of("message", "Password reset successfully. You can now log in."));
    }
}



