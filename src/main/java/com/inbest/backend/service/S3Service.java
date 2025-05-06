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
import java.util.List;
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

        Set<String> allowedTypes = Set.of("image/jpeg", "image/png", "image/jpg", "image/gif", "image/webp");
        Set<String> allowedExtensions = Set.of("jpg","jpeg", "png", "gif", "webp");
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
                return new FileUploadResponseDTO("error", "Invalid file type. Only JPEG, JPG, PNG, GIF, WEBP are allowed.", null, null);
            }

            if (multipartFile.getSize() > maxSize)
            {
                return new FileUploadResponseDTO("error", "File size exceeds the 5 MB limit.", null, null);
            }

            String originalFilename = multipartFile.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains("."))
            {
                return new FileUploadResponseDTO("error", "Invalid file name.", null, null);
            }

            String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
            if (!allowedExtensions.contains(extension))
            {
                return new FileUploadResponseDTO("error", "Invalid file extension.", null, null);
            }

            String filePath = userId + "." + extension;

            deleteExistingImage(userId);

            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentType(contentType);
            meta.setContentLength(multipartFile.getSize());
            meta.setCacheControl("no-cache, no-store, must-revalidate");

            s3Client.putObject(bucketName, filePath, multipartFile.getInputStream(), meta);

            String username = userRepository.findById((long) userId)
                    .map(User::getUsername)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

            String imageUrl = getImageUrl(username);

            User user = userRepository.findById((long) userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            user.setImageUrl(imageUrl);
            userRepository.save(user);

            return new FileUploadResponseDTO("success", "File uploaded successfully", imageUrl, LocalDateTime.now());
        }
        catch (IOException e)
        {
            log.error("IOException during upload: {}", e.getMessage());
            return new FileUploadResponseDTO("error", "Error during file upload: " + e.getMessage(), null, null);
        }
        catch (Exception e)
        {
            log.error("Unexpected error during upload: {}", e.getMessage());
            return new FileUploadResponseDTO("error", "Unexpected error: " + e.getMessage(), null, null);
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

    private void deleteExistingImage(int userId)
    {
        ListObjectsV2Result objects =
                s3Client.listObjectsV2(bucketName, userId + ".");
        List<S3ObjectSummary> summaries = objects.getObjectSummaries();

        if (!summaries.isEmpty())
        {
            DeleteObjectsRequest deleteReq = new DeleteObjectsRequest(bucketName)
                    .withKeys(
                            summaries.stream()
                                    .map(S3ObjectSummary::getKey)
                                    .toArray(String[]::new));
            s3Client.deleteObjects(deleteReq);
        }
    }
}
