package com.inbest.backend.service;

import com.inbest.backend.dto.PortfolioDTO;
import com.inbest.backend.model.position.PortfolioMetric;
import com.inbest.backend.model.response.PortfolioGetResponse;
import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.User;
import com.inbest.backend.model.response.PortfolioRankResponse;
import com.inbest.backend.repository.PortfolioMetricRepository;
import com.inbest.backend.repository.PortfolioRepository;
import com.inbest.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PortfolioService
{
    private final UserService userService;
    @Autowired
    private PortfolioRepository portfolioRepository;
    @Autowired
    private PortfolioMetricRepository portfolioMetricRepository;
    @Autowired
    private UserRepository userRepository;

    public PortfolioService(UserService userService) {
        this.userService = userService;
    }


    public boolean doesPortfolioNameExist(String portfolioName)
    {
        return portfolioRepository.existsByPortfolioName(portfolioName);
    }

    public int createPortfolio(PortfolioDTO portfolioDTO)
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        System.out.println(username);
        User user = userRepository.findByUsername(username).orElseThrow();
        System.out.println(user);
        if (doesPortfolioNameExist(portfolioDTO.getPortfolioName()))
        {
            throw new IllegalArgumentException("Portfolio name already exists!");
        }

        Portfolio portfolio = new Portfolio();
        portfolio.setPortfolioName(portfolioDTO.getPortfolioName());
        portfolio.setVisibility(portfolioDTO.getVisibility());
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        portfolio.setCreatedDate(now);
        portfolio.setLastUpdatedDate(now);
        portfolio.setUser(user);
        Portfolio insertedPortfolio = portfolioRepository.save(portfolio);

        PortfolioMetric portfolioMetric = new PortfolioMetric();
        portfolioMetric.setPortfolioId(portfolio.getPortfolioId());
        portfolioMetric.setPortfolio(insertedPortfolio);
        portfolioMetric.setBeta(BigDecimal.ZERO);
        portfolioMetric.setLastUpdatedDate(now);
        portfolioMetric.setSharpeRatio(BigDecimal.ZERO);
        portfolioMetric.setVolatility(BigDecimal.ZERO);
        portfolioMetric.setPortfolioValue(BigDecimal.ZERO);
        portfolioMetric.setDailyReturn(BigDecimal.ZERO);
        portfolioMetric.setHourlyReturn(BigDecimal.ZERO);
        portfolioMetric.setMonthlyReturn(BigDecimal.ZERO);
        portfolioMetric.setTotalReturn(BigDecimal.ZERO);
        portfolioMetric.setRiskScore(BigDecimal.ZERO);
        portfolioMetric.setRiskCategory("Conservative");
        portfolioMetricRepository.save(portfolioMetric);

        return insertedPortfolio.getPortfolioId();
    }

    public void updatePortfolio(int id, PortfolioDTO portfolioDTO)
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        Optional<Portfolio> portfolio = portfolioRepository.findById(Long.valueOf(id));
        if (!portfolio.isPresent())
        {
            throw new IllegalArgumentException("Portfolio not found!");
        }

        Portfolio existingPortfolio = portfolio.get();

        if (!existingPortfolio.getUser().getUsername().equals(username))
        {
            throw new SecurityException("You can only update your own portfolio!");
        }

        if (!existingPortfolio.getPortfolioName().equals(portfolioDTO.getPortfolioName())
                && doesPortfolioNameExist(portfolioDTO.getPortfolioName()))
        {
            throw new IllegalArgumentException("Portfolio name already exists!");
        }
        existingPortfolio.setPortfolioName(portfolioDTO.getPortfolioName());
        existingPortfolio.setVisibility(portfolioDTO.getVisibility());
        existingPortfolio.setUser(user);
        existingPortfolio.setLastUpdatedDate(LocalDateTime.now(ZoneId.of("UTC")));

        portfolioRepository.save(existingPortfolio);
    }

    public void deletePortfolio(int id) {
        Portfolio portfolio = portfolioRepository.findById((long) id)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found!"));

        Integer currentUserId = userService.getCurrentUserId();
        if (!portfolio.getUser().getId().equals(currentUserId)) {
            throw new SecurityException("You are not authorized to delete this portfolio.");
        }

        portfolioRepository.delete(portfolio);
    }

    public PortfolioGetResponse getPortfolioById(int id, String username)
    {
        Portfolio portfolio = portfolioRepository.findById((long) id)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));

        // Check if the portfolio belongs to the specified user
        if (!portfolio.getUser().getUsername().equals(username)) {
            throw new SecurityException("This portfolio is not owned by user " +  username);
        }

        if ("PRIVATE".equalsIgnoreCase(portfolio.getVisibility())) {
            Integer currentUserId = userService.getCurrentUserId();
            if (!portfolio.getUser().getId().equals(currentUserId)) {
                throw new SecurityException("Access denied: Portfolio is private and you are not the owner.");
            }
        }

        return new PortfolioGetResponse(
                portfolio.getPortfolioId(),
                portfolio.getPortfolioName(),
                portfolio.getCreatedDate(),
                portfolio.getLastUpdatedDate(),
                portfolio.getVisibility(),
                portfolio.getUser().getId()
        );
    }

    public List<PortfolioGetResponse> getAllPortfolios()
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Portfolio> portfolios = portfolioRepository.findByUser(user);
        return portfolios.stream().map(portfolio -> new PortfolioGetResponse(
                portfolio.getPortfolioId(),
                portfolio.getPortfolioName(),
                portfolio.getCreatedDate(),
                portfolio.getLastUpdatedDate(),
                portfolio.getVisibility(),
                portfolio.getUser().getId()
        )).collect(Collectors.toList());
    }

    public boolean checkPortfolioOwnership(int portfolioId, int userId) {
        return portfolioRepository.findByPortfolioId(portfolioId)
                .map(portfolio -> portfolio.getUser().getId().equals(userId))
                .orElse(false);
    }

    public List<PortfolioGetResponse> getPortfoliosByUsername(String username)
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String authName = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Portfolio> portfolios;

        if (authName.equals(username)) {
            // Return both public and private portfolios if user is looking for their own portfolios
            portfolios = portfolioRepository.findByUser(user);
        } else {
            // Return only public portfolios if another user is making the request
            portfolios = portfolioRepository.findByUserAndVisibility(user, "public");
        }

        return portfolios.stream().map(portfolio -> new PortfolioGetResponse(
                portfolio.getPortfolioId(),
                portfolio.getPortfolioName(),
                portfolio.getCreatedDate(),
                portfolio.getLastUpdatedDate(),
                portfolio.getVisibility(),
                portfolio.getUser().getId()
        )).collect(Collectors.toList());
    }

    public PortfolioRankResponse getPortfolioRank(int portfolioId)
    {
        Map<String, Object> result = portfolioRepository.findPortfolioRankAndTotal(portfolioId);

        if (result == null || result.isEmpty())
        {
            throw new EntityNotFoundException("Portfolio not found or has no total_return data.");
        }

        return new PortfolioRankResponse(
                (Integer) result.get("portfolio_rank"),
                ((Number) result.get("total_portfolios")).intValue()
        );
    }
}
