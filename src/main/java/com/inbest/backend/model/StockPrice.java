package com.inbest.backend.model;

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
public class StockPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticker_symbol", nullable = false)
    private String tickerSymbol;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;
} 