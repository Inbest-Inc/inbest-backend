package com.inbest.backend.model.response;

import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioStockResponse
{
    private int portfolioStockId;
    private String stockName;
    private String tickerSymbol;
    private Double stockQuantity;
    private Double currentPrice;
}
