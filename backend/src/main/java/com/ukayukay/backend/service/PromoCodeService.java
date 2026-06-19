package com.ukayukay.backend.service;

import com.ukayukay.backend.dto.PromoCodeDTO;
import com.ukayukay.backend.entity.PromoCode;
import com.ukayukay.backend.repository.PromoCodeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;

    public PromoCodeService(PromoCodeRepository promoCodeRepository) {
        this.promoCodeRepository = promoCodeRepository;
    }

    /**
     * Validate and apply promo code to a cart total
     * @param code the promo code
     * @param cartTotal the total amount in the cart
     * @return discount amount, or null if invalid
     */
    public BigDecimal validateAndCalculateDiscount(String code, BigDecimal cartTotal) {
        Optional<PromoCode> promoCodeOpt = promoCodeRepository.findValidPromoCode(code);
        
        if (promoCodeOpt.isEmpty()) {
            return null; // Code not found or expired
        }
        
        PromoCode promoCode = promoCodeOpt.get();
        
        // Check if usage limit exceeded
        if (promoCode.getUsageLimit() != null && 
            promoCode.getTimesUsed() != null && 
            promoCode.getTimesUsed() >= promoCode.getUsageLimit()) {
            return null; // Usage limit exceeded
        }
        
        // Check minimum purchase requirement
        if (promoCode.getMinimumPurchase() != null && 
            cartTotal.compareTo(promoCode.getMinimumPurchase()) < 0) {
            return null; // Cart total below minimum
        }
        
        BigDecimal discount = BigDecimal.ZERO;
        
        if ("PERCENTAGE".equalsIgnoreCase(promoCode.getDiscountType())) {
            // Calculate percentage discount
            discount = cartTotal.multiply(promoCode.getDiscountValue())
                    .divide(new BigDecimal("100"));
            
            // Apply maximum discount cap if set
            if (promoCode.getMaximumDiscount() != null && 
                discount.compareTo(promoCode.getMaximumDiscount()) > 0) {
                discount = promoCode.getMaximumDiscount();
            }
        } else if ("FIXED_AMOUNT".equalsIgnoreCase(promoCode.getDiscountType())) {
            // Use fixed discount
            discount = promoCode.getDiscountValue();
            
            // Ensure discount doesn't exceed cart total
            if (discount.compareTo(cartTotal) > 0) {
                discount = cartTotal;
            }
        }
        
        return discount;
    }

    /**
     * Apply promo code (increment usage count)
     */
    public void applyPromoCode(String code) {
        Optional<PromoCode> promoCodeOpt = promoCodeRepository.findByCode(code);
        if (promoCodeOpt.isPresent()) {
            PromoCode promoCode = promoCodeOpt.get();
            promoCode.setTimesUsed(promoCode.getTimesUsed() + 1);
            promoCodeRepository.save(promoCode);
        }
    }

    /**
     * Create a new promo code (admin only)
     */
    public PromoCodeDTO createPromoCode(PromoCodeDTO dto) {
        PromoCode promoCode = new PromoCode();
        promoCode.setCode(dto.getCode().toUpperCase());
        promoCode.setDiscountType(dto.getDiscountType());
        promoCode.setDiscountValue(dto.getDiscountValue());
        promoCode.setMaximumDiscount(dto.getMaximumDiscount());
        promoCode.setMinimumPurchase(dto.getMinimumPurchase());
        promoCode.setValidFrom(dto.getValidFrom());
        promoCode.setValidUntil(dto.getValidUntil());
        promoCode.setUsageLimit(dto.getUsageLimit());
        promoCode.setIsActive(true);
        promoCode.setTimesUsed(0);
        
        PromoCode saved = promoCodeRepository.save(promoCode);
        return toDto(saved);
    }

    /**
     * Get all active promo codes
     */
    public List<PromoCodeDTO> getAllActivePromoCodes() {
        return promoCodeRepository.findAllActivePromoCodes()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get promo code by id
     */
    public PromoCodeDTO getPromoCodeById(Integer id) {
        Optional<PromoCode> opt = promoCodeRepository.findById(id);
        return opt.map(this::toDto).orElse(null);
    }

    /**
     * Deactivate a promo code
     */
    public PromoCodeDTO deactivatePromoCode(Integer id) {
        Optional<PromoCode> opt = promoCodeRepository.findById(id);
        if (opt.isPresent()) {
            PromoCode promoCode = opt.get();
            promoCode.setIsActive(false);
            PromoCode updated = promoCodeRepository.save(promoCode);
            return toDto(updated);
        }
        return null;
    }

    private PromoCodeDTO toDto(PromoCode promoCode) {
        PromoCodeDTO dto = new PromoCodeDTO();
        dto.setCodeId(promoCode.getCodeId());
        dto.setCode(promoCode.getCode());
        dto.setDiscountType(promoCode.getDiscountType());
        dto.setDiscountValue(promoCode.getDiscountValue());
        dto.setMaximumDiscount(promoCode.getMaximumDiscount());
        dto.setMinimumPurchase(promoCode.getMinimumPurchase());
        dto.setValidFrom(promoCode.getValidFrom());
        dto.setValidUntil(promoCode.getValidUntil());
        dto.setUsageLimit(promoCode.getUsageLimit());
        dto.setTimesUsed(promoCode.getTimesUsed());
        dto.setIsActive(promoCode.getIsActive());
        dto.setCreatedAt(promoCode.getCreatedAt());
        return dto;
    }
}
