package com.inbest.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class StockService
{
    private final WebClient webClient;

    //Dependency injection of WebClient into StockService
    public StockService(WebClient webClient)
    {
        this.webClient = webClient;
    }

    /*
        Description: getHistoricalData method is used to get historical data for the list of tickers over the last 3 months.
    */
    public Mono<Map<String, Object>> getHistoricalData()
    {
        List<String> sp500Tickers = new ArrayList<>();
        sp500Tickers.add("AAPL");
        sp500Tickers.add("MSFT");
        sp500Tickers.add("GOOGL");
        sp500Tickers.add("AMZN");
        sp500Tickers.add("TSLA");
        sp500Tickers.add("META");
        sp500Tickers.add("NVDA");
        sp500Tickers.add("UNH");
        sp500Tickers.add("V");
        sp500Tickers.add("JNJ");
        sp500Tickers.add("JPM");
        sp500Tickers.add("PFE");
        sp500Tickers.add("MA");
        sp500Tickers.add("HD");
        sp500Tickers.add("DIS");
        sp500Tickers.add("PYPL");
        sp500Tickers.add("VZ");
        sp500Tickers.add("NVDA");
        sp500Tickers.add("NFLX");
        sp500Tickers.add("CSCO");
        sp500Tickers.add("INTC");
        sp500Tickers.add("MRK");
        sp500Tickers.add("XOM");
        sp500Tickers.add("BA");
        sp500Tickers.add("WMT");
        sp500Tickers.add("MCD");
        sp500Tickers.add("KO");
        sp500Tickers.add("CAT");
        sp500Tickers.add("IBM");
        sp500Tickers.add("ABT");
        sp500Tickers.add("LLY");
        sp500Tickers.add("MS");
        sp500Tickers.add("GS");
        sp500Tickers.add("NKE");
        sp500Tickers.add("HON");
        sp500Tickers.add("AMGN");
        sp500Tickers.add("BMY");
        sp500Tickers.add("MMM");
        sp500Tickers.add("TMO");
        sp500Tickers.add("MDT");
        sp500Tickers.add("SBUX");
        sp500Tickers.add("LMT");
        sp500Tickers.add("DHR");
        sp500Tickers.add("CVX");
        sp500Tickers.add("SPG");
        LocalDate today = LocalDate.now();
        LocalDate threeMonthsAgo = today.minusDays(90);

        long startTime = threeMonthsAgo.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        long endTime = today.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

        Flux<Map<String, Object>> tickerFlux = Flux.fromIterable(sp500Tickers)
                .flatMap(ticker -> getTickerData(ticker, startTime, endTime));
        return tickerFlux.collectList().map(data -> Map.of("result", data));
    }

    /*
        getTickerData retrieves the stock data for a single ticker.
        Params: String ticker ex.,AAPL
                startTime: type is long because API requires time in seconds
                endTime: type is long because API requires time in seconds
     */
    private Mono getTickerData(String ticker, long startTime, long endtime)
    {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v8/finance/chart/" + ticker)
                        .queryParam("interval", "1d")
                        .queryParam("period1", startTime)
                        .queryParam("period2", endtime)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> parseTickerData(ticker, response));
    }

    /*
        parseTickerData extracts and formats data from Yahoo Finance API.
        Params: String ticker and API response as a map
     */
    private Map<String, Object> parseTickerData(String ticker, Map<String, Object> data)
    {
        Map<String, Object> result = Map.of();
        try
        {
            Map<String, Object> chart = (Map<String, Object>) data.get("chart");
            List<Map<String, Object>> results = (List<Map<String, Object>>) chart.get("result");

            if (results != null && !results.isEmpty())
            {
                Map<String, Object> firstResult = results.get(0);
                List<Long> timestamps = (List<Long>) firstResult.get("timestamp");
                //indicators: open, close, high, and low values of a ticker.
                Map<String, Object> indicators = (Map<String, Object>) firstResult.get("indicators");
                List<Map<String, Object>> quotes = (List<Map<String, Object>>) indicators.get("quote");

                if (quotes != null && !quotes.isEmpty())
                {
                    Map<String, Object> quote = quotes.get(0);
                    List<Double> open = (List<Double>) quote.get("open");
                    List<Double> high = (List<Double>) quote.get("high");
                    List<Double> low = (List<Double>) quote.get("low");
                    List<Double> close = (List<Double>) quote.get("close");

                    List<Map<String, Object>> historicalData = new ArrayList<>();
                    for (int i = 0; i < timestamps.size(); i++)
                    {
                        Map<String, Object> tickerData = new HashMap<>();
                        tickerData.put("date", Instant.ofEpochSecond(
                                        ((Number) timestamps.get(i)).longValue()) //it seems like redundant but when to remove Number, it gives error
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate());
                        tickerData.put("open", open.get(i));
                        tickerData.put("high", high.get(i));
                        tickerData.put("low", low.get(i));
                        tickerData.put("close", close.get(i));
                        historicalData.add(tickerData);
                    }
                    result = Map.of("symbol", ticker, "data", historicalData);
                }
            }
        }
        catch (Exception e)
        {
            result = Map.of("symbol", ticker, "error", e.getMessage());
        }
        return result;
    }
}
