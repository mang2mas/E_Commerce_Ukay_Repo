package com.ukayukay.backend.entity;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "audit_logs")
@Data
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;

    @Column(name = "admin_id", nullable = false)
    private Integer adminId;

    @Column(name = "action", nullable = false)
    private String action; 

    @Column(name = "table_affected")
    private String tableAffected; 

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

}
