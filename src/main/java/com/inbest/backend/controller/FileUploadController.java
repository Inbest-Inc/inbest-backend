package com.inbest.backend.controller;

import com.inbest.backend.dto.FileDataDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import com.inbest.backend.dto.FileUploadResponseDTO;
import com.inbest.backend.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/s3")
@RequiredArgsConstructor
public class FileUploadController
{

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponseDTO> uploadFile(@RequestParam("file") MultipartFile file)
    {
        FileUploadResponseDTO response = fileService.uploadFile(file);
        if ("error".equals(response.getStatus()))
        {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get-image/{username}")
    public ResponseEntity<?> getImage(@PathVariable String username)
    {
        try
        {
            FileDataDTO fileData = fileService.getImage(username);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(fileData.getContentType()));
            headers.setContentLength(fileData.getData().length);

            return new ResponseEntity<>(fileData.getData(), headers, HttpStatus.OK);
        }
        catch (FileNotFoundException e)
        {
            Map<String, String> errorResponse = Map.of(
                    "status", "error",
                    "message", "Image not found for: " + username
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
        catch (Exception e)
        {
            Map<String, String> errorResponse = Map.of(
                    "status", "error",
                    "message", "Unexpected error while retrieving the image"
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
