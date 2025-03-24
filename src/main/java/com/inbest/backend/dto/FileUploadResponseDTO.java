package com.inbest.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadResponseDTO
{
    private String status;
    private String message;
    private String filePath;
    private LocalDateTime dateTime;

    public FileUploadResponseDTO(String error, String fileIsEmpty, Object o, Object o1)
    {

    }
}