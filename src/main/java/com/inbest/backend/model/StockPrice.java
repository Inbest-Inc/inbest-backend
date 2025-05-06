package com.inbest.backend.model;

import com.inbest.backend.model.position.PortfolioStockMetricId;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stockprice")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(StockPriceId.class)  // Composite Key kullanımı için
public class StockPrice {

    @Id
    @Column(name = "ticker_symbol", nullable = false)
    private String tickerSymbol;

    @Id
    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

}
