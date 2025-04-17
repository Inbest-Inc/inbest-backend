package com.inbest.backend.service;

import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.PortfolioStockModel;
import com.inbest.backend.model.Stock;
import com.inbest.backend.model.TradeMetrics;
import com.inbest.backend.model.position.PortfolioStockMetric;
import com.inbest.backend.model.response.PortfolioStockResponse;
import com.inbest.backend.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class PortfolioStockService
{

    private final PortfolioStockRepository portfolioStockRepository;
    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;
    private final PortfolioStockMetricRepository portfolioStockMetricRepository;
    private final TradeMetricsRepository tradeMetricsRepository;



    public PortfolioStockService(PortfolioStockRepository portfolioStockRepository, PortfolioRepository portfolioRepository, StockRepository stockRepository, PortfolioStockMetricRepository portfolioStockMetricRepository, TradeMetricsRepository tradeMetricsRepository)
    {
        this.portfolioStockRepository = portfolioStockRepository;
        this.portfolioRepository = portfolioRepository;
        this.stockRepository = stockRepository;
        this.portfolioStockMetricRepository = portfolioStockMetricRepository;
        this.tradeMetricsRepository = tradeMetricsRepository;
    }

    @Transactional
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

        recalculatePositionWeights(portfolioId);
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
                .findByPortfolioIdAndStockId(portfolioId, stock.getStockId())
                .orElseThrow(() -> new Exception("Metrics not found"));

        Double oldQuantity = portfolioStockMetric.getQuantity();
        BigDecimal currentPrice = BigDecimal.valueOf(stock.getCurrentPrice());

        BigDecimal avgCost = portfolioStockMetric.getAverageCost();
        BigDecimal totalReturn;

        if (transactionType.equals("BUY"))
        {
            avgCost = ((avgCost.multiply(BigDecimal.valueOf(oldQuantity)))
                    .add(currentPrice.multiply(BigDecimal.valueOf(quantity - oldQuantity))))
                    .divide(BigDecimal.valueOf(quantity), 2, BigDecimal.ROUND_HALF_UP);
            //avgCost = ((avgCost* * oldQuantity) + (currentPrice * quantityDiff)) / totalQuantity
        }
        else
        {
            recordTradeOnSell(portfolioId, stock.getStockId(), oldQuantity - quantity,avgCost,BigDecimal.valueOf(stock.getCurrentPrice()));
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

    }

    @Transactional
    public void removeStockFromPortfolio(Integer portfolioId, String tickerName) throws Exception
    {
        Portfolio portfolio = portfolioRepository.findById(Long.valueOf(portfolioId)).orElseThrow(() -> new Exception("Portfolio not found"));

        Stock stock = stockRepository.findByTickerSymbol(tickerName).orElseThrow(() -> new Exception("Stock not found"));

        boolean stockExistInPortfolioStock = portfolioStockRepository.existsByPortfolioAndStock(portfolio, stock);
        if (!stockExistInPortfolioStock)
        {
            throw new Exception("You do not have portfolio or stock");
        }

        PortfolioStockMetric portfolioStockMetric = portfolioStockMetricRepository
                .findByPortfolioIdAndStockId(portfolioId, stock.getStockId())
                .orElseThrow(() -> new Exception("Metrics not found"));
        BigDecimal avgCost = portfolioStockMetric.getAverageCost();

        recordTradeOnSell(portfolioId, stock.getStockId(), portfolioStockMetric.getQuantity(),avgCost,BigDecimal.valueOf(stock.getCurrentPrice()));
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

    private void recordTradeOnSell(
            Integer portfolioId,
            Integer stockId,
            Double sellQuantity,
            BigDecimal averageCost,
            BigDecimal currentPrice
    ) {
        BigDecimal totalCost = averageCost.multiply(BigDecimal.valueOf(sellQuantity));
        BigDecimal totalRevenue = currentPrice.multiply(BigDecimal.valueOf(sellQuantity));

        BigDecimal returnPercentage = BigDecimal.ZERO;

        if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
            returnPercentage = totalRevenue.subtract(totalCost)
                    .divide(totalCost, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        TradeMetrics trade = TradeMetrics.builder()
                .portfolioId(portfolioId)
                .stockId(stockId)
                .entryPrice(averageCost)
                .exitPrice(currentPrice)
                .entryDate(LocalDateTime.now())
                .exitDate(LocalDateTime.now())
                .quantity(sellQuantity.intValue())
                .totalReturn(returnPercentage)
                .isBestTrade(false)
                .isWorstTrade(false)
                .build();

        tradeMetricsRepository.save(trade);
        updateBestAndWorstTrade(portfolioId);
    }

    public void updateBestAndWorstTrade(Integer portfolioId) {
        List<TradeMetrics> trades = tradeMetricsRepository.findByPortfolioId(portfolioId);

        if (trades.isEmpty()) return;

        for (TradeMetrics trade : trades) {
            trade.setIsBestTrade(false);
            trade.setIsWorstTrade(false);
        }

        TradeMetrics bestTrade = trades.stream()
                .max(Comparator.comparing(TradeMetrics::getTotalReturn))
                .orElse(null);

        TradeMetrics worstTrade = trades.stream()
                .min(Comparator.comparing(TradeMetrics::getTotalReturn))
                .orElse(null);

        if (bestTrade != null) bestTrade.setIsBestTrade(true);
        if (worstTrade != null) worstTrade.setIsWorstTrade(true);

        tradeMetricsRepository.saveAll(trades);
    }
}
