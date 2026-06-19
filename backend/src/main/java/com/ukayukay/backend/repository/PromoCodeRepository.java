package com.ukayukay.backend.repository;

import com.ukayukay.backend.entity.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Integer> {
    
    Optional<PromoCode> findByCode(String code);
    
    @Query("SELECT p FROM PromoCode p WHERE p.isActive = true AND p.validFrom <= CURRENT_TIMESTAMP AND p.validUntil >= CURRENT_TIMESTAMP")
    List<PromoCode> findAllActivePromoCodes();
    
    @Query("SELECT p FROM PromoCode p WHERE p.code = :code AND p.isActive = true AND p.validFrom <= CURRENT_TIMESTAMP AND p.validUntil >= CURRENT_TIMESTAMP")
    Optional<PromoCode> findValidPromoCode(@Param("code") String code);
}
