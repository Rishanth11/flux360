package com.rishanth.flux360.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {

    private Long id;

    private String action;

    private String performedBy;

    private String details;

    private LocalDateTime createdAt;
}