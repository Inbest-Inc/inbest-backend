package com.inbest.backend.service;

import com.inbest.backend.repository.PortfolioRepository;
import com.inbest.backend.repository.PortfolioStockMetricRepository;
import com.inbest.backend.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PortfolioStockMetricService
{
    private final PortfolioStockMetricRepository portfolioStockMetricRepository;

    public List<Map<String, Object>> getStocksAndMetrics(int portfolioID) {
        return portfolioStockMetricRepository.findMetricsByPortfolioId(portfolioID);
    }
}
