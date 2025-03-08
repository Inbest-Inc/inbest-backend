package com.inbest.backend.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.inbest.backend.dto.FileUploadResponseDTO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class S3Service implements FileService
{

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Value("${aws.s3.accessKey}")
    private String accessKey;

    @Value("${aws.s3.secretKey}")
    private String secretKey;

    private AmazonS3 s3Client;

    @PostConstruct
    private void initialize() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.EU_CENTRAL_1)
                .build();
    }

    @Override
    public FileUploadResponseDTO uploadFile(MultipartFile multipartFile) {
        FileUploadResponseDTO fileUploadResponse = new FileUploadResponseDTO();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String todayDate = dateTimeFormatter.format(LocalDate.now());
        String filePath = "";
        try {
            if (multipartFile.isEmpty()) {
                throw new RuntimeException("File is empty");
            }

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(multipartFile.getContentType());
            objectMetadata.setContentLength(multipartFile.getSize());
            filePath = todayDate + "/" + multipartFile.getOriginalFilename();

            s3Client.putObject(bucketName, filePath, multipartFile.getInputStream(), objectMetadata);

            fileUploadResponse.setFilePath(filePath);
            fileUploadResponse.setDateTime(LocalDateTime.now());
        } catch (IOException e) {
            log.error("IOException occurred: {}", e.getMessage());
            throw new RuntimeException("Error occurred in file upload: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error occurred during file upload: {}", e.getMessage());
            throw new RuntimeException("Unexpected error occurred during file upload: " + e.getMessage());
        }
        return fileUploadResponse;
    }
}