package com.rishanth.flux360.dto;

import com.rishanth.flux360.entity.BudgetAlert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Projection returned by GET /api/budgets/alerts — carries the parent budget
 * name alongside each alert so the frontend never has to join client-side.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertDTO {
    private Long id;
    private Long budgetId;
    private String budgetName;
    private Long budgetCategoryId;
    private BudgetAlert.AlertType alertType;
    private String message;
    private LocalDateTime triggeredAt;
    private Boolean isSeen;
}