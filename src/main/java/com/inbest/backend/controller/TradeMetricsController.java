package com.inbest.backend.controller;

import com.inbest.backend.model.TradeMetrics;
import com.inbest.backend.model.response.GenericResponse;
import com.inbest.backend.model.response.TradeMetricsResponseDTO;
import com.inbest.backend.service.TradeMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trade")
@RequiredArgsConstructor
public class TradeMetricsController {

    private final TradeMetricsService tradeMetricsService;

    @GetMapping("/latest")
    public GenericResponse getLatestTrade() {
        try {
            TradeMetricsResponseDTO trade = tradeMetricsService.getLatestTrade();
            return new GenericResponse("success", "Latest trade fetched successfully.", trade);
        } catch (Exception e) {
            return new GenericResponse("error", e.getMessage(), null);
        }
    }

    @GetMapping("/user/best")
    public GenericResponse getUserBestTrade() {
        try {
            TradeMetricsResponseDTO trade = tradeMetricsService.getUserBestTrade();
            return new GenericResponse("success", "Best trade of user fetched successfully.", trade);
        } catch (Exception e) {
            return new GenericResponse("error", e.getMessage(), null);
        }
    }

    @GetMapping("/user/worst")
    public GenericResponse getUserWorstTrade() {
        try {
            TradeMetricsResponseDTO trade = tradeMetricsService.getUserWorstTrade();
            return new GenericResponse("success", "Worst trade of user fetched successfully.", trade);
        } catch (Exception e) {
            return new GenericResponse("error", e.getMessage(), null);
        }
    }

    @GetMapping("/portfolio/{portfolioId}/best")
    public GenericResponse getPortfolioBestTrade(@PathVariable Integer portfolioId) {
        try {
            TradeMetricsResponseDTO trade = tradeMetricsService.getPortfolioBestTrade(portfolioId);
            return new GenericResponse("success", "Best trade of portfolio fetched successfully.", trade);
        } catch (Exception e) {
            return new GenericResponse("error", e.getMessage(), null);
        }
    }

    @GetMapping("/portfolio/{portfolioId}/worst")
    public GenericResponse getPortfolioWorstTrade(@PathVariable Integer portfolioId) {
        try {
            TradeMetricsResponseDTO trade = tradeMetricsService.getPortfolioWorstTrade(portfolioId);
            return new GenericResponse("success", "Worst trade of portfolio fetched successfully.", trade);
        } catch (Exception e) {
            return new GenericResponse("error", e.getMessage(), null);
        }
    }
}