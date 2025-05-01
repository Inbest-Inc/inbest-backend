package com.inbest.backend.repository;

import com.inbest.backend.model.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {
    
    @Query(value = """
            WITH daily_prices AS (
                SELECT 
                    date_trunc('day', date) as day,
                    ticker_symbol,
                    FIRST_VALUE(price) OVER (PARTITION BY date_trunc('day', date), ticker_symbol ORDER BY date) as first_price,
                    LAST_VALUE(price) OVER (PARTITION BY date_trunc('day', date), ticker_symbol ORDER BY date
                        ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) as last_price
                FROM stockprice
                WHERE ticker_symbol = :ticker
                AND date BETWEEN :startDate AND :endDate
            )
            SELECT 
                day::timestamp as date,
                CASE 
                    WHEN first_price IS NOT NULL AND first_price != 0 
                    THEN ((last_price - first_price) / first_price) * 100
                    ELSE NULL 
                END as daily_return
            FROM daily_prices
            GROUP BY day, ticker_symbol, first_price, last_price
            ORDER BY day
            """, nativeQuery = true)
    List<Map<String, Object>> findDailyReturnsByTickerAndDateRange(
            @Param("ticker") String ticker,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query(value = """
            WITH monthly_prices AS (
                SELECT 
                    date_trunc('month', date) as month,
                    ticker_symbol,
                    FIRST_VALUE(price) OVER (PARTITION BY date_trunc('month', date), ticker_symbol ORDER BY date) as first_price,
                    LAST_VALUE(price) OVER (PARTITION BY date_trunc('month', date), ticker_symbol ORDER BY date
                        ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) as last_price
                FROM stockprice
                WHERE ticker_symbol = :ticker
                AND date BETWEEN :startDate AND :endDate
            )
            SELECT 
                month::timestamp as date,
                CASE 
                    WHEN first_price IS NOT NULL AND first_price != 0 
                    THEN ((last_price - first_price) / first_price) * 100
                    ELSE NULL 
                END as monthly_return
            FROM monthly_prices
            GROUP BY month, ticker_symbol, first_price, last_price
            ORDER BY month
            """, nativeQuery = true)
    List<Map<String, Object>> findMonthlyReturnsByTickerAndDateRange(
            @Param("ticker") String ticker,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

} 