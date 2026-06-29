package com.ukayukay.backend.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ukayukay.backend.dto.AuthResponse;
import com.ukayukay.backend.dto.RegisterRequest;
import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.UserRepository;
import com.ukayukay.backend.security.JwtUtil;
import com.ukayukay.backend.util.AppConstants;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(AppConstants.USER_NOT_FOUND_EMAIL));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException(AppConstants.INVALID_PASSWORD);
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new RuntimeException("Your account has been suspended by administration.");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getUserId());
        return new AuthResponse(token, user.getUserId(), user.getEmail(),
                user.getFullName(), user.getRole().name());
    }

    public User register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException(AppConstants.EMAIL_EXISTS);
        }

        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setFullName(request.getFullName());
        newUser.setContactNo(request.getContactNo());
        newUser.setRole(request.getRole() == null ? User.UserRole.buyer : request.getRole());
        newUser.setDiscountType("NONE");
        newUser.setIsPwdSenior(false);

        String accountType = request.getAccountType();
        if ("Senior".equalsIgnoreCase(accountType)) {
            newUser.setDiscountType("SENIOR");
            newUser.setIsPwdSenior(true);
        } else if ("PWD".equalsIgnoreCase(accountType)) {
            newUser.setDiscountType("PWD");
            newUser.setIsPwdSenior(true);
        }
        newUser.setIsPwdSenior(Boolean.TRUE.equals(request.getIsPwdSenior()));
        newUser.setIdNumber(request.getIdNumber());

        return userRepository.save(newUser);
    }

    /**
     * Generates a 6-digit reset code, stores it on the user (15-min TTL) and returns
     * it so the controller can surface it in dev mode.
     * In production replace the return value with an email-send call.
     */
    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("No account found with this email."));

        String code = String.format("%06d", new Random().nextInt(1_000_000));
        user.setPasswordResetToken(code);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);
        return code;
    }

    /** Validates the reset code then updates the password. */
    public void resetPassword(String email, String code, String newPassword) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("No account found with this email."));

        if (user.getPasswordResetToken() == null || !user.getPasswordResetToken().equals(code)) {
            throw new RuntimeException("Invalid verification code.");
        }
        if (user.getPasswordResetTokenExpiry() == null
                || user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification code has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
    }
}

