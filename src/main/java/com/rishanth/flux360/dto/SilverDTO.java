package com.rishanth.flux360.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SilverDTO {

    private Long id;

    @NotNull(message = "Grams is required")
    @DecimalMin(value = "0.0001", message = "Grams must be greater than 0")
    private BigDecimal grams;

    @NotNull(message = "Price per gram is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal pricePerGram;

    @NotNull(message = "Purchase date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;
}