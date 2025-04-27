package com.inbest.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class DonutChartDTO {
    String tickerSymbol;
    BigDecimal position_weight;
}
