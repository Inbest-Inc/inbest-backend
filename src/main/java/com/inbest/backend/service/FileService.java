package com.inbest.backend.service;

import com.inbest.backend.dto.FileDataDTO;
import com.inbest.backend.dto.FileUploadResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;

public interface FileService {
    FileUploadResponseDTO uploadFile(MultipartFile multipartFile);

    FileDataDTO getImage(Integer userId) throws FileNotFoundException;

}