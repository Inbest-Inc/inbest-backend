package com.inbest.backend.controller;

import com.inbest.backend.dto.CommentDTO;
import com.inbest.backend.model.Comment;
import com.inbest.backend.model.User;
import com.inbest.backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
