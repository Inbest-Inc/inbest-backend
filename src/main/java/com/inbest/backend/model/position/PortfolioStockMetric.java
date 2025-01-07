package com.inbest.backend.model.position;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "positionmetrics")
@IdClass(PortfolioStockMetricId.class)  // Composite Key kullanımı için
public class PortfolioStockMetric {

    @Id
    @Column(name = "portfolio_id")
    private Integer portfolioId;

    @Id
    @Column(name = "stock_id")
    private Integer stockId;

    @Id
    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "average_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal averageCost;

    @Column(name = "current_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentValue;

    @Column(name = "total_return", precision = 10, scale = 4)
    private BigDecimal totalReturn;

    @Column(name = "position_weight", precision = 10, scale = 4)
    private BigDecimal positionWeight;

    @Column(name = "last_transaction_type")
    private String lastTransactionType;

    @Column(name = "last_transaction_date")
    private LocalDateTime lastTransactionDate;

    @Column(name = "last_updated", insertable = false, updatable = false)
    private LocalDateTime lastUpdated;
}
