package com.inbest.backend.service;

import com.inbest.backend.dto.DonutChartDTO;
import com.inbest.backend.model.PortfolioStockModel;
import com.inbest.backend.model.position.PortfolioStockMetric;
import com.inbest.backend.repository.PortfolioStockMetricRepository;
import com.inbest.backend.repository.PortfolioStockRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
@Service
public class ChartService {

    private final PortfolioStockRepository portfolioStockRepository;
    private final PortfolioStockMetricRepository portfolioStockMetricRepository;

    public ChartService(PortfolioStockRepository portfolioStockRepository, PortfolioStockMetricRepository portfolioStockMetricRepository) {
        this.portfolioStockRepository = portfolioStockRepository;
        this.portfolioStockMetricRepository = portfolioStockMetricRepository;
    }

    public List<DonutChartDTO> createDonutChartData(Long portfolioId) {
        List<DonutChartDTO> donutChartList = new ArrayList<>();

        List<PortfolioStockModel> portfolioStocks = portfolioStockRepository.findByPortfolio_PortfolioId(portfolioId);

        if (portfolioStocks.isEmpty()) {
            throw new NoSuchElementException("No stocks found for portfolio id: " + portfolioId);
        }

        for (PortfolioStockModel stock : portfolioStocks) {
            PortfolioStockMetric metric = portfolioStockMetricRepository
                    .findTopByPortfolioIdAndStockIdOrderByDateDesc(stock.getPortfolio().getPortfolioId(), stock.getStock().getStockId())
                    .orElseThrow(() -> new NoSuchElementException("No metrics found for stock id: " + stock.getStock().getStockId()));

            DonutChartDTO donutChartDTO = new DonutChartDTO();
            donutChartDTO.setTickerSymbol(stock.getStock().getTickerSymbol());
            donutChartDTO.setPosition_weight(metric.getPositionWeight());

            donutChartList.add(donutChartDTO);
        }

        return donutChartList;
    }
}
