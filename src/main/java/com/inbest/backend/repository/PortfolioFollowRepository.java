package com.inbest.backend.repository;

import com.inbest.backend.model.PortfolioFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioFollowRepository extends JpaRepository<PortfolioFollow, Integer> {
    boolean existsByUserIdAndPortfolioId(Integer userId, Integer portfolioId);
    List<PortfolioFollow> findByUserIdOrderByCreatedAtDesc(Integer userId);
    List<PortfolioFollow> findByPortfolioIdOrderByCreatedAtDesc(Integer portfolioId);
    void deleteByUserIdAndPortfolioId(Integer userId, Integer portfolioId);
} 