package com.ukayukay.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_fees")
@Data
public class SystemFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fee_id")
    private Integer feeId;

    @Column(name = "fee_type", nullable = false, length = 50)
    private String feeType; // PLATFORM_COMMISSION, TAX, SHIPPING_SUBSIDY, etc.

    @Column(name = "fee_percentage", precision = 5, scale = 2)
    private BigDecimal feePercentage;

    @Column(name = "fee_flat_amount", precision = 10, scale = 2)
    private BigDecimal feeFlatAmount;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_until")
    private LocalDateTime effectiveUntil;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
