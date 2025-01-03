package com.inbest.backend.controller;

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
@RequestMapping("/portfolio/stock/metric")
@RequiredArgsConstructor
public class PortfolioStockMetricController
{
    private final PortfolioStockMetricService portfolioStockMetricService;
    private final PortfolioService portfolioService;
    private final JwtService jwtService;

    @GetMapping("/{portfolioId}")
    public ResponseEntity<?> getStocksAndMetrics(@PathVariable int portfolioId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String token = (String) authentication.getCredentials();

            int userId = jwtService.extractUserIdFromToken(token);
            boolean hasAccess = portfolioService.checkPortfolioOwnership(portfolioId, userId);

            if (!hasAccess) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied", "message", "You do not have access to this portfolio."));
            }

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
