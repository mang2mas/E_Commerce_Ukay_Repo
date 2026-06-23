package com.ukayukay.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ukayukay.backend.entity.PromoCode;

public interface PromoCodeRepository extends JpaRepository<PromoCode, Integer> {
    
    Optional<PromoCode> findByCode(String code);
    
    @Query("SELECT p FROM PromoCode p WHERE p.isActive = true AND p.validFrom <= CURRENT_TIMESTAMP AND p.validUntil >= CURRENT_TIMESTAMP")
    List<PromoCode> findAllActivePromoCodes();
    
    @Query("SELECT p FROM PromoCode p WHERE p.code = :code AND p.isActive = true AND p.validFrom <= CURRENT_TIMESTAMP AND p.validUntil >= CURRENT_TIMESTAMP")
    Optional<PromoCode> findValidPromoCode(@Param("code") String code);
}
