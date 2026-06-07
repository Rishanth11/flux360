package com.rishanth.flux360.dto;

import com.rishanth.flux360.entity.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class GoalDTO {

    // ───────────────── REQUEST ─────────────────

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        @NotBlank(message = "Goal name is required")
        private String name;

        private String description;

        @NotNull(message = "Category is required")
        private GoalCategory category;

        @NotNull(message = "Priority is required")
        private GoalPriority priority;

        @NotNull(message = "Target amount is required")
        @Positive(message = "Target amount must be positive")
        private BigDecimal targetAmount;

        @NotNull(message = "Monthly contribution is required")
        @Positive(message = "Monthly contribution must be positive")
        private BigDecimal monthlyContribution;

        @NotNull(message = "Target date is required")
        @Future(message = "Target date must be future")
        private LocalDate targetDate;

        private LocalDate startDate;

        private String linkedAccount;
    }

    // ───────────────── RESPONSE ─────────────────

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {

        private Long id;

        private String name;

        private String description;

        private GoalCategory category;

        private GoalPriority priority;

        private GoalStatus status;

        private BigDecimal targetAmount;

        private BigDecimal savedAmount;

        private BigDecimal remainingAmount;

        private BigDecimal monthlyContribution;

        private double progressPercentage;

        private LocalDate targetDate;

        private LocalDate startDate;

        private LocalDate projectedCompletionDate;

        private String linkedAccount;

        private long monthsRemaining;

        private boolean onTrack;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;
    }

    // ───────────────── CONTRIBUTION REQUEST ─────────────────

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContributionRequest {

        @NotNull(message = "Goal ID required")
        private Long goalId;

        @NotNull(message = "Amount required")
        @Positive(message = "Amount must be positive")
        private BigDecimal amount;

        private LocalDate contributionDate;

        private String note;
    }

    // ───────────────── CONTRIBUTION RESPONSE ─────────────────

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContributionResponse {

        private Long id;

        private Long goalId;

        private String goalName;

        private BigDecimal amount;

        private LocalDate contributionDate;

        private String note;

        private LocalDateTime createdAt;
    }

    // ───────────────── SUMMARY ─────────────────

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Summary {

        private long totalGoals;

        private long activeGoals;

        private long completedGoals;

        private BigDecimal totalTargetAmount;

        private BigDecimal totalSavedAmount;

        private BigDecimal totalRemainingAmount;

        private double overallProgress;
    }
}