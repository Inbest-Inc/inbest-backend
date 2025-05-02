package com.inbest.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trademetrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_id")
    private Long tradeId;

    @Column(name = "portfolio_id", nullable = false)
    private Integer portfolioId;

    @Column(name = "stock_id", nullable = false)
    private Integer stockId;

    @Column(name="entry_date",nullable = false)
    private LocalDateTime entryDate;

    @Column(name = "exit_date")
    private LocalDateTime exitDate;

    @Column(name = "average_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal averageCost;

    @Column(name = "exit_price", precision = 10, scale = 2)
    private BigDecimal exitPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "total_return", precision = 10, scale = 4)
    private BigDecimal totalReturn;

    @Column(name = "is_best_trade")
    private Boolean isBestTrade = false;

    @Column(name = "is_worst_trade")
    private Boolean isWorstTrade = false;

    @Column(name = "last_updated", insertable = false, updatable = false)
    private LocalDateTime lastUpdated;

    // ✅ Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", referencedColumnName = "stock_id", insertable = false, updatable = false)
    private Stock stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", referencedColumnName = "portfolio_id", insertable = false, updatable = false)
    private Portfolio portfolio;
}