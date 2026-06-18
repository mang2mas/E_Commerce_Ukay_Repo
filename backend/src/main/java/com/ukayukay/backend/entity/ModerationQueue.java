package com.ukayukay.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "moderation_queue")
@Data
public class ModerationQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "queue_id")
    private Integer queueId;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType; // PRODUCT, REVIEW, MESSAGE, etc.

    @Column(name = "content_id", nullable = false)
    private Integer contentId;

    @Column(name = "flagged_reason", columnDefinition = "TEXT")
    private String flaggedReason; // INAPPROPRIATE, OFFENSIVE, SPAM, etc.

    @Column(name = "reported_by")
    private Integer reportedBy; // user_id who reported

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderated_by")
    private User moderatedBy;

    @Column(name = "moderation_notes", columnDefinition = "TEXT")
    private String moderationNotes;

    @Column(name = "flagged_at")
    private LocalDateTime flaggedAt;

    @Column(name = "moderated_at")
    private LocalDateTime moderatedAt;

    @PrePersist
    protected void onCreate() {
        this.flaggedAt = LocalDateTime.now();
    }
}
