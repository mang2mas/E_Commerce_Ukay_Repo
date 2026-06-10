package com.ukayukay.backend.repository;

import com.ukayukay.backend.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {
    List<AuditLog> findByOrderByTimestampDesc();
}