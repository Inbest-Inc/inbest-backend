package com.inbest.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDTO {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String stockSymbol;
    private int likeCount;
    private int commentCount;
    private boolean isTrending;
    private UserDTO userDTO;
    private InvestmentActivityResponseDTO investmentActivityResponseDTO;
    private boolean isLiked;
}