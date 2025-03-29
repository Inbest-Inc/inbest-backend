package com.inbest.backend.service;

import com.inbest.backend.dto.InvestmentActivityCreateDTO;
import com.inbest.backend.dto.InvestmentActivityResponseDTO;
import com.inbest.backend.model.InvestmentActivity;
import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.Stock;
import com.inbest.backend.repository.InvestmentActivityRepository;
import com.inbest.backend.repository.PortfolioRepository;
import com.inbest.backend.repository.StockRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvestmentActivityService {
    private final InvestmentActivityRepository investmentActivityRepository;
    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;

    @Transactional
    public InvestmentActivityResponseDTO createActivity(InvestmentActivityCreateDTO dto) {
        Portfolio portfolio = portfolioRepository.findById(dto.getPortfolioId().longValue())
                .orElseThrow(() -> new EntityNotFoundException("Portfolio not found"));
        
        Stock stock = stockRepository.findById(dto.getStockId().longValue())
                .orElseThrow(() -> new EntityNotFoundException("Stock not found"));

        InvestmentActivity activity = new InvestmentActivity();
        activity.setPortfolio(portfolio);
        activity.setStock(stock);
        activity.setActionType(InvestmentActivity.ActionType.valueOf(dto.getActionType()));
        activity.setAmount(dto.getAmount());
        activity.setStockQuantity(dto.getStockQuantity());
        activity.setDate(LocalDateTime.now());

        InvestmentActivity savedActivity = investmentActivityRepository.save(activity);
        return convertToDTO(savedActivity);
    }

//    public List<InvestmentActivityResponseDTO> getAllActivities() {
//        return investmentActivityRepository.findAll().stream()
//                .map(this::convertToDTO)
//                .collect(Collectors.toList());
//    }

    public Optional<InvestmentActivityResponseDTO> getActivityById(Long id) {
        return investmentActivityRepository.findById(id)
                .map(this::convertToDTO);
    }

    public List<InvestmentActivityResponseDTO> getActivitiesByPortfolioId(Integer portfolioId) {
        if (!portfolioRepository.existsById(Long.valueOf(portfolioId))) {
            throw new EntityNotFoundException("Portfolio can not be found");
        }
        return investmentActivityRepository.findByPortfolio_PortfolioId(portfolioId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteActivity(Long id) {
        if (!investmentActivityRepository.existsById(id)) {
            throw new EntityNotFoundException("Investment activity not found");
        }
        investmentActivityRepository.deleteById(id);
    }

    private InvestmentActivityResponseDTO convertToDTO(InvestmentActivity activity) {
        InvestmentActivityResponseDTO dto = new InvestmentActivityResponseDTO();
        dto.setActivityId(activity.getActivityId());
        dto.setPortfolioId(activity.getPortfolio().getPortfolioId());
        dto.setStockId(activity.getStock().getStockId());
        dto.setStockSymbol(activity.getStock().getTickerSymbol());
        dto.setStockName(activity.getStock().getStockName());
        dto.setActionType(activity.getActionType().toString());
        dto.setAmount(activity.getAmount());
        dto.setStockQuantity(activity.getStockQuantity());
        dto.setDate(activity.getDate());
        return dto;
    }
} 