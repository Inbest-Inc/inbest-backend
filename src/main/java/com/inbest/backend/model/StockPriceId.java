package com.inbest.backend.model;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockPriceId implements Serializable
{
    private String tickerSymbol;
    private LocalDateTime date;
}
