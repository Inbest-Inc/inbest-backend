package com.inbest.backend.repository;

import com.inbest.backend.model.InvestmentActivity;
import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvestmentActivityRepository extends JpaRepository<InvestmentActivity, Long> {
    List<InvestmentActivity> findByPortfolio_PortfolioId(Integer portfolioId);
    List<InvestmentActivity> findByStock_StockId(Long stockId);
    Optional<InvestmentActivity> findByActivityId(Long activityId);
    Optional<InvestmentActivity> findTopByPortfolio_PortfolioIdAndStock_StockIdAndActionTypeOrderByDateDesc(
            Integer portfolioId, Integer stockId, InvestmentActivity.ActionType actionType);
} 