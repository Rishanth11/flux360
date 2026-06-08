package com.rishanth.flux360.repository;

import com.rishanth.flux360.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {
}