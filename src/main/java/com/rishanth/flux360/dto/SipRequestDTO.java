package com.rishanth.flux360.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SipRequestDTO {

    @NotBlank(message = "Fund name is required")
    private String fundName;

    @NotBlank(message = "Fund code is required")
    private String fundCode;

    @NotNull(message = "Monthly amount is required")
    @DecimalMin(
            value = "100",
            message = "Minimum SIP amount is 100"
    )
    private BigDecimal monthlyAmount;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(
            message = "Start date cannot be in the past"
    )
    private LocalDate startDate;

    @Min(value = 1, message = "SIP day must be between 1 and 28")
    @Max(value = 28, message = "SIP day must be between 1 and 28")
    private int sipDay;

    private String goalName;

    @DecimalMin(
            value = "0",
            message = "Target amount must be positive"
    )
    private BigDecimal targetAmount;

    @DecimalMin(
            value = "0",
            message = "Inflation rate cannot be negative"
    )
    private BigDecimal inflationRate;
}