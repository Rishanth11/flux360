package com.rishanth.flux360.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PiggyBankTransactionDTO {

    private Long id;

    private BigDecimal amount;

    private String type;

    private String note;

    private LocalDate transactionDate;
}