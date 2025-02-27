package com.inbest.backend.service;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.io.InputStream;

@Service
public class S3Service
{
    private final S3Client s3Client;

    public S3Service(S3Client s3Client)
    {
        this.s3Client = s3Client;
    }

    private final String bucketName = "inbest-backend";

    public String uploadFile(InputStream inputStream, String fileName, String contentType, long contentLength) throws IOException
    {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .contentLength(contentLength)
                .build();
        PutObjectResponse response = s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromInputStream(inputStream, contentLength));
        return response.eTag();
    }
}
