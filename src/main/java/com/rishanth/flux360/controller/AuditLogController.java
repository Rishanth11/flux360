package com.rishanth.flux360.controller;

import com.rishanth.flux360.dto.AuditLogDTO;
import com.rishanth.flux360.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public List<AuditLogDTO> getLogs() {
        return auditLogService.getAll();
    }
}