package com.inbest.backend.model.response;

import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioRankResponse
{
    private int rank;
    private int total;
}
