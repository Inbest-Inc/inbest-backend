package com.inbest.backend.controller;

import com.inbest.backend.dto.UserUpdateDTO;
import com.inbest.backend.dto.ChangePasswordDTO;
import com.inbest.backend.exception.UserNotFoundException;
import com.inbest.backend.model.User;
import com.inbest.backend.repository.UserRepository;
import com.inbest.backend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;

import java.util.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController
{

    private final UserService userService;
    private final UserRepository userRepository;
    private final FollowService followService;

    @PutMapping("/update")
    public ResponseEntity<?> updateUserNameAndSurname(@Valid @RequestBody UserUpdateDTO userUpdateDTO)
    {
        try
        {
            userService.updateUserNameAndSurname(userUpdateDTO);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "User information updated successfully"
            ));
        }
        catch (UserNotFoundException e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "An error occurred while updating user information"));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordDTO request)
    {
        try
        {
            userService.changePassword(request);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Password updated successfully"
            ));
        }
        catch (UserNotFoundException e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "error", e.getMessage()));
        }
        catch (IllegalArgumentException e)
        {
            if (e.getMessage().contains("must be at least 6 characters"))
            {
                return ResponseEntity.badRequest()
                        .body(Map.of("status", "error", "error", "New password must be at least 6 characters"));
            }
            else
            {
                return ResponseEntity.badRequest()
                        .body(Map.of("status", "error", "error", e.getMessage()));
            }
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "error", "An error occurred while changing password"));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String searchTerm, @AuthenticationPrincipal User currentUser)
    {
        try
        {
            if (currentUser == null)
            {
                Map<String, Object> errorResponse = Map.of(
                        "status", "error",
                        "message", "You must be logged in to search"
                );
                return ResponseEntity.status(401).body(errorResponse); // Unauthorized
            }
            List<User> users = userService.searchUsers(searchTerm);

            if (users.isEmpty())
            {
                Map<String, Object> errorResponse = Map.of(
                        "status", "error",
                        "message", "No users found"
                );
                return ResponseEntity.status(404).body(errorResponse);
            }

            List<Map<String, Object>> userList = new ArrayList<>();
            for (User user : users)
            {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("username", user.getUsername());
                userMap.put("fullName", user.getName() + " " + user.getSurname());
                userMap.put("imageUrl", user.getImageUrl());
                userList.add(userMap);
            }

            Map<String, Object> response = Map.of(
                    "status", "success",
                    "message", "Users found",
                    "data", userList
            );

            return ResponseEntity.ok(response);

        }
        catch (Exception e)
        {
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", "Failed to search users"
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkUserAuthentication(Authentication authentication)
    {
        try
        {
            if (authentication != null && authentication.isAuthenticated())
            {
                String currentUsername = authentication.getName();
                Optional<User> user = userRepository.findByUsername(currentUsername);
                if (user.isEmpty())
                {
                    Map<String, Object> errorResponse = Map.of(
                            "status", "error",
                            "message", "User not found."
                    );
                    return ResponseEntity.status(404).body(errorResponse);
                }
                String imageUrl = user.get().getImageUrl();
                boolean isVerified = user.get().isVerified();

                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("username", currentUsername);
                response.put("imageUrl", imageUrl);
                response.put("isVerified", isVerified);

                return ResponseEntity.ok(response);
            }
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", "Failed to fetch user information."
            );

            return ResponseEntity.status(403).body(errorResponse);
        }
        catch (Exception e)
        {
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", "Failed to fetch user information."
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getPublicUserInfo(@PathVariable String username, Authentication authentication)
    {
        try
        {
            Optional<User> user = userRepository.findByUsername(username);
            if (!user.isPresent())
            {
                Map<String, Object> errorResponse = Map.of(
                        "status", "error",
                        "message", "User not found"
                );
                return ResponseEntity.status(404).body(errorResponse);
            }
            Long followerCount = followService.getFollowerCount(username);
            Long followingCount = followService.getFollowingCount(username);
            String imageUrl = user.get().getImageUrl();
            String fullName = userService.getPublicUserInfo(username);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("fullName", fullName);
            response.put("followerCount", followerCount);
            response.put("followingCount", followingCount);
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
}
