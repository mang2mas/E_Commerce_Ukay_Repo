package com.ukayukay.backend.service;

import org.springframework.stereotype.Service;

import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.UserRepository;
import com.ukayukay.backend.util.AppConstants;

@Service
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserProfile(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(String.format(AppConstants.USER_NOT_FOUND, userId)));
    }

    public User updateUserProfile(Integer userId, User userUpdates) {
        User user = getUserProfile(userId);

        if (userUpdates.getFullName() != null) {
            user.setFullName(userUpdates.getFullName());
        }
        if (userUpdates.getContactNo() != null) {
            user.setContactNo(userUpdates.getContactNo());
        }
        if (userUpdates.getEmail() != null) {
            user.setEmail(userUpdates.getEmail());
        }
        if (userUpdates.getPassword() != null) {
            user.setPassword(userUpdates.getPassword());
        }
        if (userUpdates.getBusinessName() != null) {
            user.setBusinessName(userUpdates.getBusinessName());
        }
        if (userUpdates.getBusinessDescription() != null) {
            user.setBusinessDescription(userUpdates.getBusinessDescription());
        }

        return userRepository.save(user);
    }
}
