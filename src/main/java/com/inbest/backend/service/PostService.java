package com.inbest.backend.service;

import com.inbest.backend.dto.PostCreateDTO;
import com.inbest.backend.dto.PostResponseDTO;
import com.inbest.backend.exception.UserNotFoundException;
import com.inbest.backend.model.InvestmentActivity;
import com.inbest.backend.model.Post;
import com.inbest.backend.model.User;
import com.inbest.backend.repository.InvestmentActivityRepository;
import com.inbest.backend.repository.PostRepository;
import com.inbest.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final InvestmentActivityRepository investmentActivityRepository;

    @Transactional
    public PostResponseDTO createPost(PostCreateDTO postDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        InvestmentActivity investmentActivity = investmentActivityRepository.findById(postDTO.getInvestmentActivityId())
                .orElseThrow(() -> new IllegalStateException("Investment activity not found"));

        Post post = new Post();
        post.setContent(postDTO.getContent());
        post.setUser(user);
        post.setInvestmentActivity(investmentActivity);
        post.setLikeCount(0);
        post.setIsTrending(false);
       

        Post savedPost = postRepository.save(post);
        return convertToDTO(savedPost);
    }

    public List<PostResponseDTO> getAllPosts() {
        return postRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<PostResponseDTO> getPostById(Long id) {
        return postRepository.findById(id)
                .map(this::convertToDTO);
    }


    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Post not found"));
        postRepository.delete(post);
    }

    private PostResponseDTO convertToDTO(Post post) {
        PostResponseDTO dto = new PostResponseDTO();
        dto.setId(post.getId());
        dto.setContent(post.getContent());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUserId(post.getUser().getId().longValue());
        dto.setUsername(post.getUser().getUsername());
        dto.setStockSymbol(post.getInvestmentActivity().getStock().getTickerSymbol());
        dto.setActionType(post.getInvestmentActivity().getActionType().name());
        dto.setAmount(post.getInvestmentActivity().getAmount());
        dto.setLikeCount(post.getLikeCount());
        dto.setTrending(post.getIsTrending());
        return dto;
    }
}
