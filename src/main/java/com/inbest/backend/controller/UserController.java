package com.inbest.backend.controller;

import com.inbest.backend.dto.UserUpdateDTO;
import com.inbest.backend.dto.ChangePasswordDTO;
import com.inbest.backend.exception.UserNotFoundException;
import com.inbest.backend.service.FileService;
import com.inbest.backend.service.FollowService;
import com.inbest.backend.service.S3Service;
import com.inbest.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController
{

    private final UserService userService;
    private final S3Service s3Service;
    private final FollowService followService;

    @GetMapping("/{username}")
    public ResponseEntity<?> getPublicUserInfo(@PathVariable String username)
    {
        try
        {
            Long followerCount = followService.getFollowerCount(username);

            String imageUrl = s3Service.getImageUrl(username);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "User information fetched successfully");
            response.put("fullName", userService.getPublicUserInfo(username));
            response.put("followerCount", followerCount);
            response.put("imageUrl", imageUrl);

            return ResponseEntity.ok(response);
        }
        catch (Exception e)
        {
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", "Failed to fetch user information"
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUserNameAndSurname(@Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        try {
            userService.updateUserNameAndSurname(userUpdateDTO);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "User information updated successfully"
            ));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "An error occurred while updating user information"));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordDTO request) {
        try {
            userService.changePassword(request);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Password updated successfully"
            ));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("must be at least 6 characters")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("status", "error", "error", "New password must be at least 6 characters"));
            }  else {
                return ResponseEntity.badRequest()
                        .body(Map.of("status", "error", "error", e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "error", "An error occurred while changing password"));
        }
    }
}
