package com.inbest.backend.repository;

import com.inbest.backend.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long>
{
    boolean existsByPortfolioName(String portfolioName);
}
