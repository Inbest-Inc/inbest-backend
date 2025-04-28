package com.inbest.backend.service;

import com.inbest.backend.dto.InvestmentActivityResponseDTO;
import com.inbest.backend.model.*;
import com.inbest.backend.model.position.PortfolioStockMetric;
import com.inbest.backend.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;


@Service
public class PortfolioStockService
{

    private final PortfolioStockRepository portfolioStockRepository;
    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;
    private final PortfolioStockMetricRepository portfolioStockMetricRepository;
    private final InvestmentActivityRepository investmentActivityRepository;
    private final TradeMetricsRepository tradeMetricsRepository;


    public PortfolioStockService(PortfolioStockRepository portfolioStockRepository, PortfolioRepository portfolioRepository, StockRepository stockRepository, PortfolioStockMetricRepository portfolioStockMetricRepository, InvestmentActivityRepository investmentActivityRepository, TradeMetricsRepository tradeMetricsRepository)
    {
        this.portfolioStockRepository = portfolioStockRepository;
        this.portfolioRepository = portfolioRepository;
        this.stockRepository = stockRepository;
        this.portfolioStockMetricRepository = portfolioStockMetricRepository;
        this.investmentActivityRepository = investmentActivityRepository;
        this.tradeMetricsRepository = tradeMetricsRepository;
    }


    public InvestmentActivityResponseDTO addStockToPortfolio(Integer portfolioId, String tickerName, Double quantity) throws Exception
    {
        LocalDateTime latestDate = portfolioStockMetricRepository.findLatestDateByPortfolioId(portfolioId);

        if (latestDate == null)
        {
            // Şu anki zaman (ET timezone'unda)
            ZonedDateTime nowET = ZonedDateTime.now(ZoneId.of("America/New_York"));

            LocalTime currentTime = nowET.toLocalTime();
            LocalDate currentDate = nowET.toLocalDate();

            // Borsa açık mı? 09:30 <= now < 16:00
            LocalTime marketOpen = LocalTime.of(9, 30);
            LocalTime marketClose = LocalTime.of(16, 0);

            if (!nowET.getDayOfWeek().equals(DayOfWeek.SATURDAY) && !nowET.getDayOfWeek().equals(DayOfWeek.SUNDAY)
                    && (currentTime.isAfter(marketOpen) && currentTime.isBefore(marketClose)))
            {
                // Borsa açık -> Saatin başına yuvarla (örnek 16:23 -> 16:00)
                LocalDateTime roundedHour = nowET.truncatedTo(ChronoUnit.HOURS).toLocalDateTime();
                latestDate = roundedHour;
            }
            else
            {
                // Borsa kapalı -> Bir önceki iş gününün 17:00'ı

                LocalDate previousBusinessDay = currentDate.minusDays(1);

                // Eğer önceki gün cumartesi ise, cuma gününe git
                if (previousBusinessDay.getDayOfWeek() == DayOfWeek.SUNDAY)
                {
                    previousBusinessDay = previousBusinessDay.minusDays(2);
                }
                else if (previousBusinessDay.getDayOfWeek() == DayOfWeek.SATURDAY)
                {
                    previousBusinessDay = previousBusinessDay.minusDays(1);
                }

                latestDate = LocalDateTime.of(previousBusinessDay, LocalTime.of(17, 0));
            }
        }

        Portfolio portfolio = portfolioRepository.findById(Long.valueOf(portfolioId)).orElseThrow(() -> new Exception("Portfolio not found"));

        Stock stock = stockRepository.findByTickerSymbol(tickerName).orElseThrow(() -> new Exception("Stock not found"));

        boolean stockExistsInPortfolio = portfolioStockRepository.existsByPortfolioAndStock(portfolio, stock);
        if (stockExistsInPortfolio)
        {
            throw new Exception("Stock is already added to the portfolio");
        }

        if (quantity <= 0)
        {
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
        activity.setActionType(InvestmentActivity.ActionType.OPEN);
        activity.setOldPositionWeight(BigDecimal.ZERO);


        portfolioStockRepository.save(portfolioStockModel);

        PortfolioStockMetric portfolioStockMetric = PortfolioStockMetric.builder()
                .portfolioId(portfolioId)
                .stockId(stock.getStockId())
                .date(latestDate)
                .quantity(quantity)
                .averageCost(BigDecimal.valueOf(stock.getCurrentPrice()))
                .currentValue(BigDecimal.valueOf(stock.getCurrentPrice()))
                .totalReturn(BigDecimal.valueOf(0))
                .positionWeight(BigDecimal.valueOf(0))
                .lastTransactionType("OPEN")
                .lastUpdated(Timestamp.from(Instant.now()).toLocalDateTime())
                .build();

        portfolioStockMetricRepository.save(portfolioStockMetric);
        PortfolioStockMetric portfolioStockMetricNew = portfolioStockMetricRepository.findTopByPortfolioIdAndStockIdOrderByDateDesc(portfolioId, stock.getStockId()).orElseThrow(() -> new Exception("Metrics not found"));

        recalculatePositionWeights(portfolioId);
        activity.setNewPositionWeight(portfolioStockMetricNew.getPositionWeight());
        investmentActivityRepository.save(activity);
        return convertToResponseDTO(activity);
    }

    @Transactional
    public InvestmentActivityResponseDTO updateQuantity(Integer portfolioId, String tickerName, Double quantity) throws Exception
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
            activity.setStockQuantity(quantity - oldQuantity);
            activity.setDate(LocalDateTime.now());
            activity.setActionType(InvestmentActivity.ActionType.BUY);
            activity.setOldPositionWeight(portfolioStockMetric.getPositionWeight());
        }
        else
        {
            activity.setPortfolio(portfolio);
            activity.setStock(stock);
            activity.setStockQuantity(oldQuantity - quantity);
            activity.setDate(LocalDateTime.now());
            activity.setActionType(InvestmentActivity.ActionType.SELL);
            activity.setOldPositionWeight(portfolioStockMetric.getPositionWeight());
            recordTradeOnSell(portfolioId, stock.getStockId(), oldQuantity - quantity, avgCost, BigDecimal.valueOf(stock.getCurrentPrice()));

        }

