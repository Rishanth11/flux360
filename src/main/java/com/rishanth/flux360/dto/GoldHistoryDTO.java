package com.rishanth.flux360.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoldHistoryDTO {

    private Long id;
    private LocalDate purchaseDate;
    private BigDecimal gramsPurchased;
    private BigDecimal purchasePricePerGram;
    private BigDecimal totalInvested;
    private BigDecimal currentValue;
    private BigDecimal profit;

}