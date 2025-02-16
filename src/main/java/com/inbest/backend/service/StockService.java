package com.inbest.backend.service;

import com.inbest.backend.repository.StockRepository;
import com.inbest.backend.repository.StockPriceRepository;
import com.inbest.backend.model.Stock;
import com.inbest.backend.model.StockPrice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final RestTemplate restTemplate;
    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    //bu iki fonksiyon data ve tickers endpointlerindeki ciktilari bozmamak icindi burada hata olabilir
    public List<Map<String, String>> getAllStockNamesAndSymbols() {
        return stockRepository.findAllStocks()
                .stream()
                .map(stock -> Map.of(
                        "ticker_symbol", stock.getTickerSymbol(),
                        "stock_name", stock.getStockName()
                ))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getHistoricalData() {
        LocalDateTime threeDaysAgo = LocalDate.now().minusDays(3).atStartOfDay();
        Set<String> sp500Tickers = stockRepository.findAllTickerSymbols();
        List<Map<String, Object>> stockData = new ArrayList<>();

        for (String ticker : sp500Tickers) {
            try {
                Map<String, Object> yahooData = getYahooFinanceData(ticker, threeDaysAgo.toLocalDate(), threeDaysAgo.toLocalDate());
                if (yahooData == null) {
                    throw new RuntimeException("Yahoo Finance'den veri alinamadi");
                }
                double closePrice = extractPrice(yahooData);

                stockData.add(Map.of(
                        "date", threeDaysAgo.toLocalDate().toString(),
                        "symbol", ticker,
                        "close", closePrice,
                        "status", "Updated successfully"
                ));

                Thread.sleep(200); // Rate limiting

            } catch (Exception e) {
                stockData.add(Map.of(
                        "symbol", ticker,
                        "error", e.getMessage()
                ));
            }
        }
        return Map.of("result", stockData);
    }

    @Value("${stock.sp500.tickers}")
    private String sp500TickersString;

    @Transactional
    @Scheduled(cron = "0 */5 * * * *") // Her gun saat 1 de calisacak
    public void updateStockPrices() {
        LocalDateTime yesterday = LocalDate.now().minusDays(1).atStartOfDay();
        log.info("=== {} tarihli fiyat guncelleme islemi baslatildi ===", yesterday.toLocalDate());

        // S&P 500 listesini al
        Set<String> sp500Tickers = getSP500Components();
        log.info("Kayitli S&P 500 listesinden {} hisse alindi", sp500Tickers.size());

        int successCount = 0;
        int errorCount = 0;
        List<String> failedStocks = new ArrayList<>();
        List<String> newStocks = new ArrayList<>();
        List<String> updatedStocks = new ArrayList<>();

        for (String ticker : sp500Tickers) {
            try {
                // Yahoo Finance'den veriyi al
                Map<String, Object> yahooData = getYahooFinanceData(ticker, LocalDate.now(), LocalDate.now());
                if (yahooData == null) {
                    throw new RuntimeException("Yahoo Finance'den veri alinamadi");
                }

                double currentPrice = extractPrice(yahooData);
                Optional<Stock> stockOpt = stockRepository.findByTickerSymbol(ticker);

                if (stockOpt.isPresent()) {
                    // Hisse zaten dbde varsa guncelle
                    Stock stock = stockOpt.get();
                    // Onceki gunun fiyati stockprice listesinde var mi check
                    boolean exists = stockPriceRepository.existsByStockIdAndDate(stock, yesterday);
                    if (!exists) {
                        // Kayitli degilse dune ait stockprice tablosunda kayit olustur
                        StockPrice stockPrice = new StockPrice();
                        stockPrice.setStock(stock);
                        stockPrice.setPrice(currentPrice);
                        stockPrice.setDate(yesterday);
                        stockPriceRepository.save(stockPrice);
                        log.info("{} icin {} tarihli fiyat eklendi: {}", ticker, yesterday.toLocalDate(), currentPrice);
                    } else {
                        log.info("{} icin {} tarihli fiyat zaten kayitli, eklenmedi.", ticker, yesterday.toLocalDate());
                    }

                    // Stock listesindeki price'i guncelle
                    stockRepository.updateCurrentPrice(ticker, currentPrice);
                    updatedStocks.add(ticker);
                } else {
                    // Hisse dbde yoksa
                    Stock newStock = new Stock();
                    newStock.setTickerSymbol(ticker);
                    newStock.setStockName(extractStockName(yahooData));
                    newStock.setCurrentPrice(currentPrice);
                    stockRepository.save(newStock);
                    newStocks.add(ticker);
                }

                successCount++;
                Thread.sleep(200); // Rate limiting kesin bir deger degil internette tam olarak bulamadim

            } catch (Exception e) {
                errorCount++;
                failedStocks.add(ticker);
                log.error("{} icin hata olustu: {}", ticker, e.getMessage());
            }
        }

        logSummary(yesterday.toLocalDate(), sp500Tickers.size(), successCount,
                errorCount, failedStocks, newStocks, updatedStocks);
    }

    private Set<String> getSP500Components() {
        try {
            return new HashSet<>(Arrays.asList(sp500TickersString.split(",")));
        } catch (Exception e) {
            log.error("S&P 500 listesi alinamadi: {}", e.getMessage());
            return stockRepository.findAllTickerSymbols();
        }
    }

    private Map<String, Object> getYahooFinanceData(String ticker, LocalDate startDate, LocalDate endDate) {
        try {
            String url = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=1d&period1=%d&period2=%d",
                    ticker,
                    startDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond(),
                    endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond());

            ResponseEntity<Map> responseEntity = restTemplate.getForEntity(url, Map.class);

            if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
                log.error("{} icin Yahoo Finance'den veri alinamadi", ticker);
                return null;
            }

            return responseEntity.getBody();

        } catch (Exception e) {
            log.error("{} icin Yahoo Finance API hatasi: {}", ticker, e.getMessage());
            return null;
        }
    }

    private double extractPrice(Map<String, Object> data) {
        try {
            Map<String, Object> chart = (Map<String, Object>) data.get("chart");
            List<Map<String, Object>> results = (List<Map<String, Object>>) chart.get("result");

            if (results == null || results.isEmpty()) {
                throw new RuntimeException("Results verisi boş");
            }

            Map<String, Object> result = results.get(0);
            Map<String, Object> meta = (Map<String, Object>) result.get("meta");

            if (meta == null) {
                throw new RuntimeException("Meta verisi bulunamadı");
            }


            if (meta.containsKey("regularMarketPrice")) {
                return ((Number) meta.get("regularMarketPrice")).doubleValue();
            }

            throw new RuntimeException("Ne regularMarketPrice ne de previousClose değeri bulunamadı!");

        } catch (Exception e) {
            log.error("Fiyat verisi cikarilirken hata: {}", e.getMessage());
            return -1; // Eger veri yoksa -1 bu degistirilebilir
        }
    }


    private String extractStockName(Map<String, Object> data) {
        try {
            Map<String, Object> chart = (Map<String, Object>) data.get("chart");
            List<Map<String, Object>> results = (List<Map<String, Object>>) chart.get("result");

            if (results != null && !results.isEmpty()) {
                Map<String, Object> firstResult = results.get(0);
                Map<String, Object> meta = (Map<String, Object>) firstResult.get("meta");

                if (meta != null) {
                    if (meta.containsKey("shortName")) {
                        return (String) meta.get("shortName");
                    } else if (meta.containsKey("longName")) {
                        return (String) meta.get("longName");
                    }
                }
            }
            return "Unknown";
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private void logSummary(LocalDate date, int totalStocks, int successCount,
                            int errorCount, List<String> failedStocks,
                            List<String> newStocks, List<String> updatedStocks) {
        log.info("=== {} Tarihli Islem Ozeti ===", date);
        log.info("S&P 500'de Toplam: {}", totalStocks);
        log.info("Basarili Islem: {}", successCount); // Hisse sayisini yukselttigimizde bu kaldirilacak.
        log.info("Basarisiz Islem: {}", errorCount);

        if (!newStocks.isEmpty()) {
            log.info("Yeni Eklenen Hisseler: {}", String.join(", ", newStocks));
        }

        if (!updatedStocks.isEmpty()) {
            log.info("Guncelenen Hisseler: {}", String.join(", ", updatedStocks));
        }

        if (!failedStocks.isEmpty()) {
            log.info("Basarisiz Olan Hisseler: {}", String.join(", ", failedStocks));
        }

        log.info("=== Islem Tamamlandi ===");
    }
}