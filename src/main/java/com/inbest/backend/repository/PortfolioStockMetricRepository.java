package com.inbest.backend.repository;

import com.inbest.backend.model.position.PortfolioStockMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface PortfolioStockMetricRepository extends JpaRepository<PortfolioStockMetric, Long>
{
    @Query(value = "SELECT pm.stock_id, " +
                   "s.ticker_symbol AS symbol ," +
                   "s.stock_name AS name, " +
                   "pm.quantity AS shares, " +
                   "pm.position_weight AS allocation, " +
                   "pm.average_cost AS averagePrice, " +
                   "pm.current_value AS currentPrice, " +
                   "pm.total_return AS return " +
            "FROM positionmetrics pm " +
            "JOIN stock s ON pm.stock_id = s.stock_id " +
            "WHERE pm.portfolio_id = :portfolioId " +
            "ORDER BY pm.date DESC", nativeQuery = true)
    List<Map<String, Object>> findMetricsByPortfolioId(@Param("portfolioId") int portfolioId);
}
