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
public class GoldDTO {

    private Long id;

    @NotNull(message = "Grams purchased is required")
    @DecimalMin(value = "0.0001", message = "Grams must be greater than 0")
    private BigDecimal gramsPurchased;

    @NotNull(message = "Purchase price per gram is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal purchasePricePerGram;

    @NotNull(message = "Purchase date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;
}