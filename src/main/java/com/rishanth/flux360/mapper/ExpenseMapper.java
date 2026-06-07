package com.rishanth.flux360.mapper;

import com.rishanth.flux360.dto.ExpenseDTO;
import com.rishanth.flux360.entity.Expense;

public class ExpenseMapper {

    public static ExpenseDTO toDTO(Expense expense) {

        return new ExpenseDTO(
                expense.getId(),
                expense.getCategory().name(),
                expense.getAmount(),
                expense.getExpenseDate(),
                expense.getDescription()
        );
    }
}