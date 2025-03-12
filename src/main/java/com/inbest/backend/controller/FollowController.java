package com.inbest.backend.controller;

import com.inbest.backend.model.User;
import com.inbest.backend.service.AuthenticationService;
import com.inbest.backend.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/follow")
public class FollowController
{

    @Autowired
    private FollowService followService;

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/{followingId}")
    public ResponseEntity<Map<String, Object>> followUser(@PathVariable Long followingId)
    {
        try
        {
            int userId = authenticationService.authenticate_user();
            followService.followUser((long) userId, followingId);

            Map<String, Object> response = Map.of(
                    "status", "success",
                    "message", "Followed successfully"
            );
            return ResponseEntity.ok(response);
        }
        catch (Exception e)
        {
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", "Failed to follow the user"
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @DeleteMapping("/unfollow/{followingId}")
    public ResponseEntity<Map<String, Object>> unfollowUser(@PathVariable Long followingId)
    {
        try
        {
            int userId = authenticationService.authenticate_user();
            followService.unfollowUser((long) userId, followingId);

            Map<String, Object> response = Map.of(
                    "status", "success",
                    "message", "Unfollowed successfully"
            );
            return ResponseEntity.ok(response);
        }
        catch (Exception e)
        {
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", "Failed to unfollow the user"
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<Map<String, Object>> getFollowing(@PathVariable Long userId)
    {
        try
        {
            List<User> followingList = followService.getFollowing(userId);
            Map<String, Object> response = Map.of(
                    "status", "success",
                    "message", "Following users fetched successfully",
                    "data", followingList
            );
            return ResponseEntity.ok(response);
        }
        catch (Exception e)
        {
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", "Failed to fetch following users"
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<Map<String, Object>> getFollowers(@PathVariable Long userId)
    {
        try
        {
            List<User> followersList = followService.getFollowers(userId);
            Map<String, Object> response = Map.of(
                    "status", "success",
                    "message", "Followers fetched successfully",
                    "data", followersList
            );
            return ResponseEntity.ok(response);
        }
        catch (Exception e)
        {
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", "Failed to fetch followers"
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/{userId}/followers/count")
    public ResponseEntity<Map<String, Object>> getFollowerCount(@PathVariable Long userId)
    {
        try
        {
            Long followerCount = followService.getFollowerCount(userId);
            Map<String, Object> response = Map.of(
                    "status", "success",
                    "message", "Follower count fetched successfully",
                    "data", followerCount
            );
            return ResponseEntity.ok(response);
        }
        catch (Exception e)
        {
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", "Failed to fetch follower count"
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // Future endpoint for following count
    /*
    @GetMapping("/{userId}/following/count")
    public ResponseEntity<Map<String, Object>> getFollowingCount(@PathVariable Long userId) {
        try {
            Long followingCount = followService.getFollowingCount(userId);
            Map<String, Object> response = Map.of(
                "status", "success",
                "message", "Following count fetched successfully",
                "data", followingCount
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "status", "error",
                "message", "Failed to fetch following count"
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    */
}