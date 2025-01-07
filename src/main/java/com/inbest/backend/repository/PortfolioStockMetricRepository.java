package com.inbest.backend.repository;

import com.inbest.backend.model.position.PortfolioStockMetric;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    Optional<PortfolioStockMetric> findByPortfolioIdAndStockId(Integer portfolioId, Integer stockId);

    @Query(value = "SELECT SUM(quantity) " +
            "FROM positionmetrics " +
            "WHERE portfolio_id = :portfolioId", nativeQuery = true)
    BigDecimal findTotalPortfolioValue(@Param("portfolioId") int portfolioId);

    void deleteByPortfolioIdAndStockId(Integer portfolioId, Integer stockId);

    List<PortfolioStockMetric> findByPortfolioId(Integer portfolioId);

    @Query("SELECT MAX(psm.date) FROM PortfolioStockMetric psm WHERE psm.portfolioId = :portfolioId")
    LocalDateTime findLatestDateByPortfolioId(@Param("portfolioId") Integer portfolioId);

    @Query("SELECT psm FROM PortfolioStockMetric psm WHERE psm.portfolioId = :portfolioId AND psm.date = :date")
    List<PortfolioStockMetric> findByPortfolioIdAndDate(@Param("portfolioId") Integer portfolioId, @Param("date") LocalDateTime date);

    void deleteByPortfolioIdAndStockIdAndDate(Integer portfolioId, Integer stockId, LocalDateTime date);

}
