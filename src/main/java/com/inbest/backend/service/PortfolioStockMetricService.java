package com.inbest.backend.service;

import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.PortfolioStockModel;
import com.inbest.backend.model.User;
import com.inbest.backend.model.response.PortfolioStockResponse;
import com.inbest.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

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

    public List<Map<String, Object>> getStocksAndMetrics(int portfolioID) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Optional<User> user = userRepository.findByUsername(username);
        if(user.isPresent())
        {
            if (portfolioService.checkPortfolioOwnership(portfolioID, user.get().getId()))
            {
                List<PortfolioStockModel> stocks = portfolioStockRepository.findByPortfolio_PortfolioId((long) portfolioID);
                return stocks.stream()
                        .map(stock -> portfolioStockMetricRepository.findTopByPortfolioIdAndStockIdOrderByDateDesc(portfolioID, stock.getStock().getStockId())
                                .map(metric -> {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("stock_id", metric.getStockId());
                                    map.put("symbol", stock.getStock().getTickerSymbol());
                                    map.put("name", stock.getStock().getStockName());
                                    map.put("shares", metric.getQuantity());
                                    map.put("allocation", metric.getPositionWeight());
                                    map.put("averageprice", metric.getAverageCost());
                                    map.put("currentprice", metric.getCurrentValue());
                                    map.put("return", metric.getTotalReturn());
                                    return map;
                                })
                                .orElse(null))
                        .filter(metric -> metric != null)
                        .toList();
            }
        }
        return portfolioStockMetricRepository.findMetricsByPortfolioIdIfPublic(portfolioID);
    }
}
