package com.rishanth.flux360.service;

import com.rishanth.flux360.dto.IncomeDTO;
import com.rishanth.flux360.entity.Income;
import com.rishanth.flux360.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IncomeService {

    Income createIncome(IncomeDTO dto, User user);

    List<Income> findByUser(User user);

    Income findById(Long id, User user);

    Income updateIncome(Long id, IncomeDTO dto, User user);

    void deleteIncome(Long id, User user);

    List<Income> findByUserAndDateRange(
            User user,
            LocalDate start,
            LocalDate end
    );

    BigDecimal getTotalIncome(Long userId);
}