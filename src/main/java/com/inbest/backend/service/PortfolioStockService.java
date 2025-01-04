package com.inbest.backend.service;

import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.PortfolioStockModel;
import com.inbest.backend.model.Stock;
import com.inbest.backend.model.position.PortfolioStockMetric;
import com.inbest.backend.model.response.PortfolioStockResponse;
import com.inbest.backend.repository.PortfolioRepository;
import com.inbest.backend.repository.PortfolioStockMetricRepository;
import com.inbest.backend.repository.PortfolioStockRepository;
import com.inbest.backend.repository.StockRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

@Service
public class PortfolioStockService
{

    private final PortfolioStockRepository portfolioStockRepository;
    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;
    private final PortfolioStockMetricRepository portfolioStockMetricRepository;

    public PortfolioStockService(PortfolioStockRepository portfolioStockRepository, PortfolioRepository portfolioRepository, StockRepository stockRepository, PortfolioStockMetricRepository portfolioStockMetricRepository)
    {
        this.portfolioStockRepository = portfolioStockRepository;
        this.portfolioRepository = portfolioRepository;
        this.stockRepository = stockRepository;
        this.portfolioStockMetricRepository = portfolioStockMetricRepository;
    }

    @Transactional
    public PortfolioStockResponse addStockToPortfolio(Integer portfolioId, Integer stockId) throws Exception
    {
        Portfolio portfolio = portfolioRepository.findById(Long.valueOf(portfolioId)).orElseThrow(() -> new Exception("Portfolio not found"));

        Stock stock = stockRepository.findById(Long.valueOf(stockId)).orElseThrow(() -> new Exception("Stock not found"));

        boolean stockExistsInPortfolio = portfolioStockRepository.existsByPortfolioAndStock(portfolio, stock);
        if (stockExistsInPortfolio)
        {
            throw new Exception("Stock is already added to the portfolio");
        }

        PortfolioStockModel portfolioStockModel = PortfolioStockModel.builder()
                .portfolio(portfolio)
                .stock(stock)
                .quantity((double) 0)
                .visibility(portfolio.getVisibility())
                .build();


        portfolioStockRepository.save(portfolioStockModel);

        PortfolioStockMetric portfolioStockMetric = PortfolioStockMetric.builder()
                .portfolioId(portfolioId)
                .stockId(stockId)
                .date(Timestamp.from(Instant.now()).toLocalDateTime())
                .quantity(0)
                .averageCost(BigDecimal.valueOf(0))
                .currentValue(BigDecimal.valueOf(0))
                .totalReturn(BigDecimal.valueOf(0))
                .positionWeight(BigDecimal.valueOf(0))
                .lastTransactionType("ADD")
                .lastUpdated(Timestamp.from(Instant.now()).toLocalDateTime())
                .build();

        portfolioStockMetricRepository.save(portfolioStockMetric);

        return new PortfolioStockResponse(
                portfolioStockModel.getPortfolioStockId(),
                stock.getStockName(),
                stock.getTickerSymbol(),
                portfolioStockModel.getQuantity(),
                stock.getCurrentPrice()
        );
    }

    @Transactional
    public void updateQuantity(Integer portfolioId, Integer stockId, Integer quantity) throws Exception
    {
        Portfolio portfolio = portfolioRepository.findById(Long.valueOf(portfolioId)).orElseThrow(() -> new Exception("Portfolio not found"));

        Stock stock = stockRepository.findById(Long.valueOf(stockId)).orElseThrow(() -> new Exception("Stock not found"));

        boolean stockExistInPortfolioStock = portfolioStockRepository.existsByPortfolioAndStock(portfolio, stock);
        if (!stockExistInPortfolioStock)
        {
            throw new Exception("You do not have portfolio or stock");
        }
        PortfolioStockModel portfolioStockModel = portfolioStockRepository
                .findById(Long.valueOf(portfolioId))
                .orElseThrow(() -> new Exception("Portfolio stock not found"));
        portfolioStockModel.setQuantity((double) quantity);
        portfolioStockRepository.save(portfolioStockModel);

        PortfolioStockMetric portfolioStockMetric = portfolioStockMetricRepository
                .findByPortfolioIdAndStockId(portfolioId, stockId)
                .orElseThrow(() -> new Exception("Metrics not found"));
        portfolioStockMetric.setQuantity(quantity);
        portfolioStockMetric.setLastTransactionType("BUY");
        portfolioStockMetric.setLastTransactionDate(Timestamp.from(Instant.now()).toLocalDateTime());
        portfolioStockMetric.setLastUpdated(Timestamp.from(Instant.now()).toLocalDateTime());
        portfolioStockMetricRepository.save(portfolioStockMetric);

    }
}
