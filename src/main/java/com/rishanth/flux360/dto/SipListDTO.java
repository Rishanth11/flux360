package com.rishanth.flux360.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SipListDTO {

    private Long id;

    private String fundName;

    private String fundCode;

    private BigDecimal monthlyAmount;

    private LocalDate startDate;

    private int sipDay;

    private boolean active;

    private String goalName;

    private BigDecimal targetAmount;
}