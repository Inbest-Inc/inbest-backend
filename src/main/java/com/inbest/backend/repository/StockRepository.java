package com.inbest.backend.repository;

import com.inbest.backend.model.Stock;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface StockRepository extends JpaRepository<Stock, Long>
{
    @Modifying
    @Transactional
    @Query("UPDATE Stock s SET s.currentPrice = :price WHERE s.tickerSymbol = :ticker")
    int updateCurrentPrice(@Param("ticker") String ticker, @Param("price") double price);
    @Query("SELECT s.tickerSymbol FROM Stock s")
    Set<String> findAllTickerSymbols();

    Optional<Stock> findByTickerSymbol(String tickerSymbol);
}
