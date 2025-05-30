package com.inbest.backend.service;

import com.inbest.backend.dto.BestPortfolioResponseDTO;
import com.inbest.backend.dto.PortfolioDTO;
import com.inbest.backend.model.response.BestPortfolioResponse;
import com.inbest.backend.dto.UserDTO;
import com.inbest.backend.model.position.PortfolioMetric;
import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.User;
import com.inbest.backend.model.response.PortfolioMetricResponse;
import com.inbest.backend.repository.PortfolioMetricRepository;
import com.inbest.backend.repository.PortfolioStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BestPortfoliosService
{

    private final PortfolioMetricRepository portfolioMetricRepository;
    private final FollowService followService;
    private final PortfolioStockRepository portfolioStockRepository;

    public List<BestPortfolioResponse> getBestPortfoliosByTotalReturn()
    {
        return buildResponse(portfolioMetricRepository.findTop10ByTotalReturnForPublic());
    }

    public List<BestPortfolioResponse> getBestPortfoliosByDailyReturn()
    {
        return buildResponse(portfolioMetricRepository.findTop10ByDailyReturnForPublic());
    }

    public List<BestPortfolioResponse> getBestPortfoliosByMonthlyReturn()
    {
        return buildResponse(portfolioMetricRepository.findTop10ByMonthlyReturnForPublic());
    }

    public List<BestPortfolioResponse> getBestPortfoliosByHourlyReturn()
    {
        return buildResponse(portfolioMetricRepository.findTop10ByHourlyReturnForPublic());
    }

    private List<BestPortfolioResponse> buildResponse(List<PortfolioMetric> metricsList)
    {
        return metricsList.stream().map(metric -> {
            Portfolio portfolio = metric.getPortfolio();
            User user = portfolio.getUser();

            UserDTO userDTO = UserDTO.builder()
                    .username(user.getUsername())
                    .name(user.getName())
                    .email(null)
                    .surname(user.getSurname())
                    .image_url(user.getImageUrl())
                    .followerCount(followService.getFollowerCount(user.getUsername()))
                    .build();

            Integer distinctStockCount = portfolioStockRepository.countDistinctStocksByPortfolio(portfolio);


            BestPortfolioResponseDTO bestPortfolioResponseDTO = BestPortfolioResponseDTO.builder()
                    .portfolioName(portfolio.getPortfolioName())
                    .visibility(portfolio.getVisibility())
                    .holdingCount(distinctStockCount)
                    .build();

            PortfolioMetricResponse metricDTO = PortfolioMetricResponse.builder()
                    .portfolioId(metric.getPortfolioId())
                    .hourlyReturn(metric.getHourlyReturn())
                    .dailyReturn(metric.getDailyReturn())
                    .monthlyReturn(metric.getMonthlyReturn())
                    .totalReturn(metric.getTotalReturn())
                    .beta(metric.getBeta())
                    .sharpeRatio(metric.getSharpeRatio())
                    .volatility(metric.getVolatility())
                    .riskScore(metric.getRiskScore())
                    .riskCategory(metric.getRiskCategory())
                    .build();

            return BestPortfolioResponse.builder()
                    .portfolioMetric(metricDTO)
                    .user(userDTO)
                    .bestPortfolioResponseDTO(bestPortfolioResponseDTO)
                    .build();

        }).collect(Collectors.toList());
    }
}