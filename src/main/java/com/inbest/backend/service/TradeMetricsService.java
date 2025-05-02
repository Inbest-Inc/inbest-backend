package com.inbest.backend.service;

import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.TradeMetrics;
import com.inbest.backend.model.User;
import com.inbest.backend.repository.PortfolioRepository;
import com.inbest.backend.repository.TradeMetricsRepository;
import com.inbest.backend.model.response.TradeMetricsResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class TradeMetricsService {
    private final TradeMetricsRepository tradeMetricsRepository;
    private final PortfolioRepository portfolioRepository;
    private final UserService userService;

    private List<Integer> getCurrentUserPortfolioIds() {
        User user = userService.getCurrentUser();
        List<Portfolio> portfolios = portfolioRepository.findByUser(user);
        if (portfolios.isEmpty()) {
            throw new NoSuchElementException("User has no portfolios.");
        }
        return portfolios.stream()
                .map(Portfolio::getPortfolioId)
                .toList();
    }

    private TradeMetricsResponseDTO toDTO(TradeMetrics trade) {
        return new TradeMetricsResponseDTO(
                trade.getTradeId(),
                trade.getPortfolioId(),
                trade.getStockId(),
                trade.getStock() != null ? trade.getStock().getTickerSymbol() : null,
                trade.getAverageCost(),
                trade.getExitPrice(),
                BigDecimal.valueOf(trade.getQuantity()), // Convert Integer to BigDecimal if needed
                trade.getTotalReturn(),
                trade.getEntryDate(),
                trade.getExitDate()
        );
    }

    public TradeMetricsResponseDTO getPortfolioBestTrade(Integer portfolioId) {
        TradeMetrics bestTrade = tradeMetricsRepository.findTopByPortfolioIdAndIsBestTradeTrue(portfolioId);
        if (bestTrade == null) {
            throw new NoSuchElementException("No best trade found for portfolio with ID: " + portfolioId);
        }
        return toDTO(bestTrade);
    }

    public TradeMetricsResponseDTO getPortfolioWorstTrade(Integer portfolioId) {
        TradeMetrics worstTrade = tradeMetricsRepository.findTopByPortfolioIdAndIsWorstTradeTrue(portfolioId);
        if (worstTrade == null) {
            throw new NoSuchElementException("No worst trade found for portfolio with ID: " + portfolioId);
        }
        return toDTO(worstTrade);
    }

    public TradeMetricsResponseDTO getLatestTrade() {
        List<Integer> portfolioIds = getCurrentUserPortfolioIds();

        TradeMetrics latestTrade = tradeMetricsRepository
                .findTopByPortfolioIdInOrderByExitDateDesc(portfolioIds);

        if (latestTrade == null) {
            throw new NoSuchElementException("No trade metrics found for user portfolios.");
        }

        return toDTO(latestTrade);
    }

    public TradeMetricsResponseDTO getUserBestTrade() {
        List<Integer> portfolioIds = getCurrentUserPortfolioIds();

        List<TradeMetrics> bestTrades = tradeMetricsRepository
                .findByPortfolioIdInAndIsBestTradeTrue(portfolioIds);

        return bestTrades.stream()
                .max(Comparator.comparing(TradeMetrics::getTotalReturn))
                .map(this::toDTO)
                .orElseThrow(() -> new NoSuchElementException("No best trades found for user portfolios."));
    }

    public TradeMetricsResponseDTO getUserWorstTrade() {
        List<Integer> portfolioIds = getCurrentUserPortfolioIds();

        List<TradeMetrics> worstTrades = tradeMetricsRepository
                .findByPortfolioIdInAndIsWorstTradeTrue(portfolioIds);

        return worstTrades.stream()
                .min(Comparator.comparing(TradeMetrics::getTotalReturn)
                        .thenComparing(TradeMetrics::getExitDate, Comparator.reverseOrder()))
                .map(this::toDTO)
                .orElseThrow(() -> new NoSuchElementException("No worst trades found for user portfolios."));
    }
}
