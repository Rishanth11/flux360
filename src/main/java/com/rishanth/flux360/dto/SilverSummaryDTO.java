package com.rishanth.flux360.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class SilverSummaryDTO {

    private BigDecimal totalGrams;

    private BigDecimal totalInvested;

    private BigDecimal totalCurrentValue;

    private BigDecimal totalProfitLoss;

    private BigDecimal totalProfitLossPercent;

    private boolean stale;

    private String priceAsOf;

    private List<SilverHistoryDTO> investments;
}