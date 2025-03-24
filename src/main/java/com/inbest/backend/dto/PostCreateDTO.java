package com.inbest.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateDTO {
    @NotBlank(message = "Content cannot be empty")
    private String content;

    @NotNull(message = "Investment activity ID cannot be null")
    private Long investmentActivityId;
} 