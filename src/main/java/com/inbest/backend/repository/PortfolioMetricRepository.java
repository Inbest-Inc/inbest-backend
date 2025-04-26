package com.inbest.backend.repository;

import com.inbest.backend.model.position.PortfolioMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PortfolioMetricRepository extends JpaRepository<PortfolioMetric, Integer>
{

    /**
     * Finds all metrics for a specific portfolio ID, ordered by last updated date in descending order
     *
     * @param portfolioId The ID of the portfolio
     * @return List of portfolio metrics for the specified portfolio, with most recent first
     */
    List<PortfolioMetric> findByPortfolioIdOrderByLastUpdatedDateDesc(Integer portfolioId);

    @Query(value = """
                SELECT pm.*
                        FROM (
                                 SELECT DISTINCT ON (pm.portfolio_id) pm.*
                                 FROM portfoliometrics pm
                                          JOIN portfolio p ON pm.portfolio_id = p.portfolio_id
                                 WHERE p.visibility = 'public'
                                 ORDER BY pm.portfolio_id, pm.daily_return DESC
                             ) pm
                        ORDER BY pm.total_return DESC
                        LIMIT 10;
            
            """, nativeQuery = true)
    List<PortfolioMetric> findTop10ByTotalReturnForPublic();

    @Query(value = """
               SELECT pm.*
            FROM (
                     SELECT DISTINCT ON (pm.portfolio_id) pm.*
                     FROM portfoliometrics pm
                              JOIN portfolio p ON pm.portfolio_id = p.portfolio_id
                     WHERE p.visibility = 'public'
                     ORDER BY pm.portfolio_id, pm.daily_return DESC
                 ) pm
            ORDER BY pm.daily_return DESC
            LIMIT 10;
            
            """, nativeQuery = true)
    List<PortfolioMetric> findTop10ByDailyReturnForPublic();

    @Query(value = """
              SELECT pm.*
            FROM (
                     SELECT DISTINCT ON (pm.portfolio_id) pm.*
                     FROM portfoliometrics pm
                              JOIN portfolio p ON pm.portfolio_id = p.portfolio_id
                     WHERE p.visibility = 'public'
                     ORDER BY pm.portfolio_id, pm.daily_return DESC
                 ) pm
            ORDER BY pm.monthly_return DESC
            LIMIT 10;
            
            """, nativeQuery = true)
    List<PortfolioMetric> findTop10ByMonthlyReturnForPublic();

    @Query(value = """
             SELECT pm.*
            FROM (
                     SELECT DISTINCT ON (pm.portfolio_id) pm.*
                     FROM portfoliometrics pm
                              JOIN portfolio p ON pm.portfolio_id = p.portfolio_id
                     WHERE p.visibility = 'public'
                     ORDER BY pm.portfolio_id, pm.daily_return DESC
                 ) pm
            ORDER BY pm.hourly_return DESC
            LIMIT 10;
            
            """, nativeQuery = true)
    List<PortfolioMetric> findTop10ByHourlyReturnForPublic();

}
