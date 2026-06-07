package com.rishanth.flux360.dto;

import com.rishanth.flux360.entity.Budget;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetRequestDTO {

    // userId is resolved server-side from the JWT — never accepted from the client.

    @NotBlank(message = "Budget name is required")
    private String name;

    @NotNull(message = "Budget type is required")
    private Budget.BudgetType budgetType;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    private BigDecimal totalAmount;

    private Long goalId;

    private List<BudgetCategoryDTO> categories;
}