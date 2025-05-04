package com.inbest.backend.repository;

import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Map;
import java.util.Optional;


public interface PortfolioRepository extends JpaRepository<Portfolio, Long>
{
    Optional<Portfolio> findByPortfolioId(int portfolioId);
    boolean existsByPortfolioName(String portfolioName);

    List<Portfolio> findByUser(User user);
    @Query("SELECT p FROM Portfolio p WHERE p.user = :user AND p.visibility = :visibility")
    List<Portfolio> findByUserAndVisibility(@Param("user") User user, @Param("visibility") String visibility);

    @Query(value = """
            SELECT *
            FROM (
                SELECT
                    p.portfolio_id,
                    RANK() OVER (ORDER BY pm.total_return DESC) AS portfolio_rank,
                    COUNT(*) OVER () AS total_portfolios
                FROM portfolio p
                JOIN (
                    SELECT DISTINCT ON (portfolio_id) *
                    FROM portfoliometrics
                    WHERE total_return IS NOT NULL
                    ORDER BY portfolio_id, last_updated_date DESC
                ) pm ON p.portfolio_id = pm.portfolio_id
            ) ranked
            WHERE portfolio_id = :portfolioId
            """, nativeQuery = true)
    Map<String, Object> findPortfolioRankAndTotal(@Param("portfolioId") int portfolioId);
}
