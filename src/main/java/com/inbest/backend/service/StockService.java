package com.inbest.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StockService {

    private final RestTemplate restTemplate;

    public List<Map<String, Object>> getHistoricalData() {
        Set<String> sp500Tickers = Set.of(
                "AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "META", "NVDA", "UNH",
                "V", "JNJ", "JPM", "PFE", "MA", "HD", "DIS", "PYPL", "VZ", "NFLX",
                "CSCO", "INTC", "MRK", "XOM", "BA", "WMT", "MCD", "KO", "CAT",
                "IBM", "ABT", "LLY", "MS", "GS", "NKE", "HON", "AMGN", "BMY",
                "MMM", "TMO", "MDT", "SBUX", "LMT", "DHR", "CVX", "SPG"
        );

        LocalDate today = LocalDate.now();
        LocalDate threeMonthsAgo = today.minusDays(90);

        long startTime = threeMonthsAgo.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        long endTime = today.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

        List<Map<String, Object>> stockData = new ArrayList<>();

        for (String ticker : sp500Tickers) {
            try {
                Map<String, Object> response = getTickerData(ticker, startTime, endTime);
                stockData.add(parseTickerData(ticker, response));
            } catch (Exception e) {
                e.printStackTrace();
                stockData.add(Map.of("symbol", ticker, "error", e.getMessage()));
            }
        }
        return stockData;
    }

    private Map<String, Object> getTickerData(String ticker, long startTime, long endtime) {
        String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + ticker +
                "?interval=1d&period1=" + startTime + "&period2=" + endtime;

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
                    List<Double> open = (List<Double>) quote.get("open");
                    List<Double> high = (List<Double>) quote.get("high");
                    List<Double> low = (List<Double>) quote.get("low");
                    List<Double> close = (List<Double>) quote.get("close");

                    List<Map<String, Object>> historicalData = new ArrayList<>();
                    for (int i = 0; i < timestamps.size(); i++) {
                        Map<String, Object> tickerData = new HashMap<>();
                        tickerData.put("date", Instant.ofEpochSecond(timestamps.get(i).longValue())
                                .atZone(ZoneId.systemDefault()).toLocalDate());
                        tickerData.put("open", open.get(i));
                        tickerData.put("high", high.get(i));
                        tickerData.put("low", low.get(i));
                        tickerData.put("close", close.get(i));
                        historicalData.add(tickerData);
                    }
                    result.put("symbol", ticker);
                    result.put("data", historicalData);
                }
            }
        } catch (Exception e) {
            result.put("symbol", ticker);
            result.put("error", "Parsing error: " + e.getMessage());
        }
        return result;
    }
}
