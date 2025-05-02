package com.inbest.backend.model.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class TradeMetricsResponseDTO {
    private Long tradeId;
    private Integer portfolioId;
    private Integer stockId;
    private String tickerSymbol;
    private BigDecimal averageCost;
    private BigDecimal exitPrice;
    private BigDecimal quantity;
    private BigDecimal totalReturn;
    private LocalDateTime entryDate;
    private LocalDateTime exitDate;
}
