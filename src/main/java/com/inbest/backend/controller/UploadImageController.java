package com.inbest.backend.controller;
import com.inbest.backend.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/image")
public class UploadImageController
{
    private final S3Service s3Service;

    @PostMapping(value = "/upload",
            consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
   public ResponseEntity<String> uploadFile(
            @RequestHeader("Content-Type") String contentType,
            @RequestHeader("Content-Length") long contentLength,
            @RequestHeader("X-File-Name") String fileName,
            @RequestBody InputStream fileStream            ) {

       try{
           String eTag = s3Service.uploadFile(fileStream, fileName, contentType, contentLength);
           return new ResponseEntity<>("File uploaded successfully: " + eTag, HttpStatus.OK);
       } catch (IOException e) {
           return new ResponseEntity<>("Failed to upload image: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
       }
   }
}
