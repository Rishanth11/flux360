package com.rishanth.flux360.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PiggyBankSummaryDTO {

    private BigDecimal balance;

    private BigDecimal totalDeposits;

    private BigDecimal totalWithdrawals;

    private BigDecimal remainingSalary;
}