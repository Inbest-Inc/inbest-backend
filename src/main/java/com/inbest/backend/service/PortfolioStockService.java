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
                .currentValue(BigDecimal.valueOf(stock.getCurrentPrice()))
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
    public PortfolioStockResponse updateQuantity(Integer portfolioId, Integer stockId, Integer quantity) throws Exception
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

        String transactionType = quantity < portfolioStockModel.getQuantity() ? "SELL" : "BUY";

        PortfolioStockMetric portfolioStockMetric = portfolioStockMetricRepository
                .findByPortfolioIdAndStockId(portfolioId, stockId)
                .orElseThrow(() -> new Exception("Metrics not found"));

        int oldQuantity = portfolioStockMetric.getQuantity();
        BigDecimal currentPrice = BigDecimal.valueOf(stock.getCurrentPrice());

        BigDecimal avgCost = portfolioStockMetric.getAverageCost();
        BigDecimal totalReturn = portfolioStockMetric.getTotalReturn();
        BigDecimal positionWeight = portfolioStockMetric.getPositionWeight();
        BigDecimal totalQuantity = portfolioStockMetricRepository.findTotalPortfolioValue(portfolioId);

        if (transactionType.equals("BUY"))
        {
            totalQuantity = totalQuantity.add(BigDecimal.valueOf(quantity - oldQuantity));
            avgCost = ((avgCost.multiply(BigDecimal.valueOf(oldQuantity)))
                    .add(currentPrice.multiply(BigDecimal.valueOf(quantity - oldQuantity))))
                    .divide(BigDecimal.valueOf(quantity), 2, BigDecimal.ROUND_HALF_UP);
            //avgCost = ((avgCost * oldQuantity) + (currentPrice * quantityDiff)) / totalQuantity
        }
        else
        {
            totalQuantity = totalQuantity.subtract(BigDecimal.valueOf(oldQuantity - quantity));
            avgCost = ((avgCost.multiply(BigDecimal.valueOf(oldQuantity)))
                    .subtract(currentPrice.multiply(BigDecimal.valueOf(oldQuantity - quantity))))
                    .divide(BigDecimal.valueOf(quantity), 2, BigDecimal.ROUND_HALF_UP);
        }

        totalReturn = currentPrice.divide(avgCost, 2, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)).subtract(BigDecimal.valueOf(100));

        positionWeight = BigDecimal.valueOf(quantity).divide(totalQuantity, 2, BigDecimal.ROUND_HALF_UP);
        portfolioStockModel.setQuantity((double) quantity);
        portfolioStockRepository.save(portfolioStockModel);

        portfolioStockMetric.setQuantity(quantity);
        portfolioStockMetric.setAverageCost(avgCost);
        portfolioStockMetric.setCurrentValue(BigDecimal.valueOf(stock.getCurrentPrice()));
        portfolioStockMetric.setTotalReturn(totalReturn);
        portfolioStockMetric.setPositionWeight(positionWeight);
        portfolioStockMetric.setLastTransactionType(transactionType);
        portfolioStockMetric.setLastTransactionDate(Timestamp.from(Instant.now()).toLocalDateTime());
        portfolioStockMetric.setLastUpdated(Timestamp.from(Instant.now()).toLocalDateTime());
        portfolioStockMetricRepository.save(portfolioStockMetric);

        //return düzeltilecek!
        return new PortfolioStockResponse(
                portfolioStockModel.getPortfolioStockId(),
                stock.getStockName(),
                stock.getTickerSymbol(),
                portfolioStockModel.getQuantity(),
                stock.getCurrentPrice()
        );
    }
}
