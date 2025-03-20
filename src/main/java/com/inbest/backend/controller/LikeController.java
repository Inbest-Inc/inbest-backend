package com.inbest.backend.controller;

import com.inbest.backend.model.Like;
import com.inbest.backend.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    @PostMapping("/posts/{postId}")
    public ResponseEntity<Map<String,String>> likePost(
            @PathVariable Long postId) {
        Map<String, String> response = new HashMap<>();
        likeService.likePost(postId);
        response.put("postId", String.valueOf(postId));
        response.put("status", "success");
        response.put("message", "Post liked successfully!");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Map<String, String>> unlikePost(@PathVariable Long postId) {
        likeService.unlikePost(postId);
        Map<String, String> response = new HashMap<>();
        response.put("postId", String.valueOf(postId));
        response.put("status", "success");
        response.put("message", "Post unliked successfully!");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/posts/{postId}/count")
    public ResponseEntity<Map<String, Object>> getLikeCount(@PathVariable Long postId) {
        long count = likeService.getLikeCount(postId);
        Map<String, Object> response = new HashMap<>();
        response.put("postId", postId);
        response.put("likeCount", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/posts/{postId}/status")
    public ResponseEntity<Map<String, Object>> hasUserLikedPost(@PathVariable Long postId) {
        boolean hasLiked = likeService.hasUserLikedPost(postId);
        Map<String, Object> response = new HashMap<>();
        response.put("postId", postId);
        response.put("hasLiked", hasLiked);
        return ResponseEntity.ok(response);
    }



    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String
            , String>> handleAccessDeniedException(AccessDeniedException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "You are not authorized to perform this action.");
        return ResponseEntity.status(403).body(response);
    }
} 