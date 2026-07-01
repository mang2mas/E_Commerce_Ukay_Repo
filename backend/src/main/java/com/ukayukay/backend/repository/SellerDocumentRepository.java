package com.ukayukay.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ukayukay.backend.entity.SellerDocument;

public interface SellerDocumentRepository extends JpaRepository<SellerDocument, Integer> {
    List<SellerDocument> findByUser_UserIdOrderByUploadedAtDesc(Integer userId);

    List<SellerDocument> findByBusinessRequest_RequestIdOrderByUploadedAtDesc(Integer requestId);

    List<SellerDocument> findByUser_UserIdAndDocumentIdIn(Integer userId, List<Integer> documentIds);
}
