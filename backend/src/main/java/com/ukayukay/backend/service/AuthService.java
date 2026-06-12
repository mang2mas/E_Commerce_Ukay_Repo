package com.ukayukay.backend.service;

import org.springframework.stereotype.Service;

import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.UserRepository;
import com.ukayukay.backend.util.AppConstants;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(AppConstants.USER_NOT_FOUND_EMAIL));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException(AppConstants.INVALID_PASSWORD);
        }

        return user;
    }

    public User register(User newUser) {
        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            throw new RuntimeException(AppConstants.EMAIL_EXISTS);
        }

        if (newUser.getRole() == null) {
            newUser.setRole(User.UserRole.buyer);
        }

        return userRepository.save(newUser);
    }
}
