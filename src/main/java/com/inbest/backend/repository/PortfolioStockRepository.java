package com.inbest.backend.repository;

import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.PortfolioStockModel;
import com.inbest.backend.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioStockRepository extends JpaRepository<PortfolioStockModel, Long> {
    boolean existsByPortfolioAndStock(Portfolio portfolio, Stock stock);
}
