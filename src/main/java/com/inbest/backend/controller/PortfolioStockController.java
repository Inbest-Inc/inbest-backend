package com.inbest.backend.controller;

import com.inbest.backend.model.PortfolioStockModel;
import com.inbest.backend.model.response.PortfolioStockResponse;
import com.inbest.backend.service.AuthenticationService;
import com.inbest.backend.service.PortfolioService;
import com.inbest.backend.service.PortfolioStockService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/portfolio-stock")
public class PortfolioStockController
{

    private final PortfolioStockService portfolioStockService;
    private final PortfolioService portfolioService;
    private final AuthenticationService authenticationService;

    @PostMapping("/add")
    public ResponseEntity<?> addStockToPortfolio(@RequestParam Integer portfolioId, @RequestParam Integer stockId)
    {
        try
        {
            int userId = authenticationService.authenticate_user();
            boolean hasAccess = portfolioService.checkPortfolioOwnership(portfolioId, userId);
            if (!hasAccess)
            {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied", "message", "You do not have access to this portfolio."));
            }
            PortfolioStockResponse portfolioStockResponse = portfolioStockService.addStockToPortfolio(portfolioId, stockId);
            return new ResponseEntity<>(portfolioStockResponse, HttpStatus.CREATED);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update/quantity")
    public ResponseEntity<?> updateStockQuantity(@RequestParam Integer portfolioId, @RequestParam Integer stockId, @RequestParam Integer quantity)
    {
        try
        {
            int userId = authenticationService.authenticate_user();
            boolean hasAccess = portfolioService.checkPortfolioOwnership(portfolioId, userId);
            if (!hasAccess)
            {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied", "message", "You do not have access to this portfolio."));
            }

            if (quantity < 0)
            {
                throw new IllegalArgumentException("Quantity must be greater than zero");
            }
            portfolioStockService.updateQuantity(portfolioId, stockId, quantity);
            return new ResponseEntity<>(new HashMap<String, String>()
            {{
                put("status", "success");
            }}, HttpStatus.OK);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteStockFromPortfolio(@RequestParam Integer portfolioId, @RequestParam Integer stockId)
    {
        try
        {
            int userId = authenticationService.authenticate_user();
            boolean hasAccess = portfolioService.checkPortfolioOwnership(portfolioId, userId);
            if (!hasAccess)
            {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied", "message", "You do not have access to this portfolio."));
            }
            portfolioStockService.removeStockFromPortfolio(portfolioId, stockId);
            return ResponseEntity.ok(Map.of("status", "success"));
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
