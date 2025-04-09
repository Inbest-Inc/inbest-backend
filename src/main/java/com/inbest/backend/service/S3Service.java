package com.inbest.backend.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.inbest.backend.dto.FileDataDTO;
import com.inbest.backend.dto.FileUploadResponseDTO;
import com.inbest.backend.exception.UserNotFoundException;
import com.inbest.backend.model.User;
import com.inbest.backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.time.LocalDateTime;
import java.util.Set;

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

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthenticationService authenticationService;

    @PostConstruct
    private void initialize()
    {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.EU_CENTRAL_1)
                .build();
    }

    @Override
    public FileUploadResponseDTO uploadFile(MultipartFile multipartFile)
    {
        int userId = authenticationService.authenticate_user();
        String filePath;

        Set<String> allowedTypes = Set.of("image/jpeg", "image/png", "image/jpg", "image/gif", "image/webp");
        Set<String> allowedExtensions = Set.of("jpeg", "jpg", "png", "gif", "webp");
        long maxSize = 5 * 1024 * 1024;

        try
        {
            if (multipartFile.isEmpty())
            {
                return new FileUploadResponseDTO("error", "File is empty", null, null);
            }

            String contentType = multipartFile.getContentType();
            if (!allowedTypes.contains(contentType))
            {
                return new FileUploadResponseDTO("error", "Invalid file type. Only JPEG, PNG and JPG are allowed.", null, null);
            }

            if (multipartFile.getSize() > maxSize)
            {
                return new FileUploadResponseDTO("error", "File size exceeds the maximum limit of 5MB.", null, null);
            }

            String originalFilename = multipartFile.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains("."))
            {
                return new FileUploadResponseDTO("error", "Invalid file name.", null, null);
            }

            String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
            if (!allowedExtensions.contains(extension))
            {
                return new FileUploadResponseDTO("error", "Invalid file extension. Only .jpeg and .png files are allowed.", null, null);
            }

            filePath = userId + "." + extension;

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(contentType);
            objectMetadata.setContentLength(multipartFile.getSize());
            objectMetadata.setCacheControl("no-cache, no-store, must-revalidate");
            s3Client.putObject(bucketName, filePath, multipartFile.getInputStream(), objectMetadata);

            return new FileUploadResponseDTO("success", "File uploaded successfully", filePath, LocalDateTime.now());

        }
        catch (IOException e)
        {
            log.error("IOException occurred: {}", e.getMessage());
            return new FileUploadResponseDTO("error", "Error occurred during file upload: " + e.getMessage(), null, null);
        }
        catch (Exception e)
        {
            log.error("Error occurred during file upload: {}", e.getMessage());
            return new FileUploadResponseDTO("error", "Unexpected error occurred during file upload: " + e.getMessage(), null, null);
        }
    }


    @Override
    public FileDataDTO getImage(String username) throws FileNotFoundException
    {
        try
        {
            int userId = userRepository.findByUsername(username)
                    .map(User::getId)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(bucketName)
                    .withPrefix(userId + ".")
                    .withMaxKeys(1);

            ListObjectsV2Result result = s3Client.listObjectsV2(request);

            if (result.getObjectSummaries().isEmpty())
            {
                throw new FileNotFoundException("No image found for user with an username: " + username);
            }

            String filePath = result.getObjectSummaries().get(0).getKey();
            S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, filePath));

            String contentType = s3Object.getObjectMetadata().getContentType();

            return new FileDataDTO(IOUtils.toByteArray(s3Object.getObjectContent()), contentType);
        }
        catch (FileNotFoundException e)
        {
            log.error("File not found: {}", e.getMessage());
            throw e;
        }
        catch (Exception e)
        {
            log.error("Error fetching image: {}", e.getMessage());
            throw new RuntimeException("Could not fetch image: " + e.getMessage());
        }
    }

    public String getImageUrl(String username) throws FileNotFoundException
    {
            int userId = userRepository.findByUsername(username)
                    .map(User::getId)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(bucketName)
                    .withPrefix(userId + ".")
                    .withMaxKeys(1);

            ListObjectsV2Result result = s3Client.listObjectsV2(request);

            if (!result.getObjectSummaries().isEmpty())
            {
                String filePath = result.getObjectSummaries().get(0).getKey();
                String imageUrl = s3Client.getUrl(bucketName, filePath).toString();

                return imageUrl + "?t=" + System.currentTimeMillis();
            }
            return null;
    }
}
