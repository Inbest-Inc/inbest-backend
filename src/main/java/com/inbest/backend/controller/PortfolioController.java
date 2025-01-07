package com.inbest.backend.controller;

import com.inbest.backend.dto.PortfolioDTO;
import com.inbest.backend.model.response.PortfolioGetResponse;
import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.response.GenericResponse;
import com.inbest.backend.model.response.PortfolioResponse;
import com.inbest.backend.service.PortfolioService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController
{

    private final PortfolioService portfolioService;

    private ResponseEntity<String> validatePortfolio(PortfolioDTO portfolioDTO)
    {
        if (!"private".equals(portfolioDTO.getVisibility()) && !"public".equals(portfolioDTO.getVisibility()))
        {
            return new ResponseEntity<>("Visibility must be either 'private' or 'public'", HttpStatus.BAD_REQUEST);
        }
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

    @PostMapping("/update/{id}")
    public ResponseEntity<?> updatePortfolio(@PathVariable Integer id, @RequestBody PortfolioDTO portfolioDTO)
    {
        if (id == null || id <= 0)
        {
            return new ResponseEntity<>("Invalid ID", HttpStatus.BAD_REQUEST);
        }

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

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePortfolio(@PathVariable Integer id)
    {
        if (id == null || id <= 0)
        {
            return new ResponseEntity<>("Invalid ID", HttpStatus.BAD_REQUEST);
        }

        try
        {
            portfolioService.deletePortfolio(id);
            return new ResponseEntity<>(new HashMap<String, String>()
            {{
                put("status", "deleted");
            }}, HttpStatus.OK);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get")
    public ResponseEntity<?> getPortfolio(@RequestParam(value = "id", required = false) Integer id)
    {
        try
        {
            if (id != null)
            {
                PortfolioGetResponse portfolio = portfolioService.getPortfolioById(id);
                return ResponseEntity.ok(new GenericResponse("success", "Portfolio retrieved successfully", portfolio));
            }
            else
            {
                List<PortfolioGetResponse> portfolios = portfolioService.getAllPortfolios();
                return ResponseEntity.ok(new GenericResponse("success", "Portfolios retrieved successfully", portfolios));
            }
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/list-by-username/{username}")
    public ResponseEntity<?> getPortfoliosByUsername(@PathVariable String username) {
        try
        {
            List<PortfolioGetResponse> portfolios = portfolioService.getPortfoliosByUsername(username);
            return ResponseEntity.ok(new GenericResponse("success", "Portfolios retrieved successfully", portfolios));
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
