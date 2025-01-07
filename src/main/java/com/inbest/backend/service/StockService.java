package com.inbest.backend.service;

import com.inbest.backend.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final RestTemplate restTemplate;
    private final StockRepository stockRepository;

    public List<Map<String, String>> getAllStockNamesAndSymbols() {
        return stockRepository.findAllStocks()
                .stream()
                .map(stock -> Map.of(
                        "ticker_symbol", stock.getTickerSymbol(),
                        "stock_name", stock.getStockName()
                ))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getHistoricalData() {
        Set<String> sp500Tickers = stockRepository.findAllTickerSymbols();

        long interval = LocalDate.now().minusDays(3).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

        List<Map<String, Object>> stockData = new ArrayList<>();

        for (String ticker : sp500Tickers) {
            try {
                Map<String, Object> response = getTickerData(ticker, interval);
                stockData.add(parseTickerData(ticker, response));
            } catch (Exception e) {
                e.printStackTrace();
                stockData.add(Map.of("symbol", ticker, "error", e.getMessage()));
            }
        }
        return stockData;
    }

    private Map<String, Object> getTickerData(String ticker, long interval) {
        String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + ticker +
                "?interval=1d&period1=" + interval + "&period2=" + interval;

        ResponseEntity<Map> responseEntity = restTemplate.getForEntity(url, Map.class);
        return responseEntity.getBody();
    }

    private Map<String, Object> parseTickerData(String ticker, Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> chart = (Map<String, Object>) data.get("chart");
            List<Map<String, Object>> results = (List<Map<String, Object>>) chart.get("result");

            if (results != null && !results.isEmpty()) {
                Map<String, Object> firstResult = results.get(0);
                List<Integer> timestamps = (List<Integer>) firstResult.get("timestamp");

                Map<String, Object> indicators = (Map<String, Object>) firstResult.get("indicators");
                List<Map<String, Object>> quotes = (List<Map<String, Object>>) indicators.get("quote");

                if (quotes != null && !quotes.isEmpty()) {
                    Map<String, Object> quote = quotes.get(0);
                    List<Double> close = (List<Double>) quote.get("close");

                    if (!timestamps.isEmpty() && !close.isEmpty()) {
                        double latestPrice = close.get(0);
                        result.put("symbol", ticker);
                        result.put("date", Instant.ofEpochSecond(timestamps.get(0).longValue())
                                .atZone(ZoneId.systemDefault()).toLocalDate().toString());
                        result.put("close", latestPrice);

                        // Stock tablosunda fiyatı güncelle
                        int updatedRows = stockRepository.updateCurrentPrice(ticker, latestPrice);
                        if (updatedRows > 0) {
                            result.put("status", "Updated successfully");
                        } else {
                            result.put("status", "Stock not found");
                        }
                    }
                }
            }
        } catch (Exception e) {
            result.put("symbol", ticker);
            result.put("error", "Parsing error: " + e.getMessage());
        }
        return result;
    }

}
