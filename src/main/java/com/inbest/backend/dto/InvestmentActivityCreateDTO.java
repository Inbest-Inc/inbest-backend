package com.inbest.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentActivityCreateDTO {
    @NotNull(message = "Portfolio ID cannot be null")
    private Integer portfolioId;

    @NotNull(message = "Stock ID cannot be null")
    private Integer stockId;

    @NotNull(message = "Action type cannot be null")
    private String actionType;

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Stock quantity cannot be null")
    @Positive(message = "Stock quantity must be positive")
    private Integer stockQuantity;
} 