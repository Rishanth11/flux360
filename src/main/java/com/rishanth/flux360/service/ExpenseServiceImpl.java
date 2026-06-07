package com.rishanth.flux360.service;

import com.rishanth.flux360.dto.ExpenseDTO;
import com.rishanth.flux360.exception.ResourceNotFoundException;
import com.rishanth.flux360.entity.Expense;
import com.rishanth.flux360.entity.ExpenseCategory;
import com.rishanth.flux360.entity.User;
import com.rishanth.flux360.repository.ExpenseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository repo;

    @Override
    @Transactional
    public Expense saveExpense(ExpenseDTO dto, User user) {

        Expense expense = Expense.builder()
                .category(
                        ExpenseCategory.valueOf(
                                dto.getCategory().toUpperCase()
                        )
                )
                .amount(dto.getAmount())
                .expenseDate(dto.getExpenseDate())
                .description(dto.getDescription())
                .user(user)
                .build();

        return repo.save(expense);
    }

    @Override
    public List<Expense> getExpensesByUser(User user) {

        return repo.findByUserOrderByExpenseDateDesc(user);
    }

    @Override
    public BigDecimal getTotalExpense(Long userId) {

        return repo.getTotalExpense(userId);
    }

    @Override
    public Expense findById(Long id, User user) {

        return repo.findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Expense not found"
                        ));
    }

    @Override
    @Transactional
    public Expense updateExpense(
            Long id,
            ExpenseDTO dto,
            User user
    ) {

        Expense existing = findById(id, user);

        existing.setCategory(
                ExpenseCategory.valueOf(
                        dto.getCategory().toUpperCase()
                )
        );

        existing.setAmount(dto.getAmount());
        existing.setExpenseDate(dto.getExpenseDate());
        existing.setDescription(dto.getDescription());

        return repo.save(existing);
    }

    @Override
    @Transactional
    public void deleteExpense(Long id, User user) {

        Expense expense = findById(id, user);

        repo.delete(expense);
    }

    @Override
    public List<Expense> getByDateRange(
            User user,
            LocalDate start,
            LocalDate end
    ) {

        return repo
                .findByUserAndExpenseDateBetweenOrderByExpenseDateDesc(
                        user,
                        start,
                        end
                );
    }
}