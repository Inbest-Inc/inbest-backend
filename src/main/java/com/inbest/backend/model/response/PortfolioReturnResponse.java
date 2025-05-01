package com.inbest.backend.model.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioReturnResponse {
    private String date;
    private BigDecimal portfolioReturn;
    private BigDecimal goldReturn;
    private BigDecimal spyReturn;

    public void setDate(LocalDateTime dateTime) {
        if (dateTime != null) {
            this.date = dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        }
    }
}