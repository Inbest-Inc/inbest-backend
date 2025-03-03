package com.inbest.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileUploadResponseDTO {
    private String filePath;
    private LocalDateTime dateTime;
}