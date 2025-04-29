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
public class PostController
{
    private final PostService postService;

    @PostMapping
    public ResponseEntity<?> createPost(@Valid @RequestBody PostCreateDTO postDTO)
    {
        try
        {
            PostResponseDTO createdPost = postService.createPost(postDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Post created successfully");
            response.put("post", createdPost);

            return ResponseEntity.ok(response);
        }
        catch (IllegalStateException e)
        {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllPosts()
    {
        try
        {
            List<PostResponseDTO> allPosts = postService.getAllPosts();
            if (allPosts.isEmpty())
            {
                return ResponseEntity.status(200).body(Map.of(
                        "status", "success",
                        "message", "User do not have any posts"));
            }
            return ResponseEntity.status(200).body(Map.of(
                    "status", "success",
                    "message", "Posts found!",
                    "data", allPosts
            ));
        }
        catch (DataAccessException e)
        {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "An error occurred while accessing the database. Please try again later.");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/followed")
    public ResponseEntity<?> getFollowedUsersPosts(
            @RequestParam(defaultValue = "1") int page)
    {
        if (page < 1) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Page number must be greater than 0"
            ));
        }

        try
        {
            List<PostResponseDTO> followedPosts = postService.getPostsFromFollowedUsers(page, 10);
            long totalPosts = postService.getTotalFollowedPostsCount();
            int totalPages = (int) Math.ceil((double) totalPosts / 10);
            int nextPage = page + 1 <= totalPages ? page + 1 : -1;

            if (followedPosts.isEmpty())
            {
                return ResponseEntity.status(200).body(Map.of(
                        "status", "success",
                        "message", "No posts found from followed users",
                        "page", page,
                        "totalPosts", totalPosts,
                        "totalPages", totalPages,
                        "nextPage", nextPage
                ));
            }

            return ResponseEntity.status(200).body(Map.of(
                    "status", "success",
                    "message", "Posts from followed users fetched successfully",
                    "data", followedPosts,
                    "page", page,
                    "totalPosts", totalPosts,
                    "totalPages", totalPages,
                    "nextPage", nextPage
            ));
        }
        catch (Exception e)
        {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "An error occurred while fetching posts from followed users");
            return ResponseEntity.status(500).body(response);
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable Long id)
    {
        try
        {
            Optional<PostResponseDTO> post = postService.getPostById(id);
            if (post.isPresent())
            {
                return ResponseEntity.status(200).body(Map.of(
                        "status", "success",
                        "message", "Posts found!",
                        "data", post.get()
                ));
            }
            return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Post not found"));
        }
        catch (DataAccessException e)
        {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "An error occurred while accessing the database. Please try again later.");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<?> getPostsByUsername(@PathVariable String username)
    {
        try
        {
            List<PostResponseDTO> posts = postService.getPostsByUsername(username);
            if (posts.isEmpty())
            {
                return ResponseEntity.status(404).body(Map.of(
                        "status", "error",
                        "message", "No posts found for user: " + username));
            }
            return ResponseEntity.status(200).body(Map.of(
                    "status", "success",
                    "message", "Posts found!",
                    "data", posts
            ));
        }
        catch (UserNotFoundException e)
        {
            return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
        catch (DataAccessException e)
        {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "An error occurred while accessing the database. Please try again later.");
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id)
    {
        try
        {
            postService.deletePost(id);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Post deleted successfully"));
        }
        catch (IllegalStateException e)
        {
            return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
        catch (DataAccessException e)
        {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "An error occurred while accessing the database. Please try again later.");
            return ResponseEntity.status(500).body(response);
        }
    }


    @GetMapping("/trending")
    public ResponseEntity<?> getTrendingPosts(
            @RequestParam(defaultValue = "1") int page)
    {
        if (page < 1) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Page number must be greater than 0"
            ));
        }

        List<PostResponseDTO> trendingPosts = postService.getTrendingPosts(page, 10);
        long totalPosts = postService.getTotalTrendingPostsCount();
        int totalPages = (int) Math.ceil((double) totalPosts / 10);
        int nextPage = page + 1 <= totalPages ? page + 1 : -1;

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Trending posts fetched successfully",
                "data", trendingPosts,
                "page", page,
                "totalPosts", totalPosts,
                "totalPages", totalPages,
                "nextPage", nextPage
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserPosts()
    {
        try
        {
            List<PostResponseDTO> userPosts = postService.getCurrentUserPosts();
            if (userPosts.isEmpty())
            {
                return ResponseEntity.status(200).body(Map.of(
                        "status", "success",
                        "message", "You don't have any posts yet"
                ));
            }
            return ResponseEntity.status(200).body(Map.of(
                    "status", "success",
                    "message", "Your posts fetched successfully",
                    "data", userPosts
            ));
        }
        catch (Exception e)
        {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "An error occurred while fetching your posts");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/portfolio/{portfolioId}")
    public List<PostResponseDTO> getPostsByPortfolio(@PathVariable Long portfolioId)
    {
        return postService.getPostsByPortfolio(portfolioId);
    }
}
