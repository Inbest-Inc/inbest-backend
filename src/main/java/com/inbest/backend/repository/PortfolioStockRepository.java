package com.inbest.backend.repository;

import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.PortfolioStockModel;
import com.inbest.backend.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PortfolioStockRepository extends JpaRepository<PortfolioStockModel, Long>
{
    boolean existsByPortfolioAndStock(Portfolio portfolio, Stock stock);

    Optional<PortfolioStockModel> findByPortfolio_PortfolioIdAndStock_StockId(Integer portfolioId, Integer stockId);
    List<PortfolioStockModel> findByPortfolio_PortfolioId(Long portfolioId);
    void deleteByPortfolio_PortfolioIdAndStock_StockId(Integer portfolioId, Integer stockId);

    @Query("SELECT COUNT(DISTINCT ps.stock) FROM PortfolioStockModel ps WHERE ps.portfolio = :portfolio")
    Integer countDistinctStocksByPortfolio(Portfolio portfolio);
}
