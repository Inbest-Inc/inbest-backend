package com.inbest.backend.service;

import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.PortfolioFollow;
import com.inbest.backend.model.User;
import com.inbest.backend.repository.PortfolioFollowRepository;
import com.inbest.backend.repository.PortfolioRepository;
import com.inbest.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioFollowService {

    private final PortfolioFollowRepository portfolioFollowRepository;
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    @Transactional
    public void followPortfolio(Integer portfolioId) {
        if (portfolioId == null) {
            throw new IllegalArgumentException("Portfolio ID cannot be null");
        }

        User user = getCurrentUser();
        Portfolio portfolio = portfolioRepository.findById(Long.valueOf(portfolioId))
                .orElseThrow(() -> new IllegalArgumentException("Portfolio with ID " + portfolioId + " does not exist"));

        if (portfolio.getUser().equals(user)) {
            throw new IllegalArgumentException("You cannot follow your own portfolio");
        }

        if (portfolioFollowRepository.existsByUserIdAndPortfolioId(user.getId(), portfolioId)) {
            throw new IllegalArgumentException("You are already following portfolio with ID " + portfolioId);
        }

        PortfolioFollow follow = new PortfolioFollow();
        follow.setUserId(user.getId());
        follow.setPortfolioId(portfolioId);
        portfolioFollowRepository.save(follow);
    }

    @Transactional
    public void unfollowPortfolio(Integer portfolioId) {
        if (portfolioId == null) {
            throw new IllegalArgumentException("Portfolio ID cannot be null");
        }

        User user = getCurrentUser();
        Portfolio portfolio = portfolioRepository.findById(Long.valueOf(portfolioId))
                .orElseThrow(() -> new IllegalArgumentException("Portfolio with ID " + portfolioId + " does not exist"));

        if (!portfolioFollowRepository.existsByUserIdAndPortfolioId(user.getId(), portfolioId)) {
            throw new IllegalArgumentException("You are not following portfolio with ID " + portfolioId);
        }

        portfolioFollowRepository.deleteByUserIdAndPortfolioId(user.getId(), portfolioId);
    }

    public boolean isFollowing(Integer portfolioId) {
        if (portfolioId == null) {
            throw new IllegalArgumentException("Portfolio ID cannot be null");
        }

        User user = getCurrentUser();
        return portfolioFollowRepository.existsByUserIdAndPortfolioId(user.getId(), portfolioId);
    }

    public List<PortfolioFollow> getFollowedPortfolios() {
        User user = getCurrentUser();
        return portfolioFollowRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public List<PortfolioFollow> getPortfolioFollowers(Integer portfolioId) {
        if (portfolioId == null) {
            throw new IllegalArgumentException("Portfolio ID cannot be null");
        }

        Portfolio portfolio = portfolioRepository.findById(Long.valueOf(portfolioId))
                .orElseThrow(() -> new IllegalArgumentException("Portfolio with ID " + portfolioId + " does not exist"));
        return portfolioFollowRepository.findByPortfolioIdOrderByCreatedAtDesc(portfolioId);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User with username '" + username + "' not found"));
    }
} 