package com.inbest.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDTO
{
    private Integer commentId;
    private String username;
    private Integer userId;
    private String comment;
    private LocalDateTime createdAt;
    private String imageUrl;
    private String fullName;
}
