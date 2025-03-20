package com.inbest.backend.controller;

import com.inbest.backend.model.Like;
import com.inbest.backend.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    @PostMapping("/posts/{postId}")
    public ResponseEntity<?> likePost(@PathVariable Long postId) {
        try {
            likeService.likePost(postId);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Post liked successfully!");
            response.put("postId", String.valueOf(postId));
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Post not found");
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> unlikePost(@PathVariable Long postId) {
        try {
            likeService.unlikePost(postId);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Post unliked successfully!");
            response.put("postId", String.valueOf(postId));
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Post not found");
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/posts/{postId}/count")
    public ResponseEntity<?> getLikeCount(@PathVariable Long postId) {
        try {
            long count = likeService.getLikeCount(postId);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("postId", postId);
            response.put("likeCount", count);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Post not found");
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/posts/{postId}/status")
    public ResponseEntity<?> hasUserLikedPost(@PathVariable Long postId) {
        try {
            boolean hasLiked = likeService.hasUserLikedPost(postId);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("postId", postId);
            response.put("hasLiked", hasLiked);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Post not found");
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 