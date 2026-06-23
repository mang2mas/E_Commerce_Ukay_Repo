package com.ukayukay.backend.service;

import com.ukayukay.backend.entity.ModerationQueue;
import com.ukayukay.backend.entity.Review;
import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.ModerationQueueRepository;
import com.ukayukay.backend.repository.ReviewRepository;
import com.ukayukay.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ModerationQueueService {

    private final ModerationQueueRepository queueRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public ModerationQueueService(ModerationQueueRepository queueRepository,
                                   ReviewRepository reviewRepository,
                                   UserRepository userRepository,
                                   AuditLogService auditLogService) {
        this.queueRepository = queueRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    /** Flag any content (PRODUCT, REVIEW, MESSAGE) for moderation */
    public ModerationQueue flagContent(String contentType, Integer contentId,
                                        String flaggedReason, Integer reportedBy) {
        ModerationQueue entry = new ModerationQueue();
        entry.setContentType(contentType.toUpperCase());
        entry.setContentId(contentId);
        entry.setFlaggedReason(flaggedReason);
        entry.setReportedBy(reportedBy);
        entry.setStatus("PENDING");
        return queueRepository.save(entry);
    }

    /** List all pending queue items (admin dashboard) */
    public List<ModerationQueue> getPendingQueue() {
        return queueRepository.findByStatusOrderByFlaggedAtAsc("PENDING");
    }

    /** List pending items by content type */
    public List<ModerationQueue> getPendingByType(String contentType) {
        return queueRepository.findByContentTypeAndStatus(contentType.toUpperCase(), "PENDING");
    }

    /** Moderate a queue item: APPROVED or REJECTED */
    @Transactional
    public ModerationQueue moderate(Integer queueId, String decision,
                                     String notes, Integer adminId) {
        ModerationQueue entry = queueRepository.findById(queueId)
                .orElseThrow(() -> new RuntimeException("Moderation queue item not found: " + queueId));

        if (!"PENDING".equals(entry.getStatus())) {
            throw new IllegalStateException("Queue item has already been moderated.");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin user not found: " + adminId));

        String upperDecision = decision.toUpperCase();
        if (!"APPROVED".equals(upperDecision) && !"REJECTED".equals(upperDecision)) {
            throw new RuntimeException("Decision must be APPROVED or REJECTED.");
        }

        entry.setStatus(upperDecision);
        entry.setModeratedBy(admin);
        entry.setModerationNotes(notes);
        entry.setModeratedAt(LocalDateTime.now());
        ModerationQueue saved = queueRepository.save(entry);

        // If the content is a REVIEW, apply the decision to the review entity
        if ("REVIEW".equals(entry.getContentType())) {
            applyReviewDecision(entry.getContentId(), upperDecision);
        }

        auditLogService.logAction(adminId, "MODERATION_" + upperDecision + "_" + entry.getContentType(),
                "moderation_queue");

        return saved;
    }

    /** Directly moderate a review (approve/reject) */
    @Transactional
    public Review moderateReview(Integer reviewId, String decision, Integer adminId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));

        String upperDecision = decision.toUpperCase();
        if (!"APPROVED".equals(upperDecision) && !"REJECTED".equals(upperDecision)) {
            throw new RuntimeException("Decision must be APPROVED or REJECTED.");
        }

        review.setStatus(upperDecision);
        Review saved = reviewRepository.save(review);

        auditLogService.logAction(adminId, "REVIEW_" + upperDecision, "reviews");
        return saved;
    }

    private void applyReviewDecision(Integer reviewId, String decision) {
        reviewRepository.findById(reviewId).ifPresent(review -> {
            review.setStatus(decision);
            reviewRepository.save(review);
        });
    }

    public List<ModerationQueue> getAllQueueItems() {
        return queueRepository.findAll();
    }
}
