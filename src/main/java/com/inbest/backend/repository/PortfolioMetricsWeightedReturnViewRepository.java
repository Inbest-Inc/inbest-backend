package com.inbest.backend.repository;

import com.inbest.backend.model.PortfolioMetricsWeightedReturnView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PortfolioMetricsWeightedReturnViewRepository extends JpaRepository<PortfolioMetricsWeightedReturnView, Integer> {

    Optional<PortfolioMetricsWeightedReturnView> findByPortfolioId(Integer portfolioId);

    List<PortfolioMetricsWeightedReturnView> findAll();

    @Query("SELECT p FROM PortfolioMetricsWeightedReturnView p WHERE p.portfolioId = :portfolioId")
    Optional<PortfolioMetricsWeightedReturnView> findMetricsByPortfolioId(Integer portfolioId);
}
