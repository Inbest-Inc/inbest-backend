package com.inbest.backend.repository;

import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long>
{
    boolean existsByPortfolioName(String portfolioName);

    List<Portfolio> findByUser(User user);
}
