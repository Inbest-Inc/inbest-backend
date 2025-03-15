package com.inbest.backend.service;

import com.inbest.backend.dto.FileUploadResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileUploadResponseDTO uploadFile(MultipartFile multipartFile);
}