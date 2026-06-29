package com.ukayukay.backend.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ukayukay.backend.entity.AuditLog;
import com.ukayukay.backend.repository.AuditLogRepository;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(Integer adminId, String action, String tableAffected) {
        try {
            AuditLog log = new AuditLog();
            log.setAdminId(adminId);
            log.setAction(action);
            log.setTableAffected(tableAffected);
            log.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(log);
        } catch (Exception e) {
            // Audit logging should not prevent admin actions from succeeding
            System.err.println("Failed to persist audit log: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<AuditLog> getAllLogs() {
        try {
            return auditLogRepository.findByOrderByTimestampDesc();
        } catch (Exception e) {
            System.err.println("Failed to load audit logs: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}