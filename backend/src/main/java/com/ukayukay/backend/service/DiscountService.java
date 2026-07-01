package com.ukayukay.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.UserRepository;

@Service
public class DiscountService {

    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;
    private static final String STORAGE_DIR = "src/main/resources/static/images/discount-docs";

    private final UserRepository userRepository;

    public DiscountService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User uploadDiscountId(Integer userId, MultipartFile file) throws IOException {
        if (userId == null) {
            throw new RuntimeException("User ID is required.");
        }
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("An ID file is required.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File exceeds 10MB limit.");
        }

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        boolean allowedType = contentType.startsWith("image/") || "application/pdf".equals(contentType);
        if (!allowedType) {
            throw new RuntimeException("Only image and PDF files are allowed.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(String.format("User not found with ID: %d", userId)));

        String storedPath = saveDiscountDocument(file, contentType);

        user.setDiscountIdImageUrl(storedPath);
        user.setDiscountStatus(User.DiscountStatus.PENDING);

        return userRepository.save(user);
    }

    private String saveDiscountDocument(MultipartFile file, String contentType) throws IOException {
        Path uploadDir = Paths.get(STORAGE_DIR);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String extension = resolveExtension(file.getOriginalFilename(), contentType);
        String fileName = UUID.randomUUID() + extension;
        Path target = uploadDir.resolve(fileName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target);
        }

        return "/images/discount-docs/" + fileName;
    }

    private String resolveExtension(String originalName, String contentType) {
        if (originalName != null && originalName.contains(".")) {
            String ext = originalName.substring(originalName.lastIndexOf('.')).toLowerCase(Locale.ROOT);
            if (ext.matches("\\.[a-z0-9]{1,5}")) {
                return ext;
            }
        }

        if ("application/pdf".equals(contentType)) {
            return ".pdf";
        }
        if (contentType.startsWith("image/")) {
            return ".jpg";
        }
        return ".bin";
    }
}
