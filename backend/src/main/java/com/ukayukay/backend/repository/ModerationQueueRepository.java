package com.ukayukay.backend.repository;

import com.ukayukay.backend.entity.ModerationQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface ModerationQueueRepository extends JpaRepository<ModerationQueue, Integer> {

    List<ModerationQueue> findByStatusOrderByFlaggedAtAsc(String status);

    List<ModerationQueue> findByContentTypeAndStatus(String contentType, String status);

    List<ModerationQueue> findByContentTypeAndContentId(String contentType, Integer contentId);
}
