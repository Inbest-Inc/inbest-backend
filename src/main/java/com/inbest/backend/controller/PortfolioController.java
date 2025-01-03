package com.inbest.backend.controller;

import com.inbest.backend.dto.PortfolioDTO;
import com.inbest.backend.model.response.PortfolioResponse;
import com.inbest.backend.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
public class PortfolioController
{

    private final PortfolioService portfolioService;

    private ResponseEntity<String> validatePortfolio(PortfolioDTO portfolioDTO)
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

        return null;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPortfolio(@RequestBody PortfolioDTO portfolioDTO)
    {
        ResponseEntity<String> validationResponse = validatePortfolio(portfolioDTO);
        if (validationResponse != null)
        {
            return validationResponse;
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

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePortfolio(@PathVariable int id, @RequestBody PortfolioDTO portfolioDTO)
    {
        ResponseEntity<String> validationResponse = validatePortfolio(portfolioDTO);
        if (validationResponse != null)
        {
            return validationResponse;
        }

        try
        {
            portfolioService.updatePortfolio(id, portfolioDTO);
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
}
