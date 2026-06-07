package com.rishanth.flux360.service;

import com.rishanth.flux360.dto.IncomeDTO;
import com.rishanth.flux360.exception.ResourceNotFoundException;
import com.rishanth.flux360.entity.Income;
import com.rishanth.flux360.entity.IncomeCategory;
import com.rishanth.flux360.entity.User;
import com.rishanth.flux360.repository.IncomeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;

    @Override
    @Transactional
    public Income createIncome(IncomeDTO dto, User user) {

        Income income = Income.builder()
                .source(dto.getSource())
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .date(dto.getDate())
                .category(
                        IncomeCategory.valueOf(
                                dto.getCategory().toUpperCase()
                        )
                )
                .user(user)
                .build();

        return incomeRepository.save(income);
    }

    @Override
    public List<Income> findByUser(User user) {

        return incomeRepository.findByUserOrderByDateDesc(user);
    }

    @Override
    public Income findById(Long id, User user) {

        return incomeRepository.findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Income not found"));
    }

    @Override
    @Transactional
    public Income updateIncome(Long id, IncomeDTO dto, User user) {

        Income income = findById(id, user);

        income.setSource(dto.getSource());
        income.setAmount(dto.getAmount());
        income.setDate(dto.getDate());
        income.setDescription(dto.getDescription());

        income.setCategory(
                IncomeCategory.valueOf(
                        dto.getCategory().toUpperCase()
                )
        );

        return incomeRepository.save(income);
    }

    @Override
    @Transactional
    public void deleteIncome(Long id, User user) {

        Income income = findById(id, user);

        incomeRepository.delete(income);
    }

    @Override
    public List<Income> findByUserAndDateRange(
            User user,
            LocalDate start,
            LocalDate end
    ) {

        return incomeRepository
                .findByUserAndDateBetweenOrderByDateDesc(
                        user,
                        start,
                        end
                );
    }

    @Override
    public BigDecimal getTotalIncome(Long userId) {
        return incomeRepository.getTotalIncome(userId);
    }
}