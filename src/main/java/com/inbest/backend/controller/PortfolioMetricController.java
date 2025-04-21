package com.inbest.backend.controller;

import com.inbest.backend.model.position.PortfolioMetric;
import com.inbest.backend.model.response.GenericResponse;
import com.inbest.backend.model.response.PortfolioMetricResponse;
import com.inbest.backend.service.PortfolioMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio-metrics")
@RequiredArgsConstructor
public class PortfolioMetricController {

    private final PortfolioMetricService portfolioMetricService;

    @GetMapping("/get")
    public ResponseEntity<?> getPortfolioMetrics(@RequestParam(value = "portfolioId") Integer portfolioId) {
        try {
            if (portfolioId <= 0) {
                return new ResponseEntity<>("Invalid portfolio ID", HttpStatus.BAD_REQUEST);
            }

            PortfolioMetricResponse metrics = portfolioMetricService.getMetricsByPortfolioId(portfolioId);
            return ResponseEntity.ok(new GenericResponse("success", "Portfolio metrics retrieved successfully", metrics));
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
