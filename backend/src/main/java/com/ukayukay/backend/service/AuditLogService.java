package com.ukayukay.backend.service;

import com.ukayukay.backend.entity.AuditLog;
import com.ukayukay.backend.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void logAction(Integer adminId, String action, String tableAffected) {
        AuditLog log = new AuditLog();
        log.setAdminId(adminId);
        log.setAction(action);
        log.setTableAffected(tableAffected);
        log.setTimestamp(LocalDateTime.now());
        
        auditLogRepository.save(log);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findByOrderByTimestampDesc();
    }
}