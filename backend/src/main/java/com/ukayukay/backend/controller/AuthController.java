package com.ukayukay.backend.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.service.AuthService;

@RestController
@RequestMapping("/api/auth")

public class AuthController {
    @Autowired
    private AuthService authService;

    @GetMapping("/login") 
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        try {
            // Hand the credentials to the Kitchen logic
            User loggedInUser = authService.login(email, password);
            
            // If successful, return the user details (including their Customer/Admin role)
            return ResponseEntity.ok(loggedInUser);
            
        } catch (RuntimeException e) {
            // If the Service throws an error (wrong email or password), return a 400 error
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
