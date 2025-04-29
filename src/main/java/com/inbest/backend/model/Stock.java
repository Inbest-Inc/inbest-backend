package com.inbest.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stock")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    private Integer stockId;

    @Column(name = "ticker_symbol", nullable = false, length = 10, unique = true)
    private String tickerSymbol;

    @Column(name = "stock_name", nullable = false, length = 100)
    private String stockName;

    @Column(name = "current_price", nullable = false)
    private Double currentPrice;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
