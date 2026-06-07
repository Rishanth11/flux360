package com.rishanth.flux360.repository;

import com.rishanth.flux360.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    @Query("""
        SELECT DISTINCT b
        FROM Budget b
        LEFT JOIN FETCH b.categories
        WHERE b.user.id = :userId
    """)
    List<Budget> findByUserIdWithCategories(Long userId);

    @Query("""
        SELECT DISTINCT b
        FROM Budget b
        LEFT JOIN FETCH b.categories
        WHERE b.id = :budgetId
        AND b.user.id = :userId
    """)
    Optional<Budget> findByIdAndUserId(Long budgetId, Long userId);
}