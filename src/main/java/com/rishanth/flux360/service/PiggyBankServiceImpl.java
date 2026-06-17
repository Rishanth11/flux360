package com.rishanth.flux360.service;

import com.rishanth.flux360.dto.*;
import com.rishanth.flux360.entity.*;
import com.rishanth.flux360.repository.PiggyBankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PiggyBankServiceImpl implements PiggyBankService {

    private final PiggyBankRepository repository;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;

    @Override
    @Transactional
    public PiggyBankTransactionDTO deposit(
            BigDecimal amount,
            String note,
            User user
    ) {

        BigDecimal remainingSalary =
                calculateRemainingSalary(user);

        if (amount.compareTo(remainingSalary) > 0) {
            throw new IllegalArgumentException(
                    "Insufficient remaining salary"
            );
        }

        PiggyBankTransaction transaction =
                PiggyBankTransaction.builder()
                        .user(user)
                        .amount(amount)
                        .type(PiggyBankTransactionType.DEPOSIT)
                        .note(note)
                        .transactionDate(LocalDate.now())
                        .build();

        repository.save(transaction);

        return mapToDTO(transaction);
    }

    @Override
    @Transactional
    public PiggyBankTransactionDTO withdraw(
            BigDecimal amount,
            String note,
            User user
    ) {

        BigDecimal balance =
                getCurrentBalance(user.getId());

        if (amount.compareTo(balance) > 0) {
            throw new IllegalArgumentException(
                    "Insufficient piggy bank balance"
            );
        }

        PiggyBankTransaction transaction =
                PiggyBankTransaction.builder()
                        .user(user)
                        .amount(amount)
                        .type(PiggyBankTransactionType.WITHDRAW)
                        .note(note)
                        .transactionDate(LocalDate.now())
                        .build();

        repository.save(transaction);

        return mapToDTO(transaction);
    }

    @Override
    public PiggyBankSummaryDTO getSummary(User user) {

        BigDecimal deposits =
                repository.totalDeposits(user.getId());

        BigDecimal withdrawals =
                repository.totalWithdrawals(user.getId());

        BigDecimal balance =
                deposits.subtract(withdrawals);

        BigDecimal remainingSalary =
                calculateRemainingSalary(user);

        return new PiggyBankSummaryDTO(
                balance,
                deposits,
                withdrawals,
                remainingSalary
        );
    }

    @Override
    public List<PiggyBankTransactionDTO> getHistory(
            User user
    ) {

        return repository
                .findByUserOrderByTransactionDateDesc(user)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private BigDecimal calculateRemainingSalary(
            User user
    ) {

        BigDecimal totalIncome =
                incomeService.getTotalIncome(user.getId());

        BigDecimal totalExpense =
                expenseService.getTotalExpense(user.getId());

        BigDecimal totalDeposits =
                repository.totalDeposits(user.getId());

        return totalIncome
                .subtract(totalExpense)
                .subtract(totalDeposits);
    }

    private BigDecimal getCurrentBalance(
            Long userId
    ) {

        BigDecimal deposits =
                repository.totalDeposits(userId);

        BigDecimal withdrawals =
                repository.totalWithdrawals(userId);

        return deposits.subtract(withdrawals);
    }

    private PiggyBankTransactionDTO mapToDTO(
            PiggyBankTransaction transaction
    ) {

        return new PiggyBankTransactionDTO(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getType().name(),
                transaction.getNote(),
                transaction.getTransactionDate()
        );
    }
}