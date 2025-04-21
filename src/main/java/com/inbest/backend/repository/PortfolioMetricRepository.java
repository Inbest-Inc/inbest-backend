package com.inbest.backend.repository;

import com.inbest.backend.model.position.PortfolioMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PortfolioMetricRepository extends JpaRepository<PortfolioMetric, Integer> {

    /**
     * Finds all metrics for a specific portfolio ID, ordered by last updated date in descending order
     *
     * @param portfolioId The ID of the portfolio
     * @return List of portfolio metrics for the specified portfolio, with most recent first
     */
    List<PortfolioMetric> findByPortfolioIdOrderByLastUpdatedDateDesc(Integer portfolioId);
}
