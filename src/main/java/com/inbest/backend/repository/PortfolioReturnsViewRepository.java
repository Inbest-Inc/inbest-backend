package com.inbest.backend.repository;

import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.PortfolioReturnsView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PortfolioReturnsViewRepository extends JpaRepository<PortfolioReturnsView, Long>
{
    @Query(value = "SELECT portfolio_id, date, normalized_portfolio_return as portfolio_return FROM portfolioreturnnormalized WHERE portfolio_id = :portfolioId ORDER BY date", nativeQuery = true)
    List<Object[]> findPortfolioReturns(@Param("portfolioId") Integer portfolioId);
}
