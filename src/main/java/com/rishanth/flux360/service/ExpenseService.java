package com.rishanth.flux360.service;

import com.rishanth.flux360.dto.ExpenseDTO;
import com.rishanth.flux360.entity.Expense;
import com.rishanth.flux360.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {

    Expense saveExpense(ExpenseDTO dto, User user);

    List<Expense> getExpensesByUser(User user);

    BigDecimal getTotalExpense(Long userId);

    Expense updateExpense(Long id, ExpenseDTO dto, User user);

    void deleteExpense(Long id, User user);

    Expense findById(Long id, User user);

    List<Expense> getByDateRange(
            User user,
            LocalDate start,
            LocalDate end
    );
}