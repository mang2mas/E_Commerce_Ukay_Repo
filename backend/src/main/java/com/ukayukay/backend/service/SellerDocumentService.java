package com.ukayukay.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ukayukay.backend.entity.BusinessRequest;
import com.ukayukay.backend.entity.SellerDocument;
import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.SellerDocumentRepository;
import com.ukayukay.backend.repository.UserRepository;

@Service
public class SellerDocumentService {

    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;
    private static final String STORAGE_DIR = "src/main/resources/static/images/seller-docs";

    private final SellerDocumentRepository sellerDocumentRepository;
    private final UserRepository userRepository;

    public SellerDocumentService(SellerDocumentRepository sellerDocumentRepository, UserRepository userRepository) {
        this.sellerDocumentRepository = sellerDocumentRepository;
        this.userRepository = userRepository;
    }

    public SellerDocument uploadDocument(Integer userId, SellerDocument.DocumentType documentType, MultipartFile file)
            throws IOException {
        if (userId == null) {
            throw new RuntimeException("User ID is required.");
        }
        if (documentType == null) {
            throw new RuntimeException("Document type is required.");
        }
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("A document file is required.");
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

        String storedPath = saveSellerDocument(file, contentType);

        SellerDocument document = new SellerDocument();
        document.setUser(user);
        document.setDocumentType(documentType);
        document.setFilePath(storedPath);
        document.setOriginalFileName(file.getOriginalFilename());
        document.setContentType(contentType);
        document.setFileSizeBytes(file.getSize());

        SellerDocument saved = sellerDocumentRepository.save(document);

        if (documentType == SellerDocument.DocumentType.PROFILE_IMAGE) {
            user.setBusinessPermitUrl(storedPath);
            userRepository.save(user);
        }

        return saved;
    }

    public List<SellerDocument> getDocumentsForUser(Integer userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return sellerDocumentRepository.findByUser_UserIdOrderByUploadedAtDesc(userId);
    }

    public List<SellerDocument> getDocumentsForRequest(Integer requestId) {
        if (requestId == null) {
            return Collections.emptyList();
        }
        return sellerDocumentRepository.findByBusinessRequest_RequestIdOrderByUploadedAtDesc(requestId);
    }

    public void linkDocumentsToRequest(Integer userId, BusinessRequest businessRequest, List<Integer> documentIds) {
        if (businessRequest == null || userId == null || documentIds == null || documentIds.isEmpty()) {
            return;
        }

        List<SellerDocument> documents = sellerDocumentRepository.findByUser_UserIdAndDocumentIdIn(userId, documentIds);
        for (SellerDocument document : documents) {
            document.setBusinessRequest(businessRequest);
            document.setStatus(SellerDocument.DocumentStatus.PENDING);
        }
        sellerDocumentRepository.saveAll(documents);
    }

    private String saveSellerDocument(MultipartFile file, String contentType) throws IOException {
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

        return "/images/seller-docs/" + fileName;
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
