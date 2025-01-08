package com.inbest.backend.service;

import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.User;
import com.inbest.backend.repository.PortfolioRepository;
import com.inbest.backend.repository.PortfolioStockMetricRepository;
import com.inbest.backend.repository.StockRepository;
import com.inbest.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PortfolioStockMetricService
{
    private final AuthenticationService authenticationService;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioStockMetricRepository portfolioStockMetricRepository;
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
                return portfolioStockMetricRepository.findMetricsByPortfolioId(portfolioID);
            }
        }
        return portfolioStockMetricRepository.findMetricsByPortfolioIdIfPublic(portfolioID);
    }
}
