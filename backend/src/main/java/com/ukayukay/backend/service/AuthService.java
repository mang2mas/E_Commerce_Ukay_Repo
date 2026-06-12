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

<<<<<<< HEAD
        // 3. If everything is correct, return the user. 
        // The user object will naturally contain their "role" (Customer/Admin) flag!

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password!");
        }

        if (user.getIsActive() != null && !user.getIsActive()) {
            throw new RuntimeException("Your account has been suspended by administration.");
        }
=======
>>>>>>> fadce4da5cf5ae44b46070d81b06a183d04fa16a
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
