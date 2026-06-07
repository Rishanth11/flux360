package com.rishanth.flux360.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SipPortfolioDTO {

    private BigDecimal totalInvested;

    private BigDecimal currentValue;

    private BigDecimal returns;

    private BigDecimal totalUnits;

    private BigDecimal xirr;

    private BigDecimal realReturn;

    private BigDecimal goalProgress;

    private boolean navAvailable;

    private String navAsOf;

    private String goalName;

    private BigDecimal targetAmount;
}