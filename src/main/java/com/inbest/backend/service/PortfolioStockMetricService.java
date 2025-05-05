package com.inbest.backend.service;

import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.PortfolioStockModel;
import com.inbest.backend.model.User;
import com.inbest.backend.model.position.PortfolioStockMetric;
import com.inbest.backend.model.response.PortfolioStockResponse;
import com.inbest.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PortfolioStockMetricService
{
    private final AuthenticationService authenticationService;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioStockMetricRepository portfolioStockMetricRepository;
    private final PortfolioStockRepository portfolioStockRepository;
    private final PortfolioService portfolioService;
    private final UserRepository userRepository;
    private final InvestmentActivityRepository investmentActivityRepository;

    public List<Map<String, Object>> getStocksAndMetrics(int portfolioID) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Optional<User> user = userRepository.findByUsername(username);
            List<PortfolioStockModel> stocks = portfolioStockRepository.findByPortfolio_PortfolioId((long) portfolioID);
            return stocks.stream()
                    .map(stock -> {
                        Optional<PortfolioStockMetric> metricOpt = portfolioStockMetricRepository
                                .findTopByPortfolioIdAndStockIdOrderByDateDesc(portfolioID, stock.getStock().getStockId());

                        if (metricOpt.isPresent())
                        {
                            PortfolioStockMetric metric = metricOpt.get();
                            Map<String, Object> map = new HashMap<>();
                            map.put("stock_id", metric.getStockId());
                            map.put("symbol", stock.getStock().getTickerSymbol());
                            map.put("name", stock.getStock().getStockName());
                            map.put("shares", metric.getQuantity());
                            map.put("allocation", metric.getPositionWeight());
                            map.put("averageprice", metric.getAverageCost());
                            map.put("currentprice", metric.getCurrentValue());
                            map.put("return", metric.getTotalReturn());
                            Optional<LocalDateTime> openDate = investmentActivityRepository
                                    .findLatestOpenDateByPortfolioIdAndStockId(
                                            (long) portfolioID,
                                            Long.valueOf(stock.getStock().getStockId()));
                            map.put("open_date", openDate.orElse(null));
                            return map;
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .toList();
    }

    public List<Map<String, Object>> getDailyMetrics(int portfolioID) {
            return portfolioStockMetricRepository.findDailyMetricsByPortfolioId(portfolioID);
    }

}
