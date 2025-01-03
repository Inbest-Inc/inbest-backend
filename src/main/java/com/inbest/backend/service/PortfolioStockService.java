package com.inbest.backend.service;

import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.PortfolioStockModel;
import com.inbest.backend.model.Stock;
import com.inbest.backend.model.response.PortfolioStockResponse;
import com.inbest.backend.repository.PortfolioRepository;
import com.inbest.backend.repository.PortfolioStockRepository;
import com.inbest.backend.repository.StockRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class PortfolioStockService
{

    private final PortfolioStockRepository portfolioStockRepository;
    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;

    public PortfolioStockService(PortfolioStockRepository portfolioStockRepository, PortfolioRepository portfolioRepository, StockRepository stockRepository)
    {
        this.portfolioStockRepository = portfolioStockRepository;
        this.portfolioRepository = portfolioRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public PortfolioStockResponse addStockToPortfolio(Integer portfolioId, Integer stockId) throws Exception
    {
        Portfolio portfolio = portfolioRepository.findById(Long.valueOf(portfolioId)).orElseThrow(() -> new Exception("Portfolio not found"));

        Stock stock = stockRepository.findById(Long.valueOf(stockId)).orElseThrow(() -> new Exception("Stock not found"));

        boolean stockExistsInPortfolio = portfolioStockRepository.existsByPortfolioAndStock(portfolio, stock);
        if (stockExistsInPortfolio)
        {
            throw new Exception("Stock is already added to the portfolio");
        }


        PortfolioStockModel portfolioStockModel = PortfolioStockModel.builder()
                .portfolio(portfolio)
                .stock(stock)
                .quantity((double) 0)
                .visibility(portfolio.getVisibility())
                .build();


        portfolioStockRepository.save(portfolioStockModel);

        return new PortfolioStockResponse(
                portfolioStockModel.getPortfolioStockId(),
                stock.getStockName(),
                stock.getTickerSymbol(),
                portfolioStockModel.getQuantity(),
                stock.getCurrentPrice()
        );
    }
}