        totalReturn = currentPrice.divide(avgCost, 2, BigDecimal.ROUND_HALF_UP).subtract(BigDecimal.ONE);

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
        PortfolioStockMetric newPortfolioStockMetric = portfolioStockMetricRepository
                .findTopByPortfolioIdAndStockIdOrderByDateDesc(portfolioId, stock.getStockId())
                .orElseThrow(() -> new Exception("Metrics not found"));
        activity.setNewPositionWeight(newPortfolioStockMetric.getPositionWeight());
        investmentActivityRepository.save(activity);
        return convertToResponseDTO(activity);

    }

    @Transactional
    public InvestmentActivityResponseDTO removeStockFromPortfolio(Integer portfolioId, String tickerName) throws Exception
    {
        LocalDateTime latestDate = portfolioStockMetricRepository.findLatestDateByPortfolioId(portfolioId);

        Portfolio portfolio = portfolioRepository.findById(Long.valueOf(portfolioId)).orElseThrow(() -> new Exception("Portfolio not found"));
        Stock stock = stockRepository.findByTickerSymbol(tickerName).orElseThrow(() -> new Exception("Stock not found"));
        PortfolioStockModel portfolioStock = portfolioStockRepository
                .findByPortfolio_PortfolioIdAndStock_StockId(portfolioId, stock.getStockId())
                .orElseThrow(() -> new Exception("Portfolio stock not found !"));
        PortfolioStockMetric portfolioStockMetric = portfolioStockMetricRepository.findTopByPortfolioIdAndStockIdOrderByDateDesc(portfolioId, stock.getStockId()).orElseThrow(() -> new Exception("Metrics not found"));

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
        activity.setActionType(InvestmentActivity.ActionType.CLOSE);
        activity.setOldPositionWeight(portfolioStockMetric.getPositionWeight());
        activity.setNewPositionWeight(BigDecimal.ZERO);

        investmentActivityRepository.save(activity);
        portfolioStockRepository.deleteByPortfolio_PortfolioIdAndStock_StockId(portfolioId, stock.getStockId());
        portfolioStockMetricRepository.deleteByPortfolioIdAndStockId(portfolioId, stock.getStockId());

        recordTradeOnSell(portfolioId, stock.getStockId(), portfolioStockMetric.getQuantity(), portfolioStockMetric.getAverageCost(), BigDecimal.valueOf(stock.getCurrentPrice()));
        recalculatePositionWeights(portfolioId);
        return convertToResponseDTO(activity);
    }

    @Transactional
    public void recalculatePositionWeights(Integer portfolioId) throws Exception
    {
        LocalDateTime latestDate = portfolioStockMetricRepository.findLatestDateByPortfolioId(portfolioId);

        if (latestDate == null)
        {
            throw new Exception("No records found for the portfolio");
        }

        List<PortfolioStockMetric> metrics = portfolioStockMetricRepository.findByPortfolioIdAndDate(portfolioId, latestDate);

        BigDecimal totalWeightedValue = metrics.stream()
                .map(metric -> metric.getAverageCost().multiply(BigDecimal.valueOf(metric.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalWeightedValue.compareTo(BigDecimal.ZERO) == 0)
        {
            for (PortfolioStockMetric metric : metrics)
            {
                metric.setPositionWeight(BigDecimal.ZERO);
                portfolioStockMetricRepository.save(metric);
            }
            return;
        }

        for (PortfolioStockMetric metric : metrics)
        {
            BigDecimal weightedValue = metric.getAverageCost().multiply(BigDecimal.valueOf(metric.getQuantity()));
            BigDecimal positionWeight = weightedValue.divide(totalWeightedValue, 2, BigDecimal.ROUND_HALF_UP);
            metric.setPositionWeight(positionWeight);
            portfolioStockMetricRepository.save(metric);
        }
    }

    private InvestmentActivityResponseDTO convertToResponseDTO(InvestmentActivity activity)
    {
        return new InvestmentActivityResponseDTO(
                activity.getActivityId(),
                activity.getPortfolio().getPortfolioId(),
                activity.getStock().getStockId(),
                activity.getStock().getTickerSymbol(),
                activity.getStock().getStockName(),
                activity.getActionType().name(),
                activity.getStockQuantity(),
                activity.getDate(),
                activity.getOldPositionWeight(),
                activity.getNewPositionWeight()
        );
    }

    private void recordTradeOnSell(Integer portfolioId, Integer stockId, Double sellQuantity, BigDecimal averageCost, BigDecimal currentPrice) throws Exception
    {
        BigDecimal totalCost = averageCost.multiply(BigDecimal.valueOf(sellQuantity));
        BigDecimal totalRevenue = currentPrice.multiply(BigDecimal.valueOf(sellQuantity));

        BigDecimal returnPercentage = BigDecimal.ZERO;

        if (totalCost.compareTo(BigDecimal.ZERO) > 0)
        {
            returnPercentage = totalRevenue.subtract(totalCost)
                    .divide(totalCost, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        LocalDateTime entryDate = investmentActivityRepository.findTopByPortfolio_PortfolioIdAndStock_StockIdAndActionTypeOrderByDateDesc(portfolioId, stockId, InvestmentActivity.ActionType.OPEN).orElseThrow(() -> new Exception("Investment activity not found")).getDate();
        TradeMetrics trade = TradeMetrics.builder()
                .portfolioId(portfolioId)
                .stockId(stockId)
                .averageCost(averageCost)
                .entryDate(entryDate)
                .exitPrice(currentPrice)
                .exitDate(LocalDateTime.now())
                .quantity(sellQuantity.intValue())
                .totalReturn(returnPercentage)
                .isBestTrade(false)
                .isWorstTrade(false)
                .build();

        tradeMetricsRepository.save(trade);
        updateBestAndWorstTrade(portfolioId);
    }

    public void updateBestAndWorstTrade(Integer portfolioId)
    {
        List<TradeMetrics> trades = tradeMetricsRepository.findByPortfolioId(portfolioId);

        if (trades.isEmpty())
        {
            return;
        }

        for (TradeMetrics trade : trades)
        {
            trade.setIsBestTrade(false);
            trade.setIsWorstTrade(false);
        }

        Comparator<TradeMetrics> bestTradeComparator = Comparator
                .comparing(TradeMetrics::getTotalReturn)
                .thenComparing(TradeMetrics::getExitDate);

        Comparator<TradeMetrics> worstTradeComparator = Comparator
                .comparing(TradeMetrics::getTotalReturn)
                .thenComparing(TradeMetrics::getExitDate, Comparator.reverseOrder());

        TradeMetrics bestTrade = trades.stream()
                .max(bestTradeComparator)
                .orElse(null);

        TradeMetrics worstTrade = trades.stream()
                .min(worstTradeComparator)
                .orElse(null);

        if (bestTrade != null)
        {
            bestTrade.setIsBestTrade(true);
        }
        if (worstTrade != null)
        {
            worstTrade.setIsWorstTrade(true);
        }

        tradeMetricsRepository.saveAll(trades);
    }

}
