package com.inbest.backend.controller;

import com.inbest.backend.service.AuthenticationService;
import com.inbest.backend.service.JwtService;
import com.inbest.backend.service.PortfolioService;
import com.inbest.backend.service.PortfolioStockMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio/stock/metric")
@RequiredArgsConstructor
public class PortfolioStockMetricController
{
    private final AuthenticationService authenticationService;
    private final PortfolioService portfolioService;
    private final PortfolioStockMetricService portfolioStockMetricService;

    @GetMapping("/{portfolioId}")
    public ResponseEntity<?> getStocksAndMetrics(@PathVariable int portfolioId) {
        try {
            List<Map<String, Object>> data = portfolioStockMetricService.getStocksAndMetrics(portfolioId);

            if (data.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No stocks or metrics found for this portfolio."));
            }

            return ResponseEntity.ok(Map.of(
                    "holdings", data
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error", "message", e.getMessage()));
        }
    }
}
