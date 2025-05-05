package com.inbest.backend.repository;

import com.inbest.backend.model.InvestmentActivity;
import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvestmentActivityRepository extends JpaRepository<InvestmentActivity, Long> {
    List<InvestmentActivity> findByPortfolio_PortfolioId(Integer portfolioId);
    List<InvestmentActivity> findByStock_StockId(Long stockId);
    Optional<InvestmentActivity> findByActivityId(Long activityId);
    Optional<InvestmentActivity> findTopByPortfolio_PortfolioIdAndStock_StockIdAndActionTypeOrderByDateDesc(
            Integer portfolioId, Integer stockId, InvestmentActivity.ActionType actionType);

    @Query("SELECT ia.date FROM InvestmentActivity ia " +
            "WHERE ia.portfolio.id = :portfolioId AND ia.stock.id = :stockId AND ia.actionType = 'OPEN' " +
            "ORDER BY ia.date DESC LIMIT 1")
    Optional<LocalDateTime> findLatestOpenDateByPortfolioIdAndStockId(
            @Param("portfolioId") Long portfolioId,
            @Param("stockId") Long stockId);
} 