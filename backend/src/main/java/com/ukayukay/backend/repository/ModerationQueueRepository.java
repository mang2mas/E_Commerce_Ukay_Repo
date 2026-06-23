package com.ukayukay.backend.repository;

import com.ukayukay.backend.entity.ModerationQueue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModerationQueueRepository extends JpaRepository<ModerationQueue, Integer> {

    List<ModerationQueue> findByStatusOrderByFlaggedAtAsc(String status);

    List<ModerationQueue> findByContentTypeAndStatus(String contentType, String status);

    List<ModerationQueue> findByContentTypeAndContentId(String contentType, Integer contentId);
}
