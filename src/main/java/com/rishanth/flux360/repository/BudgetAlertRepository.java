package com.rishanth.flux360.repository;

import com.rishanth.flux360.entity.BudgetAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetAlertRepository extends JpaRepository<BudgetAlert, Long> {

    List<BudgetAlert> findByBudgetId(Long budgetId);

    // Batch-fetch alerts for multiple budgets in one query (used by summary)
    List<BudgetAlert> findByBudgetIdIn(List<Long> budgetIds);

    List<BudgetAlert> findByBudgetIdAndIsSeenFalse(Long budgetId);

    long countByBudgetIdAndIsSeenFalse(Long budgetId);

    // Deduplication: does an unseen alert of this type already exist for this category?
    boolean existsByBudgetIdAndBudgetCategoryIdAndAlertTypeAndIsSeenFalse(
            Long budgetId,
            Long budgetCategoryId,
            BudgetAlert.AlertType alertType
    );

    // Ownership-scoped alert fetch for a single budget
    List<BudgetAlert> findByBudgetIdAndBudgetUserId(Long budgetId, Long userId);

    long countByBudgetIdAndBudgetUserIdAndIsSeenFalse(Long budgetId, Long userId);

    // FIX: single query to fetch ALL alerts for a user across all their budgets
    // Replaces the N-per-budget fan-out on the frontend and in loadAllAlerts()
    List<BudgetAlert> findByBudgetUserId(Long userId);

    long countByBudgetUserIdAndIsSeenFalse(Long userId);
}