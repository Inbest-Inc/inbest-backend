package com.inbest.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioGetResponse
{
    private Integer portfolioId;
    private String portfolioName;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;
    private String visibility;
    private Integer userId;
}
