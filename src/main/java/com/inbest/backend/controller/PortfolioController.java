package com.inbest.backend.controller;

import com.inbest.backend.dto.PortfolioDTO;
import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.response.PortfolioResponse;
import com.inbest.backend.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// PortfolioController içerisinde sadece portfolyoya ait CRUD operasyonları yapılıyor
//Create portfolio => portfolioName ve visibility ile create ediliyor. Return: portfolioId
//Update portfolio => param: portfolioId
//Delete portfolio => param: portfolioId

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
public class PortfolioController
{

    private final PortfolioService portfolioService;

    @PostMapping("/create")
    public ResponseEntity<?> createPortfolio(@RequestBody PortfolioDTO portfolioDTO)
    {
        if (portfolioDTO.getPortfolioName() == null || portfolioDTO.getPortfolioName().isEmpty())
        {
            return new ResponseEntity<>("Portfolio name is required.", HttpStatus.BAD_REQUEST);
        }

        if (portfolioDTO.getVisibility() == null || portfolioDTO.getVisibility().isEmpty())
        {
            return new ResponseEntity<>("Visibility is required.", HttpStatus.BAD_REQUEST);
        }

        if (portfolioDTO.getPortfolioName().length() > 100)
        {
            return new ResponseEntity<>("Portfolio name cannot exceed 100 characters.", HttpStatus.BAD_REQUEST);
        }

        try
        {
            int portfolioId = portfolioService.createPortfolio(portfolioDTO);
            PortfolioResponse response = new PortfolioResponse(portfolioId);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }
        catch (IllegalArgumentException e)
        {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }
}
