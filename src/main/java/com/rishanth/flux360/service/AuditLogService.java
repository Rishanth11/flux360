package com.rishanth.flux360.service;

import com.rishanth.flux360.dto.AuditLogDTO;
import com.rishanth.flux360.entity.AuditLog;
import com.rishanth.flux360.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository repository;

    public void log(
            String action,
            String performedBy,
            String details
    ) {

        repository.save(
                AuditLog.builder()
                        .action(action)
                        .performedBy(performedBy)
                        .details(details)
                        .build()
        );
    }

    public List<AuditLogDTO> getAll() {

        return repository.findAll()
                .stream()
                .sorted((a, b) ->
                        b.getCreatedAt()
                                .compareTo(a.getCreatedAt()))
                .map(log ->
                        AuditLogDTO.builder()
                                .id(log.getId())
                                .action(log.getAction())
                                .performedBy(log.getPerformedBy())
                                .details(log.getDetails())
                                .createdAt(log.getCreatedAt())
                                .build())
                .toList();
    }
}