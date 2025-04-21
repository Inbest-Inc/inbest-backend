package com.inbest.backend.service;


import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.TradeMetrics;
import com.inbest.backend.model.User;
import com.inbest.backend.repository.PortfolioRepository;
import com.inbest.backend.repository.TradeMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class TradeMetricsService {
    private final TradeMetricsRepository tradeMetricsRepository;
    private final PortfolioRepository portfolioRepository;
    private final UserService userService;


    public TradeMetrics getPortfolioBestTrade(Integer portfolioId) {
        TradeMetrics bestTrade = tradeMetricsRepository.findTopByPortfolioIdAndIsBestTradeTrue(portfolioId);
        if (bestTrade != null) {
            return bestTrade;
        } else {
            throw new NoSuchElementException("No best trade found for portfolio with ID: " + portfolioId);
        }
    }

    public TradeMetrics getPortfolioWorstTrade(Integer portfolioId) {
        TradeMetrics worstTrade = tradeMetricsRepository.findTopByPortfolioIdAndIsWorstTradeTrue(portfolioId);
        if (worstTrade != null) {
            return worstTrade;
        } else {
            throw new NoSuchElementException("No worst trade found for portfolio with ID: " + portfolioId);
        }
    }

    public TradeMetrics getLatestTrade(){
        User user = userService.getCurrentUser();
        List<Portfolio> portfolios = portfolioRepository.findByUser(user);
        if (portfolios.isEmpty()) {
            throw new NoSuchElementException("User has no portfolios.");
        }

        List<Integer> portfolioIds = portfolios.stream()
                .map(Portfolio::getPortfolioId)
                .toList();

        TradeMetrics latestTrade = tradeMetricsRepository
                .findTopByPortfolioIdInOrderByExitDateDesc(portfolioIds);

        if (latestTrade == null) {
            throw new NoSuchElementException("No trade metrics found for user portfolios.");
        }

        return latestTrade;
    }
    public TradeMetrics getUserBestTrade() {
        User user = userService.getCurrentUser();

        List<Portfolio> portfolios = portfolioRepository.findByUser(user);
        if (portfolios.isEmpty()) {
            throw new NoSuchElementException("User has no portfolios.");
        }

        List<Integer> portfolioIds = portfolios.stream()
                .map(Portfolio::getPortfolioId)
                .toList();

        List<TradeMetrics> bestTrades = tradeMetricsRepository
                .findByPortfolioIdInAndIsBestTradeTrue(portfolioIds);

        if (bestTrades.isEmpty()) {
            throw new NoSuchElementException("No best trades found for user portfolios.");
        }

        return bestTrades.stream()
                .max(Comparator.comparing(TradeMetrics::getTotalReturn))
                .orElseThrow();
    }
    public TradeMetrics getUserWorstTrade() {
        User user = userService.getCurrentUser();

        List<Portfolio> portfolios = portfolioRepository.findByUser(user);
        if (portfolios.isEmpty()) {
            throw new NoSuchElementException("User has no portfolios.");
        }

        List<Integer> portfolioIds = portfolios.stream()
                .map(Portfolio::getPortfolioId)
                .toList();

        List<TradeMetrics> worstTrades = tradeMetricsRepository
                .findByPortfolioIdInAndIsWorstTradeTrue(portfolioIds);

        if (worstTrades.isEmpty()) {
            throw new NoSuchElementException("No worst trades found for user portfolios.");
        }

        return worstTrades.stream()
                .min(Comparator.comparing(TradeMetrics::getTotalReturn).thenComparing(TradeMetrics::getExitDate, Comparator.reverseOrder()))
                .orElseThrow();
    }

}