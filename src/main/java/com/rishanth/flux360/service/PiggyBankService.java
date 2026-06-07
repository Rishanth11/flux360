package com.rishanth.flux360.service;

import com.rishanth.flux360.dto.*;
import com.rishanth.flux360.entity.User;

import java.math.BigDecimal;
import java.util.List;

public interface PiggyBankService {

    PiggyBankTransactionDTO deposit(
            BigDecimal amount,
            String note,
            User user
    );

    PiggyBankTransactionDTO withdraw(
            BigDecimal amount,
            String note,
            User user
    );

    PiggyBankSummaryDTO getSummary(User user);

    List<PiggyBankTransactionDTO> getHistory(User user);
}