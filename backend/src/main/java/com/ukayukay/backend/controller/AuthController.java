package com.ukayukay.backend.controller;
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private AuditLogService auditLogService;
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User newUser) {
        return ResponseEntity.ok(authService.register(newUser));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        try {
            // Hand the credentials to the Kitchen logic
            User loggedInUser = authService.login(email, password);
            
            // 🚨 ADMIN CHECK MOVED HERE! 🚨
            // Check if the user is an admin BEFORE we hit the return statement
            if (User.UserRole.admin.equals(loggedInUser.getRole())) {
                auditLogService.logAction(
                    loggedInUser.getUserId(),
                    "ADMIN_LOGIN",
                    "users"
                );
            }

            // If successful, return the user details 
            return ResponseEntity.ok(loggedInUser);
            
        } catch (RuntimeException e) {
            // If the Service throws an error (wrong email or password), return a 400 error
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}


