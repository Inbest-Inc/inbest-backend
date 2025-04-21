package com.inbest.backend.model.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioMetricResponse
{
    private int portfolioId;
    private BigDecimal hourlyReturn;
    private BigDecimal dailyReturn;
    private BigDecimal monthlyReturn;
    private BigDecimal totalReturn;
    private BigDecimal beta;
    private BigDecimal sharpeRatio;
    private BigDecimal volatility;
    private BigDecimal riskScore;
    private String riskCategory;

}
