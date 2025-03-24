package com.inbest.backend.controller;

import com.inbest.backend.dto.CommentDTO;
import com.inbest.backend.dto.CommentResponseDTO;
import com.inbest.backend.model.Comment;
import com.inbest.backend.model.User;
import com.inbest.backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/comments")
public class CommentController
{

    @Autowired
    private CommentService commentService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createComment(@RequestBody CommentDTO commentDto, @AuthenticationPrincipal User user)
    {
        try
        {
            Comment comment = commentService.save(commentDto, user);
            Map<String, String> response = Map.of("status", "success", "message", "Comment created successfully");
            return ResponseEntity.ok(response);
        }
        catch (Exception e)
        {
            Map<String, String> response = Map.of("status", "error", "message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/get-all-comments/{postId}")
    public ResponseEntity<Map<String, Object>> getAllCommentsByPost(@PathVariable Integer postId)
    {
        try
        {
            Set<CommentResponseDTO> comments = commentService.getCommentByPostId(postId);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Comments fetched successfully");
            response.put("data", comments);

            return ResponseEntity.ok(response);
        }
        catch (Exception e)
        {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to fetch comments");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/delete/comment/{commentId}")
    public ResponseEntity<Map<String, String>> deleteComment(@PathVariable Integer commentId)
    {
        try
        {
            commentService.deleteComment(commentId);
            Map<String, String> response = Map.of("status", "success", "message", "Comment deleted successfully");
            return ResponseEntity.ok(response);
        }
        catch (SecurityException e)
        {
            Map<String, String> response = Map.of("status", "error", "message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        catch (Exception e)
        {
            Map<String, String> response = Map.of("status", "error", "message", "An error occurred while deleting the comment");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
