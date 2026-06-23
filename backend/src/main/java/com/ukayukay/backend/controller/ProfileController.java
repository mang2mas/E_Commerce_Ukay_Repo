package com.ukayukay.backend.controller;

import org.springframework.http.ResponseEntity;
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
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getProfile(@PathVariable Integer userId) {
        return ResponseEntity.ok(profileService.getUserProfile(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<User> updateProfile(@PathVariable Integer userId, @RequestBody User userUpdates) {
        return ResponseEntity.ok(profileService.updateUserProfile(userId, userUpdates));
    }
}

