package com.inbest.backend.repository;

import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;


public interface PortfolioRepository extends JpaRepository<Portfolio, Long>
{
    Optional<Portfolio> findByPortfolioId(int portfolioId);
    boolean existsByPortfolioName(String portfolioName);

    List<Portfolio> findByUser(User user);
    @Query("SELECT p FROM Portfolio p WHERE p.user = :user AND p.visibility = :visibility")
    List<Portfolio> findByUserAndVisibility(@Param("user") User user, @Param("visibility") String visibility);
}
