package com.inbest.backend.model.response;

import com.inbest.backend.dto.PortfolioDTO;
import com.inbest.backend.dto.UserDTO;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BestPortfolioResponse {

    private PortfolioMetricResponse portfolioMetric;
    private UserDTO user;
    private PortfolioDTO portfolioDTO;

}
