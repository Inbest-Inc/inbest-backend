package com.inbest.backend.service;

import com.inbest.backend.exception.UserNotFoundException;
import com.inbest.backend.model.Like;
import com.inbest.backend.model.Post;
import com.inbest.backend.model.User;
import com.inbest.backend.repository.LikeRepository;
import com.inbest.backend.repository.PostRepository;
import com.inbest.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional
    public Like likePost(Long postId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        if (likeRepository.existsByUserAndPost(user, post)) {
            throw new IllegalStateException("User has already liked this post");
        }

        Like like = new Like();
        like.setUser(user);
        like.setPost(post);
        likeRepository.save(like);
        
        // Update post's like count
        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);
        
        return like;
    }

    @Transactional
    public void unlikePost(Long postId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        Like like = likeRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new EntityNotFoundException("Like not found"));

        likeRepository.delete(like);
        
        // Update post's like count
        post.setLikeCount(post.getLikeCount() - 1);
        postRepository.save(post);
    }

    public long getLikeCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        return likeRepository.countByPostId(postId);
    }

    public boolean hasUserLikedPost(Long postId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        return likeRepository.existsByUserIdAndPostId(user.getId(), postId);
    }
} 