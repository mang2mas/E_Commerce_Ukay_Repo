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
@Table(name = "promo_codes")
@Data
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "code_id")
    private Integer codeId;

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "discount_type", nullable = false, length = 20)
    private String discountType; // PERCENTAGE, FIXED_AMOUNT

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "maximum_discount", precision = 10, scale = 2)
    private BigDecimal maximumDiscount;

    @Column(name = "minimum_purchase", precision = 10, scale = 2)
    private BigDecimal minimumPurchase;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "times_used")
    private Integer timesUsed = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
