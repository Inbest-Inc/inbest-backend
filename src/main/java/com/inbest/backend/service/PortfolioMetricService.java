package com.inbest.backend.service;

import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.PortfolioMetricsWeightedReturnView;
import com.inbest.backend.model.Stock;
import com.inbest.backend.model.User;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PortfolioMetricService {

    private final PortfolioMetricRepository portfolioMetricRepository;
    private final PortfolioMetricsWeightedReturnViewRepository portfolioMetricsWeightedReturnViewRepository;
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;

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

    public List<PortfolioReturnResponse> getYearlyReturns(Integer portfolioId) throws Exception {
        // Get portfolio by ID
        Optional<Portfolio> portfolioOptional = portfolioRepository.findByPortfolioId(portfolioId);

        if (portfolioOptional.isEmpty()) {
            throw new Exception("Portfolio not found with ID: " + portfolioId);
        }

        Portfolio portfolio = portfolioOptional.get();
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusYears(1);

        // Get portfolio metrics for the last year
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

        // Get monthly returns for GC=F and SPY
        List<Map<String, Object>> goldReturns = stockPriceRepository.findMonthlyReturnsByTickerAndDateRange(
                "GC=F", startDate, endDate);
        List<Map<String, Object>> spyReturns = stockPriceRepository.findMonthlyReturnsByTickerAndDateRange(
                "SPY", startDate, endDate);

        // Group metrics by month and get the latest metric for each month
        Map<String, PortfolioMetric> monthlyMetrics = new HashMap<>();
        for (PortfolioMetric metric : portfolioMetrics) {
            String monthKey = metric.getLastUpdatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            if (!monthlyMetrics.containsKey(monthKey) ||
                    metric.getLastUpdatedDate().isAfter(monthlyMetrics.get(monthKey).getLastUpdatedDate())) {
                monthlyMetrics.put(monthKey, metric);
            }
        }

        // Combine the data
        List<PortfolioReturnResponse> response = new ArrayList<>();
        List<String> sortedMonths = new ArrayList<>(monthlyMetrics.keySet());
        Collections.sort(sortedMonths);

        for (String monthKey : sortedMonths) {
            PortfolioMetric metric = monthlyMetrics.get(monthKey);
            PortfolioReturnResponse monthlyReturn = new PortfolioReturnResponse();
            monthlyReturn.setDate(metric.getLastUpdatedDate());
            monthlyReturn.setPortfolioReturn(metric.getMonthlyReturn());

            // Find matching GC=F return for the same month
            Optional<Map<String, Object>> goldReturn = goldReturns.stream()
                    .filter(r -> {
                        java.sql.Timestamp timestamp = (java.sql.Timestamp) r.get("date");
                        LocalDateTime returnDate = timestamp.toLocalDateTime();
                        return returnDate.getYear() == metric.getLastUpdatedDate().getYear() &&
                                returnDate.getMonth() == metric.getLastUpdatedDate().getMonth();
                    })
                    .findFirst();
            goldReturn.ifPresent(r -> monthlyReturn.setGoldReturn(new BigDecimal(r.get("monthly_return").toString())));

            // Find matching SPY return for the same month
            Optional<Map<String, Object>> spyReturn = spyReturns.stream()
                    .filter(r -> {
                        java.sql.Timestamp timestamp = (java.sql.Timestamp) r.get("date");
                        LocalDateTime returnDate = timestamp.toLocalDateTime();
                        return returnDate.getYear() == metric.getLastUpdatedDate().getYear() &&
                                returnDate.getMonth() == metric.getLastUpdatedDate().getMonth();
                    })
                    .findFirst();
            spyReturn.ifPresent(r -> monthlyReturn.setSpyReturn(new BigDecimal(r.get("monthly_return").toString())));

            response.add(monthlyReturn);
        }

        return response;
    }

}
