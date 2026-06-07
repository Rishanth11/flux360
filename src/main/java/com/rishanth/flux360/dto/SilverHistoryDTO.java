package com.rishanth.flux360.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class SilverHistoryDTO {

    private Long id;

    private BigDecimal grams;

    private BigDecimal purchasePrice;

    private LocalDate purchaseDate;

    private BigDecimal investedAmount;

    private BigDecimal currentPricePerGram;

    private BigDecimal currentValue;

    private BigDecimal profitLoss;

    private BigDecimal profitLossPercent;
}