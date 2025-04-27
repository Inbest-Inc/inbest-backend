package com.inbest.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BestPortfolioResponseDTO
{
    private String portfolioName;
    private String visibility;
    private Integer holdingCount;
}
