package com.inbest.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Immutable
@Table(name = "portfolioreturnsview")
public class PortfolioReturnsView
{
    @Id
    @Column(name = "portfolio_id")
    private Long portfolioId;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "portfolio_return")
    private BigDecimal portfolioReturn;
}
