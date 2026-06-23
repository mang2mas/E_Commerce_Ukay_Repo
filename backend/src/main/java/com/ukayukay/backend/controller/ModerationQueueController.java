package com.ukayukay.backend.controller;

import com.ukayukay.backend.entity.ModerationQueue;
import com.ukayukay.backend.entity.Review;
import com.ukayukay.backend.security.JwtUtil;
import com.ukayukay.backend.service.ModerationQueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/moderation")
@PreAuthorize("hasRole('ADMIN')")
public class ModerationQueueController {

    private final ModerationQueueService moderationQueueService;
    private final JwtUtil jwtUtil;

    public ModerationQueueController(ModerationQueueService moderationQueueService, JwtUtil jwtUtil) {
        this.moderationQueueService = moderationQueueService;
        this.jwtUtil = jwtUtil;
    }

    /** GET /api/moderation/queue - all pending items */
    @GetMapping("/queue")
    public ResponseEntity<List<ModerationQueue>> getPendingQueue() {
        return ResponseEntity.ok(moderationQueueService.getPendingQueue());
    }

    /** GET /api/moderation/queue?type=REVIEW - pending items by content type */
    @GetMapping("/queue/type/{contentType}")
    public ResponseEntity<List<ModerationQueue>> getPendingByType(@PathVariable String contentType) {
        return ResponseEntity.ok(moderationQueueService.getPendingByType(contentType));
    }

    /** GET /api/moderation/queue/all - all queue items */
    @GetMapping("/queue/all")
    public ResponseEntity<List<ModerationQueue>> getAllQueueItems() {
        return ResponseEntity.ok(moderationQueueService.getAllQueueItems());
    }

    /** POST /api/moderation/flag - flag content for moderation */
    @PostMapping("/flag")
    public ResponseEntity<ModerationQueue> flagContent(
            @RequestParam String contentType,
            @RequestParam Integer contentId,
            @RequestParam String reason,
            @RequestHeader("Authorization") String authHeader) {
        Integer reportedBy = extractUserId(authHeader);
        return ResponseEntity.ok(moderationQueueService.flagContent(contentType, contentId, reason, reportedBy));
    }

    /** PUT /api/moderation/queue/{queueId}/decide - moderate a queue item */
    @PutMapping("/queue/{queueId}/decide")
    public ResponseEntity<ModerationQueue> moderateQueueItem(
            @PathVariable Integer queueId,
            @RequestParam String decision,
            @RequestParam(required = false) String notes,
            @RequestHeader("Authorization") String authHeader) {
        Integer adminId = extractUserId(authHeader);
        return ResponseEntity.ok(moderationQueueService.moderate(queueId, decision, notes, adminId));
    }

    /** PUT /api/moderation/reviews/{reviewId}/decide - directly moderate a review */
    @PutMapping("/reviews/{reviewId}/decide")
    public ResponseEntity<Review> moderateReview(
            @PathVariable Integer reviewId,
            @RequestParam String decision,
            @RequestHeader("Authorization") String authHeader) {
        Integer adminId = extractUserId(authHeader);
        return ResponseEntity.ok(moderationQueueService.moderateReview(reviewId, decision, adminId));
    }

    private Integer extractUserId(String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        return jwtUtil.extractUserId(token);
    }
}
