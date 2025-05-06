package com.inbest.backend.service;

import com.inbest.backend.dto.PortfolioReturnDTO;
import com.inbest.backend.model.*;
import com.inbest.backend.model.position.PortfolioMetric;
import com.inbest.backend.model.response.PortfolioReturnResponse;
import com.inbest.backend.model.response.PortfolioMetricResponse;
import com.inbest.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioMetricService {

    private final PortfolioMetricRepository portfolioMetricRepository;
    private final PortfolioMetricsWeightedReturnViewRepository portfolioMetricsWeightedReturnViewRepository;
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final PortfolioReturnsViewRepository portfolioReturnsViewRepository;

    /**
     * Retrieves portfolio metrics for a specific portfolio ID with visibility check.
     * If portfolio is public, returns metrics without user check.
     * If portfolio is private, checks if the current user is the owner of the portfolio.
     *
     * @param portfolioId The ID of the portfolio
     * @return List of metrics for the specified portfolio
     * @throws Exception if portfolio does not exist or user doesn't have access
     */
    public PortfolioMetricResponse getMetricsByPortfolioId(Integer portfolioId) throws Exception {
        // Get portfolio by ID
        Optional<Portfolio> portfolioOptional = portfolioRepository.findByPortfolioId(portfolioId);

        if (portfolioOptional.isEmpty()) {
            throw new Exception("Portfolio not found with ID: " + portfolioId);
        }

        Portfolio portfolio = portfolioOptional.get();
        List<PortfolioMetric> metrics;

        // Check visibility
        if ("private".equals(portfolio.getVisibility())) {
            // If portfolio is private, check user authorization
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            if (username.equals("anonymousUser")) {
                throw new IllegalArgumentException("Access denied for portfolio with ID: " + portfolioId);
            }
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Check if current user is the owner of the portfolio
            if (!portfolio.getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Access denied for portfolio with ID: " + portfolioId);
            }
        }
        // User is authorized, return metrics
        metrics = portfolioMetricRepository.findByPortfolioIdOrderByLastUpdatedDateDesc(portfolioId);

        if (metrics.isEmpty()) {
            return null;
        }

        // Get only the latest metric
        PortfolioMetric latestMetric = metrics.get(0);

        Optional<PortfolioMetricsWeightedReturnView> weightedMetricsOptional = portfolioMetricsWeightedReturnViewRepository.findByPortfolioId(portfolioId);
        PortfolioMetricResponse response = null;
        if (weightedMetricsOptional.isPresent())
        {
            PortfolioMetricsWeightedReturnView weightedMetrics = weightedMetricsOptional.get();

            response = PortfolioMetricResponse.builder()
                    .portfolioId(latestMetric.getPortfolioId())
                    .hourlyReturn(weightedMetrics.getHourlyReturn())
                    .dailyReturn(weightedMetrics.getDailyReturn())
                    .monthlyReturn(weightedMetrics.getMonthlyReturn())
                    .totalReturn(weightedMetrics.getTotalReturn())
                    .beta(latestMetric.getBeta())
                    .sharpeRatio(latestMetric.getSharpeRatio())
                    .volatility(latestMetric.getVolatility())
                    .riskScore(latestMetric.getRiskScore())
                    .riskCategory(latestMetric.getRiskCategory())
                    .build();
        }
        else {
            // Create a new PortfolioMetrics object without portfolioValue
            response = PortfolioMetricResponse.builder()
                    .portfolioId(latestMetric.getPortfolioId())
                    .hourlyReturn(latestMetric.getHourlyReturn())
                    .dailyReturn(latestMetric.getDailyReturn())
                    .monthlyReturn(latestMetric.getMonthlyReturn())
                    .totalReturn(latestMetric.getTotalReturn())
                    .beta(latestMetric.getBeta())
                    .sharpeRatio(latestMetric.getSharpeRatio())
                    .volatility(latestMetric.getVolatility())
                    .riskScore(latestMetric.getRiskScore())
                    .riskCategory(latestMetric.getRiskCategory())
                    .build();
        }
        return response;
    }

    public List<PortfolioReturnResponse> getWeeklyReturns(Integer portfolioId) throws Exception {
        // Get portfolio by ID
        Optional<Portfolio> portfolioOptional = portfolioRepository.findByPortfolioId(portfolioId);

        if (portfolioOptional.isEmpty()) {
            throw new Exception("Portfolio not found with ID: " + portfolioId);
        }

        Portfolio portfolio = portfolioOptional.get();
        LocalDateTime portfolioStartDate = portfolio.getCreatedDate();
        LocalDateTime endDate = LocalDateTime.now();

        // Get portfolio metrics for the last 7 days
        List<PortfolioMetric> portfolioMetrics = portfolioMetricRepository.findByPortfolioIdAndDateBetweenOrderByLastUpdatedDateAsc(
                portfolioId, endDate.minusDays(7), endDate);
        if (portfolioMetrics.isEmpty()) {
            throw new Exception("No metrics found for portfolio with ID: " + portfolioId);
        }

        // Get GC=F and SPY stock IDs
        Optional<Stock> goldStock = stockRepository.findByTickerSymbol("GC=F");
        Optional<Stock> spyStock = stockRepository.findByTickerSymbol("SPY");

        if (goldStock.isEmpty() || spyStock.isEmpty()) {
            throw new Exception("Required stocks (GC=F or SPY) not found");
        }

        // Get daily returns for GC=F and SPY
        List<Map<String, Object>> goldReturns = stockPriceRepository.findDailyReturnsByTickerAndDateRange(
                "GC=F", endDate.minusDays(7), endDate);
        List<Map<String, Object>> spyReturns = stockPriceRepository.findDailyReturnsByTickerAndDateRange(
                "SPY", endDate.minusDays(7), endDate);

        // Combine the data
        List<PortfolioReturnResponse> response = new ArrayList<>();
        for (PortfolioMetric metric : portfolioMetrics) {
            PortfolioReturnResponse dailyReturn = new PortfolioReturnResponse();
            dailyReturn.setDate(metric.getLastUpdatedDate());
            dailyReturn.setPortfolioReturn(metric.getDailyReturn());

            // Find matching GC=F return for the same day
            Optional<Map<String, Object>> goldReturn = goldReturns.stream()
                    .filter(r -> {
                        java.sql.Timestamp timestamp = (java.sql.Timestamp) r.get("date");
                        LocalDateTime returnDate = timestamp.toLocalDateTime();
                        return returnDate.toLocalDate().equals(metric.getLastUpdatedDate().toLocalDate());
                    })
                    .findFirst();
            goldReturn.ifPresent(r -> dailyReturn.setGoldReturn(new BigDecimal(r.get("daily_return").toString())));

            // Find matching SPY return for the same day
            Optional<Map<String, Object>> spyReturn = spyReturns.stream()
                    .filter(r -> {
                        java.sql.Timestamp timestamp = (java.sql.Timestamp) r.get("date");
                        LocalDateTime returnDate = timestamp.toLocalDateTime();
                        return returnDate.toLocalDate().equals(metric.getLastUpdatedDate().toLocalDate());
                    })
                    .findFirst();
            spyReturn.ifPresent(r -> dailyReturn.setSpyReturn(new BigDecimal(r.get("daily_return").toString())));

            response.add(dailyReturn);
        }

        return response;
    }

    public List<PortfolioReturnResponse> getMonthlyReturns(Integer portfolioId) throws Exception {
        // Get portfolio by ID
        Optional<Portfolio> portfolioOptional = portfolioRepository.findByPortfolioId(portfolioId);

        if (portfolioOptional.isEmpty()) {
            throw new Exception("Portfolio not found with ID: " + portfolioId);
        }

        Portfolio portfolio = portfolioOptional.get();
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(1);

        // Get portfolio metrics for the last month
        List<PortfolioMetric> portfolioMetrics = portfolioMetricRepository.findByPortfolioIdAndDateBetweenOrderByLastUpdatedDateAsc(
                portfolioId, startDate, endDate);
        if (portfolioMetrics.isEmpty()) {
            throw new Exception("No metrics found for portfolio with ID: " + portfolioId);
        }

        // Get GC=F and SPY stock IDs
        Optional<Stock> goldStock = stockRepository.findByTickerSymbol("GC=F");
        Optional<Stock> spyStock = stockRepository.findByTickerSymbol("SPY");

        if (goldStock.isEmpty() || spyStock.isEmpty()) {
            throw new Exception("Required stocks (GC=F or SPY) not found");
        }

        // Get daily returns for GC=F and SPY
        List<Map<String, Object>> goldReturns = stockPriceRepository.findDailyReturnsByTickerAndDateRange(
                "GC=F", startDate, endDate);
        List<Map<String, Object>> spyReturns = stockPriceRepository.findDailyReturnsByTickerAndDateRange(
                "SPY", startDate, endDate);

        // Combine the data
        List<PortfolioReturnResponse> response = new ArrayList<>();
        for (PortfolioMetric metric : portfolioMetrics) {
            PortfolioReturnResponse dailyReturn = new PortfolioReturnResponse();
            dailyReturn.setDate(metric.getLastUpdatedDate());
            dailyReturn.setPortfolioReturn(metric.getDailyReturn());

            // Find matching GC=F return for the same day
            Optional<Map<String, Object>> goldReturn = goldReturns.stream()
                    .filter(r -> {
                        java.sql.Timestamp timestamp = (java.sql.Timestamp) r.get("date");
                        LocalDateTime returnDate = timestamp.toLocalDateTime();
                        return returnDate.toLocalDate().equals(metric.getLastUpdatedDate().toLocalDate());
                    })
                    .findFirst();
            goldReturn.ifPresent(r -> dailyReturn.setGoldReturn(new BigDecimal(r.get("daily_return").toString())));

            // Find matching SPY return for the same day
            Optional<Map<String, Object>> spyReturn = spyReturns.stream()
                    .filter(r -> {
                        java.sql.Timestamp timestamp = (java.sql.Timestamp) r.get("date");
                        LocalDateTime returnDate = timestamp.toLocalDateTime();
                        return returnDate.toLocalDate().equals(metric.getLastUpdatedDate().toLocalDate());
                    })
                    .findFirst();
            spyReturn.ifPresent(r -> dailyReturn.setSpyReturn(new BigDecimal(r.get("daily_return").toString())));

            response.add(dailyReturn);
        }

        return response;
    }

    public List<PortfolioReturnDTO> getPortfolioReturns(Integer portfolioId) {
        // 1. Portföy verilerini al
        List<Object[]> rows = portfolioReturnsViewRepository.findPortfolioReturns(portfolioId);

        if (rows.isEmpty()) {
            return Collections.emptyList(); // Hiç veri yoksa boş dön
        }

        // 2. DTO'ya dönüştür ve sırala
        List<PortfolioReturnDTO> allData = rows.stream()
                .map(row -> new PortfolioReturnDTO(
                        ((Timestamp) row[1]).toLocalDateTime(),
                        ((BigDecimal) row[2])
                ))
                .sorted(Comparator.comparing(PortfolioReturnDTO::getDate))
                .collect(Collectors.toList());

        // 3. Portföyün başlangıç tarihini belirle
        LocalDateTime portfolioStartDate = allData.get(0).getDate();

        // 4. SPY ve GOLD başlangıç fiyatlarını al
        BigDecimal spyRefPrice = getStockPriceAtDate("SPY", portfolioStartDate);
        BigDecimal goldRefPrice = getStockPriceAtDate("GC=F", portfolioStartDate);

        // 5. SPY ve GOLD normalize edilmiş verileri hesapla ve DTO listesine ekle
        List<PortfolioReturnDTO> finalData = allData.stream()
                .map(dto -> {
                    BigDecimal spyReturn = getNormalizedReturn("SPY", dto.getDate(), spyRefPrice);
                    BigDecimal goldReturn = getNormalizedReturn("GC=F", dto.getDate(), goldRefPrice);
                    return new PortfolioReturnDTO(dto.getDate(), dto.getPortfolioReturn(), spyReturn, goldReturn);
                })
                .collect(Collectors.toList());

        // 6. Nokta sayısını düşür ve döndür
        return reducePoints(finalData, 10, 14);
    }

    private BigDecimal getStockPriceAtDate(String tickerSymbol, LocalDateTime date) {
        StockPrice stockPrice = stockPriceRepository.findTopByTickerSymbolAndDateBeforeOrderByDateDesc(tickerSymbol, date);
        return stockPrice != null ? stockPrice.getPrice() : BigDecimal.ONE; // 0 değil → 1 dön ki bölmede patlamasın
    }

    private BigDecimal getNormalizedReturn(String tickerSymbol, LocalDateTime date, BigDecimal refPrice) {
        StockPrice stockPrice = stockPriceRepository.findTopByTickerSymbolAndDateBeforeOrderByDateDesc(tickerSymbol, date);
        if (stockPrice == null || refPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100); // Eğer veri yoksa 100 dön, çizgi düz gider
        }

        return stockPrice.getPrice()
                .divide(refPrice, 6, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private List<PortfolioReturnDTO> reducePoints(List<PortfolioReturnDTO> data, int minPoints, int maxPoints) {
        int totalCount = data.size();
        if (totalCount <= minPoints) {
            return data;
        }

        int targetCount = Math.min(totalCount, maxPoints);
        List<PortfolioReturnDTO> reduced = new ArrayList<>();
        double step = (double) (totalCount - 1) / (targetCount - 1);

        for (int i = 0; i < targetCount; i++) {
            int index = (int) Math.round(i * step);
            reduced.add(data.get(index));
        }

        return reduced;
    }


}
