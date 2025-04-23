package com.inbest.backend.repository;

import com.inbest.backend.model.TradeMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeMetricsRepository extends JpaRepository<TradeMetrics, Long> {

    List<TradeMetrics> findByPortfolioId(Integer portfolioId);
    TradeMetrics findTopByPortfolioIdAndIsBestTradeTrue(Integer portfolioId);
    TradeMetrics findTopByPortfolioIdAndIsWorstTradeTrue(Integer portfolioId);
    TradeMetrics findTopByPortfolioIdInOrderByExitDateDesc(List<Integer> portfolioIds);
    List<TradeMetrics> findByPortfolioIdInAndIsBestTradeTrue(List<Integer> portfolioIds);
    List<TradeMetrics> findByPortfolioIdInAndIsWorstTradeTrue(List<Integer> portfolioIds);


}