package com.inbest.backend.controller;

import com.inbest.backend.model.response.GenericResponse;
import com.inbest.backend.model.response.PortfolioReturnResponse;
import com.inbest.backend.model.response.PortfolioMetricResponse;
import com.inbest.backend.service.PortfolioMetricService;
import lombok.RequiredArgsConstructor;
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
                return ResponseEntity
                        .badRequest()
                        .body(new GenericResponse("error", "Invalid Portfolio Id", null));
            }

            PortfolioMetricResponse metrics = portfolioMetricService.getMetricsByPortfolioId(portfolioId);
            return ResponseEntity.ok(new GenericResponse("success", "Portfolio metrics retrieved successfully", metrics));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new GenericResponse("error", e.getMessage(), null));
        }
    }

    @GetMapping("/weekly-returns")
    public ResponseEntity<?> getWeeklyReturns(@RequestParam(value = "portfolioId") Integer portfolioId) {
        try {
            if (portfolioId <= 0) {
                return ResponseEntity
                        .badRequest()
                        .body(new GenericResponse("error", "Invalid Portfolio Id", null));
            }

            List<PortfolioReturnResponse> returns = portfolioMetricService.getWeeklyReturns(portfolioId);
            return ResponseEntity.ok(new GenericResponse("success", "Weekly returns retrieved successfully", returns));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new GenericResponse("error", e.getMessage(), null));
        }
    }

    @GetMapping("/monthly-returns")
    public ResponseEntity<?> getMonthlyReturns(@RequestParam(value = "portfolioId") Integer portfolioId) {
        try {
            if (portfolioId <= 0) {
                return ResponseEntity
                        .badRequest()
                        .body(new GenericResponse("error", "Invalid Portfolio Id", null));
            }

            List<PortfolioReturnResponse> returns = portfolioMetricService.getMonthlyReturns(portfolioId);
            return ResponseEntity.ok(new GenericResponse("success", "Monthly returns retrieved successfully", returns));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new GenericResponse("error", e.getMessage(), null));
        }
    }

    @GetMapping("/yearly-returns")
    public ResponseEntity<?> getYearlyReturns(@RequestParam(value = "portfolioId") Integer portfolioId) {
        try {
            if (portfolioId <= 0) {
                return ResponseEntity
                        .badRequest()
                        .body(new GenericResponse("error", "Invalid Portfolio Id", null));
            }

            List<PortfolioReturnResponse> returns = portfolioMetricService.getYearlyReturns(portfolioId);
            return ResponseEntity.ok(new GenericResponse("success", "Yearly returns retrieved successfully", returns));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new GenericResponse("error", e.getMessage(), null));
        }
    }
}
