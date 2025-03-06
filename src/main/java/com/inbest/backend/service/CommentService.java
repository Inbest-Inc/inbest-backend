package com.inbest.backend.service;

import com.inbest.backend.dto.CommentDTO;
import com.inbest.backend.model.Comment;
import com.inbest.backend.model.Post;
import com.inbest.backend.model.User;
import com.inbest.backend.repository.CommentRepository;
import com.inbest.backend.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CommentService
{

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    public Comment save(CommentDTO commentDTO, User user)
    {
        Comment comment = new Comment();
        Post post = postRepository.getById(commentDTO.getPostId());
        comment.setComment(commentDTO.getComment());
        comment.setUser(user);
        comment.setPost(post);
        comment.setCreatedDate(LocalDateTime.now());
        return commentRepository.save(comment);
    }
}
