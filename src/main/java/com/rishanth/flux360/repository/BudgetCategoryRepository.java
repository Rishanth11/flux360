package com.rishanth.flux360.repository;

import com.rishanth.flux360.entity.BudgetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetCategoryRepository extends JpaRepository<BudgetCategory, Long> {

    List<BudgetCategory> findByBudgetId(Long budgetId);

    // FIX: ownership-safe category lookup — verifies the category belongs to
    // the given budget before returning it, preventing cross-budget injection
    Optional<BudgetCategory> findByIdAndBudgetId(Long id, Long budgetId);
}