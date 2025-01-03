package com.inbest.backend.service;

import com.inbest.backend.dto.PortfolioDTO;
import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.User;
import com.inbest.backend.repository.PortfolioRepository;
import com.inbest.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class PortfolioService
{
    @Autowired
    private PortfolioRepository portfolioRepository;
    @Autowired
    private UserRepository userRepository;

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
            throw new IllegalArgumentException("Portfolio name already exists");
        }

        Portfolio portfolio = new Portfolio();
        portfolio.setPortfolioName(portfolioDTO.getPortfolioName());
        portfolio.setVisibility(portfolioDTO.getVisibility());
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        portfolio.setCreatedDate(now);
        portfolio.setLastUpdatedDate(now);
        portfolio.setUser(user);
        Portfolio insertedPortfolio = portfolioRepository.save(portfolio);
        return insertedPortfolio.getPortfolioId();
    }

}
