package com.inbest.backend.repository;

import com.inbest.backend.model.StockPrice;
import com.inbest.backend.model.StockPriceId;
import com.inbest.backend.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockPriceRepository extends JpaRepository<StockPrice, StockPriceId> {

    // stock_id direkt olarak kullanılacak
    @Query("SELECT CASE WHEN COUNT(sp) > 0 THEN true ELSE false END FROM StockPrice sp " +
            "WHERE sp.stock = :stock AND sp.date = :date")
    boolean existsByStockIdAndDate(@Param("stock") Stock stock, @Param("date") LocalDateTime date);
}