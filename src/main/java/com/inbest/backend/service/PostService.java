package com.inbest.backend.service;

import com.inbest.backend.dto.PostCreateDTO;
import com.inbest.backend.dto.PostResponseDTO;
import com.inbest.backend.dto.FollowDTO;
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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final InvestmentActivityRepository investmentActivityRepository;
    private final FollowService followService;

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
        post.setCommentCount(0);
        post.setIsTrending(false);
       

        Post savedPost = postRepository.save(post);
        return convertToDTO(savedPost);
    }

    public List<PostResponseDTO> getAllPosts() {
        return postRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PostResponseDTO> getPostsFromFollowedUsers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        List<FollowDTO> followedUsers = followService.getFollowing(username);
        List<User> users = followedUsers.stream()
                .map(followDTO -> userRepository.findByUsername(followDTO.getUsername())
                        .orElseThrow(() -> new UserNotFoundException("User not found")))
                .collect(Collectors.toList());
        
        return postRepository.findByUserInOrderByCreatedAtDesc(users).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<PostResponseDTO> getPostById(Long id) {
        return postRepository.findById(id)
                .map(this::convertToDTO);
    }

    public List<PostResponseDTO> getPostsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return postRepository.findByUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Post not found"));
        postRepository.delete(post);
    }

    @Transactional
public void updateAllPostScores() {
    List<Post> posts = postRepository.findAll();
    for (Post post : posts) {
        double score = calculatePostScore(post);
        post.setTrendScore(score);
    }
    postRepository.saveAll(posts);
}

    private double calculatePostScore(Post post) {
        // Get post age in hours
        long minutesSinceCreation = ChronoUnit.MINUTES.between(post.getCreatedAt(), LocalDateTime.now());
        int hoursSinceCreation = (int)Math.ceil(minutesSinceCreation / 60.0);

        // Weight factors
        double likeWeight = 1.0;
        double commentWeight = 2.0; // Comments are weighted more than likes
        double timeDecayFactor = 0.95; // Score decays over time
        
        // Calculate base score
        double baseScore = (post.getLikeCount() * likeWeight) + 
                          (post.getCommentCount() * commentWeight);
        
        // Apply time decay
        double timeDecay = Math.pow(timeDecayFactor, hoursSinceCreation);
        
        return baseScore * timeDecay;
    }

    public List<PostResponseDTO> getTrendingPosts() {
        return postRepository.findAllOrderByScoreDesc()
                .stream()
                .limit(10) //trend post size
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

  

    private PostResponseDTO convertToDTO(Post post) {
        PostResponseDTO dto = new PostResponseDTO();
        dto.setId(post.getId());
        dto.setContent(post.getContent());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUsername(post.getUser().getUsername());
        dto.setStockSymbol(post.getInvestmentActivity().getStock().getTickerSymbol());
        dto.setActionType(post.getInvestmentActivity().getActionType().name());
        dto.setLikeCount(post.getLikeCount());
        dto.setCommentCount(post.getCommentCount());
        dto.setTrending(post.getIsTrending());
        return dto;
    }
}
