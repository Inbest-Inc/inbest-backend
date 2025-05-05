package com.inbest.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Immutable
@Table(name = "portfoliometricweightedreturnview")
public class PortfolioMetricsWeightedReturnView {

    @Id
    @Column(name = "portfolio_id")
    private Integer portfolioId;

    @Column(name = "hourly_return")
    private BigDecimal hourlyReturn;

    @Column(name = "daily_return")
    private BigDecimal dailyReturn;

    @Column(name = "monthly_return")
    private BigDecimal monthlyReturn;

    @Column(name = "ytd_return")
    private BigDecimal ytdReturn;

    @Column(name = "total_return")
    private BigDecimal totalReturn;
}
