package com.ukayukay.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeDTO {
    
    private Integer codeId;
    private String code;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal maximumDiscount;
    private BigDecimal minimumPurchase;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Integer usageLimit;
    private Integer timesUsed;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
