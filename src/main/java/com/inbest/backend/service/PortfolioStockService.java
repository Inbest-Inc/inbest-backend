package com.inbest.backend.service;

import com.inbest.backend.model.InvestmentActivity;
import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.PortfolioStockModel;
import com.inbest.backend.model.Stock;
import com.inbest.backend.model.position.PortfolioStockMetric;
import com.inbest.backend.model.response.PortfolioStockResponse;
import com.inbest.backend.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PortfolioStockService
{

    private final PortfolioStockRepository portfolioStockRepository;
    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;
    private final PortfolioStockMetricRepository portfolioStockMetricRepository;
    private final InvestmentActivityRepository investmentActivityRepository;

    public PortfolioStockService(PortfolioStockRepository portfolioStockRepository, PortfolioRepository portfolioRepository, StockRepository stockRepository, PortfolioStockMetricRepository portfolioStockMetricRepository, InvestmentActivityRepository investmentActivityRepository)
    {
        this.portfolioStockRepository = portfolioStockRepository;
        this.portfolioRepository = portfolioRepository;
        this.stockRepository = stockRepository;
        this.portfolioStockMetricRepository = portfolioStockMetricRepository;
        this.investmentActivityRepository = investmentActivityRepository;
    }


    public void addStockToPortfolio(Integer portfolioId, String tickerName, Double quantity) throws Exception
    {
        Portfolio portfolio = portfolioRepository.findById(Long.valueOf(portfolioId)).orElseThrow(() -> new Exception("Portfolio not found"));

        Stock stock = stockRepository.findByTickerSymbol(tickerName).orElseThrow(() -> new Exception("Stock not found"));

        boolean stockExistsInPortfolio = portfolioStockRepository.existsByPortfolioAndStock(portfolio, stock);
        if (stockExistsInPortfolio)
        {
            throw new Exception("Stock is already added to the portfolio");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        PortfolioStockModel portfolioStockModel = PortfolioStockModel.builder()
                .portfolio(portfolio)
                .stock(stock)
                .quantity(quantity)
                .build();

       InvestmentActivity activity = new InvestmentActivity();
       activity.setPortfolio(portfolio);
       activity.setStock(stock);
       activity.setStockQuantity(quantity);
       activity.setDate(LocalDateTime.now());
       activity.setAmount(BigDecimal.valueOf(quantity * stock.getCurrentPrice()));
       activity.setActionType(InvestmentActivity.ActionType.ADD);
       activity.setOldPositionWeight(BigDecimal.ZERO);


        portfolioStockRepository.save(portfolioStockModel);

        PortfolioStockMetric portfolioStockMetric = PortfolioStockMetric.builder()
                .portfolioId(portfolioId)
                .stockId(stock.getStockId())
                .date(LocalDate.now().atStartOfDay())
                .quantity(quantity)
                .averageCost(BigDecimal.valueOf(stock.getCurrentPrice()))
                .currentValue(BigDecimal.valueOf(stock.getCurrentPrice()))
                .totalReturn(BigDecimal.valueOf(0))
                .positionWeight(BigDecimal.valueOf(0))
                .lastTransactionType("ADD")
                .lastUpdated(Timestamp.from(Instant.now()).toLocalDateTime())
                .build();

        portfolioStockMetricRepository.save(portfolioStockMetric);
        PortfolioStockMetric portfolioStockMetricNew = portfolioStockMetricRepository.findTopByPortfolioIdAndStockIdOrderByDateDesc(portfolioId,stock.getStockId()).orElseThrow(() -> new Exception("Metrics not found"));


        recalculatePositionWeights(portfolioId);
        activity.setNewPositionWeight(portfolioStockMetricNew.getPositionWeight());
        investmentActivityRepository.save(activity);
    }

    @Transactional
    public void updateQuantity(Integer portfolioId, String tickerName, Double quantity) throws Exception
    {
        Portfolio portfolio = portfolioRepository.findById(Long.valueOf(portfolioId)).orElseThrow(() -> new Exception("Portfolio not found"));

        Stock stock = stockRepository.findByTickerSymbol(tickerName).orElseThrow(() -> new Exception("Stock not found"));

        boolean stockExistInPortfolioStock = portfolioStockRepository.existsByPortfolioAndStock(portfolio, stock);
        if (!stockExistInPortfolioStock)
        {
            throw new Exception("You do not have portfolio or stock");
        }

        PortfolioStockModel portfolioStockModel = portfolioStockRepository
                .findByPortfolio_PortfolioIdAndStock_StockId(portfolioId, stock.getStockId())
                .orElseThrow(() -> new Exception("Portfolio stock not found"));

        String transactionType = quantity < portfolioStockModel.getQuantity() ? "SELL" : "BUY";

        PortfolioStockMetric portfolioStockMetric = portfolioStockMetricRepository
                .findTopByPortfolioIdAndStockIdOrderByDateDesc(portfolioId, stock.getStockId())
                .orElseThrow(() -> new Exception("Metrics not found"));

        Double oldQuantity = portfolioStockMetric.getQuantity();
        BigDecimal currentPrice = BigDecimal.valueOf(stock.getCurrentPrice());

        BigDecimal avgCost = portfolioStockMetric.getAverageCost();
        BigDecimal totalReturn;
        InvestmentActivity activity = new InvestmentActivity();
        if (transactionType.equals("BUY"))
        {
            avgCost = ((avgCost.multiply(BigDecimal.valueOf(oldQuantity)))
                    .add(currentPrice.multiply(BigDecimal.valueOf(quantity - oldQuantity))))
                    .divide(BigDecimal.valueOf(quantity), 2, BigDecimal.ROUND_HALF_UP);
            //avgCost = ((avgCost* * oldQuantity) + (currentPrice * quantityDiff)) / totalQuantity

            activity.setPortfolio(portfolio);
            activity.setStock(stock);
            activity.setStockQuantity(quantity-oldQuantity);
            activity.setDate(LocalDateTime.now());
            activity.setAmount(BigDecimal.valueOf(quantity * stock.getCurrentPrice()));
            activity.setActionType(InvestmentActivity.ActionType.BUY);
            activity.setOldPositionWeight(portfolioStockMetric.getPositionWeight());
        }
        else
        {
            activity.setPortfolio(portfolio);
            activity.setStock(stock);
            activity.setStockQuantity(oldQuantity-quantity);
            activity.setDate(LocalDateTime.now());
            activity.setAmount(BigDecimal.valueOf(quantity * stock.getCurrentPrice()));
            activity.setActionType(InvestmentActivity.ActionType.SELL);
            activity.setOldPositionWeight(portfolioStockMetric.getPositionWeight());
        }

        totalReturn = currentPrice.divide(avgCost, 2, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)).subtract(BigDecimal.valueOf(100));

        portfolioStockModel.setQuantity(quantity);
        portfolioStockRepository.save(portfolioStockModel);

        portfolioStockMetric.setQuantity(quantity);
        portfolioStockMetric.setAverageCost(avgCost);
        portfolioStockMetric.setCurrentValue(BigDecimal.valueOf(stock.getCurrentPrice()));
        portfolioStockMetric.setTotalReturn(totalReturn);
        portfolioStockMetric.setLastTransactionType(transactionType);
        portfolioStockMetric.setLastTransactionDate(Timestamp.from(Instant.now()).toLocalDateTime());
        portfolioStockMetric.setLastUpdated(Timestamp.from(Instant.now()).toLocalDateTime());
        portfolioStockMetricRepository.save(portfolioStockMetric);

        recalculatePositionWeights(portfolioId);
        activity.setNewPositionWeight(portfolioStockMetric.getPositionWeight());
        investmentActivityRepository.save(activity);

    }

    @Transactional
    public void removeStockFromPortfolio(Integer portfolioId, String tickerName) throws Exception
    {
        Portfolio portfolio = portfolioRepository.findById(Long.valueOf(portfolioId)).orElseThrow(() -> new Exception("Portfolio not found"));
        Stock stock = stockRepository.findByTickerSymbol(tickerName).orElseThrow(() -> new Exception("Stock not found"));
        PortfolioStockModel portfolioStock = portfolioStockRepository
                .findByPortfolio_PortfolioIdAndStock_StockId(portfolioId, stock.getStockId())
                .orElseThrow(() -> new Exception("Portfolio stock not found !"));
        PortfolioStockMetric portfolioStockMetric = portfolioStockMetricRepository.findTopByPortfolioIdAndStockIdOrderByDateDesc(portfolioId,stock.getStockId()).orElseThrow(() -> new Exception("Metrics not found"));

        boolean stockExistInPortfolioStock = portfolioStockRepository.existsByPortfolioAndStock(portfolio, stock);
        if (!stockExistInPortfolioStock)
        {
            throw new Exception("You do not have portfolio or stock");
        }
        InvestmentActivity activity = new InvestmentActivity();
        activity.setStock(stock);
        activity.setPortfolio(portfolio);
        activity.setStockQuantity(portfolioStock.getQuantity());
        activity.setDate(LocalDateTime.now());
        activity.setAmount(BigDecimal.valueOf(portfolioStock.getQuantity() * stock.getCurrentPrice()));
        activity.setOldPositionWeight(portfolioStockMetric.getPositionWeight());
        activity.setNewPositionWeight(BigDecimal.ZERO);

        investmentActivityRepository.save(activity);
        portfolioStockRepository.deleteByPortfolio_PortfolioIdAndStock_StockId(portfolioId, stock.getStockId());
        portfolioStockMetricRepository.deleteByPortfolioIdAndStockIdAndDate(portfolioId, stock.getStockId(), LocalDate.now().atStartOfDay());

        recalculatePositionWeights(portfolioId);
    }

    @Transactional
    public void recalculatePositionWeights(Integer portfolioId) throws Exception {
        LocalDateTime latestDate = portfolioStockMetricRepository.findLatestDateByPortfolioId(portfolioId);

        if (latestDate == null) {
            throw new Exception("No records found for the portfolio");
        }

        latestDate = latestDate.toLocalDate().atStartOfDay();  // Ensure the date is set to start of day with 00:00:00.000000

        List<PortfolioStockMetric> metrics = portfolioStockMetricRepository.findByPortfolioIdAndDate(portfolioId, latestDate);

        BigDecimal totalWeightedValue = metrics.stream()
                .map(metric -> metric.getAverageCost().multiply(BigDecimal.valueOf(metric.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalWeightedValue.compareTo(BigDecimal.ZERO) == 0) {
            for (PortfolioStockMetric metric : metrics) {
                metric.setPositionWeight(BigDecimal.ZERO);
                portfolioStockMetricRepository.save(metric);
            }
            return;
        }

        for (PortfolioStockMetric metric : metrics) {
            BigDecimal weightedValue = metric.getAverageCost().multiply(BigDecimal.valueOf(metric.getQuantity()));
            BigDecimal positionWeight = weightedValue.divide(totalWeightedValue, 2, BigDecimal.ROUND_HALF_UP);
            metric.setPositionWeight(positionWeight);
            portfolioStockMetricRepository.save(metric);
        }
    }

}
