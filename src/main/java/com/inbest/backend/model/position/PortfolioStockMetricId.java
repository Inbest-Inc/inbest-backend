package com.inbest.backend.model.position;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioStockMetricId implements Serializable {
    private Integer portfolioId;
    private Integer stockId;
    private LocalDateTime date;
}
