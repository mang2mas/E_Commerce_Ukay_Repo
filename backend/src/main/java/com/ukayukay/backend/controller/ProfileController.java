package com.ukayukay.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.service.ProfileService;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Integer userId) {
        try {
            return ResponseEntity.ok(profileService.getUserProfile(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateProfile(@PathVariable Integer userId, @RequestBody User userUpdates) {
        try {
            return ResponseEntity.ok(profileService.updateUserProfile(userId, userUpdates));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
