package com.rishanth.flux360.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetSummaryDTO {
    private Long budgetId;
    private String budgetName;
    private BigDecimal totalBudget;
    // Total spent as reported by raw expense records (source of truth)
    private BigDecimal totalSpent;
    // Convenience breakdown: expenses that were not linked to any category
    private BigDecimal uncategorizedSpent;
    private BigDecimal totalRemaining;
    private Double spentPercentage;
    private List<CategorySummaryDTO> categorySummaries;
    private long unreadAlerts;
}