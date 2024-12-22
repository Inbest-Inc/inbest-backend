package com.inbest.backend.controller;

import com.inbest.backend.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.*;

@RestController
public class StockController
{
    @Autowired
    private final StockService stockService;

    public StockController(StockService stockService)
    {
        this.stockService = stockService;
    }

    @GetMapping("/stock")
    public Mono<ResponseEntity<Map<String, Object>>> getHistoricalData()
    {
        return stockService.getHistoricalData()
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    Map<String, Object> errorResponse = Map.of(
                            "error", "Failed to get stock data",
                            "message", error.getMessage()
                    );
                    return Mono.just(ResponseEntity.badRequest().body(errorResponse));
                });
    }

}
