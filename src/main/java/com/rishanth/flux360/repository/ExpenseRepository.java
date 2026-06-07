package com.rishanth.flux360.repository;

import com.rishanth.flux360.entity.Expense;
import com.rishanth.flux360.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

        @Query("""
            SELECT COALESCE(SUM(e.amount),0)
            FROM Expense e
            WHERE e.user.id = :userId
            """)
        BigDecimal getTotalExpense(Long userId);

        List<Expense> findByUserOrderByExpenseDateDesc(User user);

        Optional<Expense> findByIdAndUser(Long id, User user);

        List<Expense> findByUserAndExpenseDateBetweenOrderByExpenseDateDesc(
                User user,
                LocalDate start,
                LocalDate end
        );

        Optional<Expense> findByBudgetExpenseId(Long budgetExpenseId);
}