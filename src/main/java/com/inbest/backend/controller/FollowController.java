package com.inbest.backend.controller;

import com.inbest.backend.model.User;
import com.inbest.backend.service.AuthenticationService;
import com.inbest.backend.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follow")
public class FollowController
{

    @Autowired
    private FollowService followService;

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/{followingId}")
    public ResponseEntity<String> followUser(@PathVariable Long followingId)
    {
        int userId = authenticationService.authenticate_user();
        followService.followUser((long) userId, followingId);
        return ResponseEntity.ok("Followed successfully");
    }

    @DeleteMapping("/unfollow/{followingId}")
    public ResponseEntity<String> unfollowUser(@PathVariable Long followingId)
    {
        int userId = authenticationService.authenticate_user();
        followService.unfollowUser((long) userId, followingId);
        return ResponseEntity.ok("Unfollowed successfully");
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<List<User>> getFollowing(@PathVariable Long userId)
    {
        return ResponseEntity.ok(followService.getFollowing(userId));
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<User>> getFollowers(@PathVariable Long userId)
    {
        return ResponseEntity.ok(followService.getFollowers(userId));
    }

    @GetMapping("/{userId}/followers/count")
    public ResponseEntity<Long> getFollowerCount(@PathVariable Long userId)
    {
        return ResponseEntity.ok(followService.getFollowerCount(userId));
    }

    //maybe for future
    /*@GetMapping("/{userId}/following/count")
    public ResponseEntity<Long> getFollowingCount(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowingCount(userId));
    }*/
}