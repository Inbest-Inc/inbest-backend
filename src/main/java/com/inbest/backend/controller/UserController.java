package com.inbest.backend.controller;

import com.inbest.backend.dto.UserUpdateDTO;
import com.inbest.backend.dto.ChangePasswordDTO;
import com.inbest.backend.exception.UserNotFoundException;
import com.inbest.backend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<?> getPublicUserInfo(@PathVariable String username, Authentication authentication)
    {
        try
        {
            Long followerCount = followService.getFollowerCount(username);
            String imageUrl = s3Service.getImageUrl(username);
            String fullName = userService.getPublicUserInfo(username);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "User information fetched successfully");
            response.put("fullName", fullName);
            response.put("followerCount", followerCount);
            response.put("imageUrl", imageUrl);

            if (authentication != null && authentication.isAuthenticated())
            {
                String currentUsername = authentication.getName();

                if (!currentUsername.equals(username))
                {
                    boolean isFollowing = followService.isFollowing(currentUsername, username);
                    response.put("following", isFollowing);
                }
                else
                {
                    response.put("following", false);
                }
            }
            else
            {
                response.put("following", false); // authorize degilse following: false
            }

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
