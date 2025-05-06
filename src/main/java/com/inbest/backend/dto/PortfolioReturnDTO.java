package com.inbest.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioReturnDTO
{
    private LocalDateTime date;
    private BigDecimal portfolioReturn;
    private BigDecimal spyReturn;
    private BigDecimal goldReturn;

    public PortfolioReturnDTO(LocalDateTime date, BigDecimal portfolioReturn)
    {
        this.portfolioReturn = portfolioReturn;
        this.date = date;
    }
}
