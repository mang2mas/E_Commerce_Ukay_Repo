package com.ukayukay.backend.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.UserRepository;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    public User login(String email, String password) {
        
        // 1. Check if the user exists in the database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email!"));

        // 2. Validate the password
        // (In a real-world app, passwords would be encrypted. For learning fundamentals, we are using direct matching)
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password!");
        }

        // 3. If everything is correct, return the user. 
        // The user object will naturally contain their "role" (Customer/Admin) flag!

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password!");
        }

        if (user.getIsActive() != null && !user.getIsActive()) {
            throw new RuntimeException("Your account has been suspended by administration.");
        }
        return user;
    }
    public User register(User newUser) {
        
        // Safety Check: Make sure the email isn't already taken!
        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            throw new RuntimeException("An account with this email already exists!");
        }

        // If the frontend didn't specify a role, default them to a normal Customer
        if (newUser.getRole() == null) {
            newUser.setRole(User.UserRole.buyer); 
        } 

        // Save the new user to the database and return the saved data
        return userRepository.save(newUser);
    }

}
