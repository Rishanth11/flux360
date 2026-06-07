package com.rishanth.flux360.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SipTransactionDTO {

    private Long id;

    private LocalDate investDate;

    private BigDecimal nav;

    private BigDecimal units;

    private BigDecimal amount;

    private BigDecimal currentValue;
}