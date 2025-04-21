package com.inbest.backend.scheduler;

import com.inbest.backend.model.Stock;
import com.inbest.backend.model.position.PortfolioStockMetric;
import com.inbest.backend.repository.PortfolioStockMetricRepository;
import com.inbest.backend.repository.StockRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PositionMetricsScheduler {

    private final PortfolioStockMetricRepository portfolioStockMetricRepository;
    private final StockRepository stockRepository;

   // @Scheduled(cron = "0 */5 * * * *") Test icin her 5 dakikada bir
    @Scheduled(cron = "0 5 * * * *") // Her saatin 5. dakikası. 10.05 11.05 12.05 etc
    @Transactional
    public void updateTodayPositionMetrics() {
        LocalDateTime today = LocalDate.now().atStartOfDay();
        LocalDateTime yesterday = today.minusDays(1);

        List<PortfolioStockMetric> todaysMetrics = portfolioStockMetricRepository.findByDate(today);

        if (todaysMetrics.isEmpty()) {
            List<PortfolioStockMetric> yesterdaysMetrics = portfolioStockMetricRepository.findByDate(yesterday);
            if (yesterdaysMetrics.isEmpty()) {
                return;
            }
            for (PortfolioStockMetric yesterdayMetric : yesterdaysMetrics) {
                PortfolioStockMetric todayMetric = PortfolioStockMetric.builder()
                        .portfolioId(yesterdayMetric.getPortfolioId())
                        .stockId(yesterdayMetric.getStockId())
                        .date(today)
                        .quantity(yesterdayMetric.getQuantity())
                        .averageCost(yesterdayMetric.getAverageCost())
                        .currentValue(yesterdayMetric.getCurrentValue())
                        .totalReturn(yesterdayMetric.getTotalReturn())
                        .positionWeight(yesterdayMetric.getPositionWeight())
                        .lastTransactionType(yesterdayMetric.getLastTransactionType())
                        .lastTransactionDate(yesterdayMetric.getLastTransactionDate())
                        .lastUpdated(LocalDateTime.now())
                        .build();

                portfolioStockMetricRepository.save(todayMetric);
            }

        } else {

            for (PortfolioStockMetric metric : todaysMetrics) {
                Integer stockId = metric.getStockId();
                Double quantity = metric.getQuantity();
                BigDecimal avgCost = metric.getAverageCost();

                Stock stock = stockRepository.findById(Long.valueOf(stockId)).orElse(null);
                if (stock == null) continue;

                BigDecimal currentPrice = BigDecimal.valueOf(stock.getCurrentPrice());
                BigDecimal currentValue = currentPrice;
                BigDecimal totalReturn = BigDecimal.ZERO;

                if (avgCost.compareTo(BigDecimal.ZERO) > 0) {
                    totalReturn = currentPrice
                            .divide(avgCost, 2, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .subtract(BigDecimal.valueOf(100));
                }

                metric.setCurrentValue(currentValue);
                metric.setTotalReturn(totalReturn);
                metric.setLastUpdated(LocalDateTime.now());

                portfolioStockMetricRepository.save(metric);
            }

            List<Integer> allPortfolioIds = portfolioStockMetricRepository.findAllPortfolioIdsDistinct();
            for (Integer pid : allPortfolioIds) {
                try {
                    recalculateWeights(pid, today);
                } catch (Exception e) {
                    System.out.println("Weight hesaplanırken hata: " + e.getMessage());
                }
            }
            System.out.println("PositionMetrics updated @ " + LocalDateTime.now());
        }
    }

    private void recalculateWeights(Integer portfolioId, LocalDateTime date) {
        List<PortfolioStockMetric> metrics = portfolioStockMetricRepository.findByPortfolioIdAndDate(portfolioId, date);
        BigDecimal total = metrics.stream()
                .map(m -> m.getAverageCost().multiply(BigDecimal.valueOf(m.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (PortfolioStockMetric metric : metrics) {
            BigDecimal weight = BigDecimal.ZERO;
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                weight = metric.getAverageCost()
                        .multiply(BigDecimal.valueOf(metric.getQuantity()))
                        .divide(total, 4, RoundingMode.HALF_UP);
            }
            metric.setPositionWeight(weight);
            portfolioStockMetricRepository.save(metric);
        }
    }
}
