package com.inbest.backend.controller;

import com.inbest.backend.dto.PortfolioDTO;
import com.inbest.backend.model.response.PortfolioGetResponse;
import com.inbest.backend.model.response.GenericResponse;
import com.inbest.backend.model.response.PortfolioRankResponse;
import com.inbest.backend.model.response.PortfolioResponse;
import com.inbest.backend.service.PortfolioService;
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
            PortfolioResponse response = new PortfolioResponse("success","Portfolio created successfully",portfolioId);
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
    public ResponseEntity<GenericResponse> deletePortfolio(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body(
                    new GenericResponse("error", "Invalid ID", null)
            );
        }

        try {
            portfolioService.deletePortfolio(id);
            return ResponseEntity.ok(
                    new GenericResponse("success", "Portfolio deleted successfully", null)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new GenericResponse("error", e.getMessage(), null)
            );
        }
    }


    @GetMapping("/get")
    public ResponseEntity<GenericResponse> getPortfolio(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "username", required = false) String username)
    {
        try
        {
            if (id != null && username != null)
            {
                PortfolioGetResponse portfolio = portfolioService.getPortfolioById(id, username);
                return ResponseEntity.ok(new GenericResponse("success", "Portfolio retrieved successfully", portfolio));
            }
            else
            {
                return new ResponseEntity<>(new GenericResponse("error", "Both id and username parameters are required when querying a specific portfolio", null), HttpStatus.BAD_REQUEST);
            }
        }
        catch (SecurityException e)
        {
            return new ResponseEntity<>(new GenericResponse("error", e.getMessage(), null), HttpStatus.FORBIDDEN);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(new GenericResponse("error", e.getMessage(), null), HttpStatus.BAD_REQUEST);
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

    @GetMapping("/{portfolioId}/rank")
    public ResponseEntity<GenericResponse> getPortfolioRank(@PathVariable int portfolioId)
    {
        PortfolioRankResponse response = portfolioService.getPortfolioRank(portfolioId);

        GenericResponse genericResponse = new GenericResponse(
                "success",
                "Portfolio ranking fetched successfully.",
                response
        );

        return ResponseEntity.ok(genericResponse);
    }
}
