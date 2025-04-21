package com.inbest.backend.service;

import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.User;
import com.inbest.backend.model.position.PortfolioMetric;
import com.inbest.backend.model.response.PortfolioMetricResponse;
import com.inbest.backend.repository.PortfolioMetricRepository;
import com.inbest.backend.repository.PortfolioMetricRepository;
import com.inbest.backend.repository.PortfolioRepository;
import com.inbest.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PortfolioMetricService {

    private final PortfolioMetricRepository portfolioMetricRepository;
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

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
        if ("private".equals(portfolio.getVisibility()))
        {
            // If portfolio is private, check user authorization
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            if (username.equals("anonymousUser"))
            {
                throw new IllegalArgumentException("Access denied for portfolio with ID: " + portfolioId);
            }
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Check if current user is the owner of the portfolio
            if (!portfolio.getUser().getId().equals(user.getId()))
            {
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

        // Create a new PortfolioMetrics object without portfolioValue
        PortfolioMetricResponse response = PortfolioMetricResponse.builder()
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

        return response;
    }


}
