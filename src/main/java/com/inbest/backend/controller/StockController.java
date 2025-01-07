package com.inbest.backend.controller;

import com.inbest.backend.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {
    private final StockService stockService;

    @GetMapping("/data")
    public ResponseEntity<?> getHistoricalData() {
        List<Map<String, Object>> data = stockService.getHistoricalData();
        return ResponseEntity.ok(Map.of("result", data));
    }

    @GetMapping("/tickers")
    public ResponseEntity<List<Map<String, String>>> getAllStockNamesAndSymbols() {
        List<Map<String, String>> stocks = stockService.getAllStockNamesAndSymbols();
        return ResponseEntity.ok(stocks);
    }
}
