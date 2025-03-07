package com.inbest.backend.controller;

import com.inbest.backend.dto.CommentDTO;
import com.inbest.backend.dto.CommentResponseDTO;
import com.inbest.backend.model.Comment;
import com.inbest.backend.model.User;
import com.inbest.backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/comments")
public class CommentController
{
    @Autowired
    private CommentService commentService;

    @PostMapping("/create")
    public ResponseEntity<Comment> createComment(@RequestBody CommentDTO commentDto, @AuthenticationPrincipal User user) {
        Comment comment = commentService.save(commentDto, user);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/get-all-comments/{postId}")
    public ResponseEntity<Set<CommentResponseDTO>> getAllCommentsByPost(@PathVariable Integer postId) {
        Set<CommentResponseDTO> comments = commentService.getCommentByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/delete/comment/{commentId}")
    public ResponseEntity<?> deleteComment (@PathVariable Integer commentId)
    {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok("Comment deleted");
    }
}
