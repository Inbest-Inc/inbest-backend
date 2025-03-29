package com.inbest.backend.controller;

import com.inbest.backend.dto.PostCreateDTO;
import com.inbest.backend.dto.PostResponseDTO;
import com.inbest.backend.service.PostService;
import com.inbest.backend.exception.UserNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<?> createPost(@Valid @RequestBody PostCreateDTO postDTO) {
        try {
            return ResponseEntity.ok(postService.createPost(postDTO));
        } catch (IllegalStateException e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllPosts() {
        try {
            List<PostResponseDTO> allPosts = postService.getAllPosts();
            if (allPosts.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                        "status", "error",
                        "message", "Posts not found"));
            }
            return ResponseEntity.ok(allPosts);
        } catch (DataAccessException e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "An error occurred while accessing the database. Please try again later.");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable Long id) {
        try {
            Optional<PostResponseDTO> post = postService.getPostById(id);
            if (post.isPresent()) {
                return ResponseEntity.ok(post.get());
            }
            return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Post not found"));
        } catch (DataAccessException e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "An error occurred while accessing the database. Please try again later.");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<?> getPostsByUsername(@PathVariable String username) {
        try {
            List<PostResponseDTO> posts = postService.getPostsByUsername(username);
            if (posts.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                        "status", "error",
                        "message", "No posts found for user: " + username));
            }
            return ResponseEntity.ok(posts);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        } catch (DataAccessException e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "An error occurred while accessing the database. Please try again later.");
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        try {
            postService.deletePost(id);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Post deleted successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        } catch (DataAccessException e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "An error occurred while accessing the database. Please try again later.");
            return ResponseEntity.status(500).body(response);
        }
    }
}
