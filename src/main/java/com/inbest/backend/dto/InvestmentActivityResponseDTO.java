package com.inbest.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentActivityResponseDTO {
    private Long activityId;
    private Integer portfolioId;
    private Integer stockId;
    private String stockSymbol;
    private String stockName;
    private String actionType;
    private BigDecimal amount;
    private Double stockQuantity;
    private LocalDateTime date;
} 