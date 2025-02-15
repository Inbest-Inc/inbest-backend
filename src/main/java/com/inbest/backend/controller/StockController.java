package com.inbest.backend.controller;

import com.inbest.backend.model.Stock;
import com.inbest.backend.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {
    private final StockService stockService;

    @GetMapping("/data")
    public ResponseEntity<?> getHistoricalData() {
        List<Stock> stocks = stockService.findAllStocks();
        return ResponseEntity.ok(Map.of("result", stocks));
    }

    @GetMapping("/tickers")
    public ResponseEntity<List<Map<String, String>>> getAllStockNamesAndSymbols() {
        List<Stock> stocks = stockService.findAllStocks();
        List<Map<String, String>> result = stocks.stream()
                .map(stock -> Map.of(
                        "symbol", stock.getTickerSymbol(),
                        "name", stock.getStockName()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}