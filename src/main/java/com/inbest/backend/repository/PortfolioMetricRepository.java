package com.inbest.backend.repository;

import com.inbest.backend.model.position.PortfolioMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PortfolioMetricRepository extends JpaRepository<PortfolioMetric, Integer> {

    /**
     * Finds all metrics for a specific portfolio ID, ordered by last updated date in descending order
     *
     * @param portfolioId The ID of the portfolio
     * @return List of portfolio metrics for the specified portfolio, with most recent first
     */
    List<PortfolioMetric> findByPortfolioIdOrderByLastUpdatedDateDesc(Integer portfolioId);

    @Query(value = """
            SELECT pm.*
            FROM portfoliometrics pm
            WHERE pm.portfolio_id = :portfolioId
            AND pm.last_updated_date BETWEEN :startDate AND :endDate
            ORDER BY pm.last_updated_date ASC
            """, nativeQuery = true)
    List<PortfolioMetric> findByPortfolioIdAndDateBetweenOrderByLastUpdatedDateAsc(
            @Param("portfolioId") Integer portfolioId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query(value = """
    SELECT pm.* FROM (
        SELECT DISTINCT ON (pm.portfolio_id) pm.* 
        FROM portfoliometrics pm 
        JOIN portfolio p ON pm.portfolio_id = p.portfolio_id
        WHERE p.visibility = 'public'
        AND p.portfolio_id IN (
            SELECT ps.portfolio_id 
            FROM portfoliostock ps 
            GROUP BY ps.portfolio_id 
            HAVING COUNT(DISTINCT ps.stock_id) > 0
        )
        ORDER BY pm.portfolio_id, pm.last_updated_date DESC
    ) pm 
    ORDER BY pm.total_return DESC 
    LIMIT 10;
    """, nativeQuery = true)
    List<PortfolioMetric> findTop10ByTotalReturnForPublic();

    @Query(value = """
    SELECT pm.* FROM (
        SELECT DISTINCT ON (pm.portfolio_id) pm.* 
        FROM portfoliometrics pm 
        JOIN portfolio p ON pm.portfolio_id = p.portfolio_id
        WHERE p.visibility = 'public'
        AND p.portfolio_id IN (
            SELECT ps.portfolio_id 
            FROM portfoliostock ps 
            GROUP BY ps.portfolio_id 
            HAVING COUNT(DISTINCT ps.stock_id) > 0
        )
        ORDER BY pm.portfolio_id, pm.last_updated_date DESC
    ) pm 
    ORDER BY pm.daily_return DESC 
    LIMIT 10;
    """, nativeQuery = true)
    List<PortfolioMetric> findTop10ByDailyReturnForPublic();

    @Query(value = """
    SELECT pm.* FROM (
        SELECT DISTINCT ON (pm.portfolio_id) pm.* 
        FROM portfoliometrics pm 
        JOIN portfolio p ON pm.portfolio_id = p.portfolio_id
        WHERE p.visibility = 'public'
        AND p.portfolio_id IN (
            SELECT ps.portfolio_id 
            FROM portfoliostock ps 
            GROUP BY ps.portfolio_id 
            HAVING COUNT(DISTINCT ps.stock_id) > 0
        )
        ORDER BY pm.portfolio_id, pm.last_updated_date DESC
    ) pm 
    ORDER BY pm.monthly_return DESC 
    LIMIT 10;
    """, nativeQuery = true)
    List<PortfolioMetric> findTop10ByMonthlyReturnForPublic();

    @Query(value = """
    SELECT pm.* FROM (
        SELECT DISTINCT ON (pm.portfolio_id) pm.* 
        FROM portfoliometrics pm 
        JOIN portfolio p ON pm.portfolio_id = p.portfolio_id
        WHERE p.visibility = 'public'
        AND p.portfolio_id IN (
            SELECT ps.portfolio_id 
            FROM portfoliostock ps 
            GROUP BY ps.portfolio_id 
            HAVING COUNT(DISTINCT ps.stock_id) > 0
        )
        ORDER BY pm.portfolio_id, pm.last_updated_date DESC
    ) pm 
    ORDER BY pm.hourly_return DESC 
    LIMIT 10;
    """, nativeQuery = true)
    List<PortfolioMetric> findTop10ByHourlyReturnForPublic();

}
