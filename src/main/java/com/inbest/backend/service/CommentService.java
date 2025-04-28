package com.inbest.backend.service;

import com.inbest.backend.dto.CommentDTO;
import com.inbest.backend.dto.CommentResponseDTO;
import com.inbest.backend.model.Comment;
import com.inbest.backend.model.Post;
import com.inbest.backend.model.User;
import com.inbest.backend.repository.CommentRepository;
import com.inbest.backend.repository.PostRepository;
import com.inbest.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommentService
{

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationService authenticationService;

    public Comment save(CommentDTO commentDTO)
    {
        String username = authenticationService.authenticateUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(commentDTO.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = new Comment();
        comment.setComment(commentDTO.getComment());
        comment.setUser(user);
        comment.setPost(post);
        comment.setCreatedDate(LocalDateTime.now());

        post.setCommentCount(post.getCommentCount() + 1);

        return commentRepository.save(comment);
    }

    public Set<CommentResponseDTO> getCommentByPostId(Integer postId)
    {
        Set<Comment> comments = commentRepository.findByPostId(postId);

        return comments.stream()
                .map(comment -> new CommentResponseDTO(
                        comment.getUser().getUsername(),
                        comment.getUser().getId(),
                        comment.getComment(),
                        comment.getCreatedDate(),
                        comment.getUser().getImageUrl(),
                        comment.getUser().getName() + " " + comment.getUser().getSurname()
                ))
                .collect(Collectors.toSet());
    }

    public void deleteComment(Integer commentId)
    {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with ID: " + commentId));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        if (!comment.getUser().getUsername().equals(username))
        {
            throw new SecurityException("You can only delete your own comment!");
        }
        
        // Decrease post's comment count
        Post post = comment.getPost();
        post.setCommentCount(post.getCommentCount() - 1);
        postRepository.save(post);
        
        commentRepository.delete(comment);
    }
}
