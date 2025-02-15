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
import org.springframework.beans.factory.annotation.Value;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
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

    @Value("${stock.deactivate-removed:true}")  // application.properties'den al, varsayılan true
    private boolean deactivateRemovedStocks;

    @Value("${stock.sp500.tickers}")
    private String sp500TickersString;

    @Transactional
    @Scheduled(cron = "0 */5 * * * *")  // Test için: Her 5 dakikada bir
    public void updateDailyStockData() {
        LocalDate today = LocalDate.now();
        log.info("=== {} Tarihli Hisse Senedi Guncelleme Islemi Baslatildi ===", today);

        // S&P 500 hisselerini Yahoo Finance'den al
        Set<String> sp500Tickers = getSP500Components();
        log.info("S&P 500'den {} hisse alindi", sp500Tickers.size());

        // Mevcut DB'deki aktif hisseleri al
        Set<String> existingTickers = stockRepository.findAllActiveTickers();
        log.info("Veritabaninda {} aktif hisse mevcut", existingTickers.size());

        // S&P 500'den çıkarılan hisseleri bul
        Set<String> removedTickers = new HashSet<>(existingTickers);
        removedTickers.removeAll(sp500Tickers);

        int successCount = 0;
        int errorCount = 0;
        List<String> failedStocks = new ArrayList<>();
        List<String> newStocks = new ArrayList<>();
        List<String> deactivatedStocks = new ArrayList<>();

        // Çıkarılan hisseleri işle
        if (!removedTickers.isEmpty() && deactivateRemovedStocks) {
            for (String ticker : removedTickers) {
                try {
                    stockRepository.deactivateStock(ticker);
                    deactivatedStocks.add(ticker);
                    log.info("{} S&P 500'den cikarildigi icin devre disi birakildi.", ticker);
                } catch (Exception e) {
                    log.error("{} devre disi birakilirken hata olustu", ticker);
                }
            }
        }

        // Tüm S&P 500 hisselerini işle
        for (String ticker : sp500Tickers) {
            try {
                Map<String, Object> currentData = getYahooFinanceData(ticker, LocalDate.now(), LocalDate.now());
                Stock existingStock = stockRepository.findByTickerSymbol(ticker).orElse(null);

                if (existingStock != null) {
                    // Eğer devre dışı bırakılmışsa tekrar aktifleştir
                    if (!existingStock.isActive()) {
                        existingStock.setActive(true);
                        log.info("{} tekrar S&P 500'e eklendigi icin aktiflestirildi", ticker);
                    }
                    updateExistingStock(existingStock, currentData);
                    successCount++;
                } else {
                    addNewStockWithHistory(ticker, currentData);
                    newStocks.add(ticker);
                    successCount++;
                }

                Thread.sleep(200);

            } catch (Exception e) {
                errorCount++;
                failedStocks.add(ticker);
                log.error("{} icin hata: {}", ticker, e.getMessage());
            }
        }

        // Özet rapor
        log.info("=== {} Tarihli Islem Ozeti ===", today);
        log.info("S&P 500'de Toplam: {}", sp500Tickers.size());
        log.info("DB'de Aktif: {}", existingTickers.size());
        log.info("Basarili Islem: {}", successCount);
        log.info("Basarisiz Islem: {}", errorCount);

        if (!newStocks.isEmpty()) {
            log.info("Yeni Eklenen Hisseler: {}", String.join(", ", newStocks));
        }

        if (!deactivatedStocks.isEmpty()) {
            log.info("S&P 500'den Cikarilan Hisseler: {}", String.join(", ", deactivatedStocks));
        }

        if (!failedStocks.isEmpty()) {
            log.info("Basarisiz Olan Hisslere: {}", String.join(", ", failedStocks));
        }

        log.info("=== Islem Tamamlandi ===");
    }

    private Set<String> getSP500Components() {
        try {
            return new HashSet<>(Arrays.asList(sp500TickersString.split(",")));
        } catch (Exception e) {
            log.error("S&P 500 listesi alınamadı: {}", e.getMessage());
            return stockRepository.findAllTickerSymbols();
        }
    }

    private void updateExistingStock(Stock stock, Map<String, Object> data) {
        try {
            double currentPrice = extractPrice(data);
            LocalDateTime currentDate = extractDate(data).withHour(0).withMinute(0).withSecond(0).withNano(0);

            // Stock tablosunu guncelle
            stock.setCurrentPrice(currentPrice);
            stockRepository.save(stock);

            // StockPrice tablosuna ekle (eğer o tarih için veri yoksa)
            //if (!stockPriceRepository.existsByStockIdAndDate(stock, currentDate)) { Test icin devre disi
                StockPrice stockPrice = new StockPrice();
                stockPrice.setStock(stock);
                stockPrice.setPrice(currentPrice);
                stockPrice.setDate(currentDate);
                stockPriceRepository.save(stockPrice);
            //}

        } catch (Exception e) {
            log.error("{} güncellenirken hata olustu", stock.getTickerSymbol());
            throw e;
        }
    }

    private void addNewStockWithHistory(String ticker, Map<String, Object> currentData) {
        try {
            Stock newStock = new Stock();
            newStock.setTickerSymbol(ticker);
            newStock.setStockName(extractStockName(currentData));
            newStock.setCurrentPrice(extractPrice(currentData));
            Stock savedStock = stockRepository.save(newStock);

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusYears(1);

            Map<String, Object> historicalData = getYahooFinanceData(ticker, startDate, endDate);
            List<StockPrice> historicalPrices = extractHistoricalPrices(historicalData, savedStock);

            stockPriceRepository.saveAll(historicalPrices);

            // Veri bütünlüğü kontrolü
            List<StockPrice> lastPrices = stockPriceRepository.findFirstByStockOrderByDateDesc(savedStock);
            if (lastPrices.isEmpty() || !lastPrices.get(0).getDate().toLocalDate().equals(endDate)) {
                log.warn("{} için son gün verisi eksik", ticker);
            }

        } catch (Exception e) {
            log.error("{} eklenirken hata oluştu", ticker);
            throw e;
        }
    }

    private Map<String, Object> getYahooFinanceData(String ticker, LocalDate startDate, LocalDate endDate) {
        String url = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=1d&period1=%d&period2=%d",
                ticker,
                startDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond(),
                endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond());

        ResponseEntity<Map> responseEntity = restTemplate.getForEntity(url, Map.class);
        return responseEntity.getBody();
    }

    private double extractPrice(Map<String, Object> data) {
        try {
            Map<String, Object> chart = (Map<String, Object>) data.get("chart");
            List<Map<String, Object>> results = (List<Map<String, Object>>) chart.get("result");

            if (results != null && !results.isEmpty()) {
                Map<String, Object> firstResult = results.get(0);
                Map<String, Object> indicators = (Map<String, Object>) firstResult.get("indicators");
                List<Map<String, Object>> quotes = (List<Map<String, Object>>) indicators.get("quote");

                if (quotes != null && !quotes.isEmpty()) {
                    Map<String, Object> quote = quotes.get(0);
                    List<Double> close = (List<Double>) quote.get("close");

                    if (close != null && !close.isEmpty()) {
                        return close.get(close.size() - 1); // En son kapanış fiyatı
                    }
                }
            }
            throw new RuntimeException("Fiyat verisi bulunamadı");
        } catch (Exception e) {
            throw new RuntimeException("Fiyat parse edilirken hata: " + e.getMessage());
        }
    }

    private LocalDateTime extractDate(Map<String, Object> data) {
        try {
            Map<String, Object> chart = (Map<String, Object>) data.get("chart");
            List<Map<String, Object>> results = (List<Map<String, Object>>) chart.get("result");

            if (results != null && !results.isEmpty()) {
                Map<String, Object> firstResult = results.get(0);
                List<Integer> timestamps = (List<Integer>) firstResult.get("timestamp");

                if (timestamps != null && !timestamps.isEmpty()) {
                    return Instant.ofEpochSecond(timestamps.get(timestamps.size() - 1).longValue())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                }
            }
            throw new RuntimeException("Tarih verisi bulunamadı");
        } catch (Exception e) {
            throw new RuntimeException("Tarih parse edilirken hata: " + e.getMessage());
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

    private List<StockPrice> extractHistoricalPrices(Map<String, Object> data, Stock stock) {
        List<StockPrice> prices = new ArrayList<>();
        try {
            Map<String, Object> chart = (Map<String, Object>) data.get("chart");
            List<Map<String, Object>> results = (List<Map<String, Object>>) chart.get("result");

            if (results != null && !results.isEmpty()) {
                Map<String, Object> firstResult = results.get(0);
                List<Integer> timestamps = (List<Integer>) firstResult.get("timestamp");
                Map<String, Object> indicators = (Map<String, Object>) firstResult.get("indicators");
                List<Map<String, Object>> quotes = (List<Map<String, Object>>) indicators.get("quote");

                if (quotes != null && !quotes.isEmpty() && timestamps != null) {
                    Map<String, Object> quote = quotes.get(0);
                    List<Double> closePrices = (List<Double>) quote.get("close");

                    for (int i = 0; i < timestamps.size(); i++) {
                        if (closePrices.get(i) != null) {  // null fiyatları atla
                            StockPrice stockPrice = new StockPrice();
                            stockPrice.setStock(stock);
                            stockPrice.setPrice(closePrices.get(i));
                            stockPrice.setDate(
                                    Instant.ofEpochSecond(timestamps.get(i).longValue())
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDateTime()
                            );
                            prices.add(stockPrice);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Geçmiş veriler parse edilirken hata: {} - {}", stock.getTickerSymbol(), e.getMessage());
        }
        return prices;
    }
    public List<Stock> findAllStocks() {
        return stockRepository.findAllStocks();
    }
}
