package com.inbest.backend.model.position;

import com.inbest.backend.model.Portfolio;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "portfoliometrics")
public class PortfolioMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_metrics_id")
    private Integer portfolioMetricsId;

    @Column(name = "portfolio_id", nullable = false)
    private Integer portfolioId;

    @Column(name = "last_updated_date", nullable = false)
    private LocalDateTime lastUpdatedDate;

    @Column(name = "hourly_return")
    private BigDecimal hourlyReturn;

    @Column(name = "daily_return")
    private BigDecimal dailyReturn;

    @Column(name = "monthly_return")
    private BigDecimal monthlyReturn;

    @Column(name = "total_return")
    private BigDecimal totalReturn;

    @Column(name = "beta")
    private BigDecimal beta;

    @Column(name = "sharpe_ratio")
    private BigDecimal sharpeRatio;

    @Column(name = "volatility")
    private BigDecimal volatility;

    @Column(name = "portfolio_value")
    private BigDecimal portfolioValue;

    @Column(name = "risk_score")
    private BigDecimal riskScore;

    @Column(name = "risk_category", length = 20)
    private String riskCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", referencedColumnName = "portfolio_id", insertable = false, updatable = false)
    private Portfolio portfolio;
}
