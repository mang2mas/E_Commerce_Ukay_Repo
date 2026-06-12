package com.ukayukay.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.service.AuditLogService;
import com.ukayukay.backend.service.AuthService;
import com.ukayukay.backend.util.AppConstants;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final AuditLogService auditLogService;

    public AuthController(AuthService authService, AuditLogService auditLogService) {
        this.authService = authService;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User newUser) {
        return ResponseEntity.ok(authService.register(newUser));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        try {
            User loggedInUser = authService.login(email, password);

            if (User.UserRole.admin.equals(loggedInUser.getRole())) {
                auditLogService.logAction(
                    loggedInUser.getUserId(),
                    AppConstants.AUDIT_ADMIN_LOGIN,
                    AppConstants.AUDIT_TABLE_USERS
                );
            }

            return ResponseEntity.ok(loggedInUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}


