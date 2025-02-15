package com.inbest.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "stockprice")
@IdClass(StockPriceId.class)  // Composite primary key için gerekli
public class StockPrice {

    @Id
    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Id
    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private Double price;
}