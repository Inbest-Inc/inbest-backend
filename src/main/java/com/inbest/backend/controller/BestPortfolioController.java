package com.inbest.backend.controller;

import com.inbest.backend.model.response.BestPortfolioResponse;
import com.inbest.backend.model.response.GenericResponse;
import com.inbest.backend.service.BestPortfoliosService;
import com.inbest.backend.service.PortfolioMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios/best")
@RequiredArgsConstructor
public class BestPortfolioController {

    private final BestPortfoliosService bestPortfoliosService;

    @GetMapping("/total-return")
    public ResponseEntity<GenericResponse> getBestByTotalReturn() {
        List<BestPortfolioResponse> result = bestPortfoliosService.getBestPortfoliosByTotalReturn();
        return ResponseEntity.ok(new GenericResponse(
                "success",
                "Top 10 portfolios by total return",
                result
        ));
    }

    @GetMapping("/daily-return")
    public ResponseEntity<GenericResponse> getBestByDailyReturn() {
        List<BestPortfolioResponse> result = bestPortfoliosService.getBestPortfoliosByDailyReturn();
        return ResponseEntity.ok(new GenericResponse(
                "success",
                "Top 10 portfolios by daily return",
                result
        ));
    }

    @GetMapping("/monthly-return")
    public ResponseEntity<GenericResponse> getBestByMonthlyReturn() {
        List<BestPortfolioResponse> result = bestPortfoliosService.getBestPortfoliosByMonthlyReturn();
        return ResponseEntity.ok(new GenericResponse(
                "success",
                "Top 10 portfolios by monthly return",
                result
        ));
    }

    @GetMapping("/hourly-return")
    public ResponseEntity<GenericResponse> getBestByHourlyReturn() {
        List<BestPortfolioResponse> result = bestPortfoliosService.getBestPortfoliosByHourlyReturn();
        return ResponseEntity.ok(new GenericResponse(
                "success",
                "Top 10 portfolios by hourly return",
                result
        ));
    }
}
